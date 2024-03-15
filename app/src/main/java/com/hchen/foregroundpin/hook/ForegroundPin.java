/*
 * This file is part of ForegroundPin.

 * ForegroundPin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.

 * Copyright (C) 2023-2024 ForegroundPin Contributions
 */
package com.hchen.foregroundpin.hook;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.SurfaceControl;

import androidx.annotation.Nullable;

import com.hchen.foregroundpin.mode.Hook;
import com.hchen.foregroundpin.utils.settings.SettingsData;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class ForegroundPin extends Hook {
    private boolean isObserver = false;
    private final HashMap<String, Integer> hashMap = new HashMap<>();

    @Override
    public void init() {
        try {
            /*Hyper*/
            getDeclaredMethod("com.android.server.wm.MiuiFreeFormGestureController",
                    "needForegroundPin",
                    "com.android.server.wm.MiuiFreeFormActivityStack");
            findAndHookConstructor("com.android.server.wm.MiuiFreeFormGestureController",
                    "com.android.server.wm.ActivityTaskManagerService",
                    "com.android.server.wm.MiuiFreeFormManagerService", Handler.class,
                    new HookAction() {
                        @Override
                        protected void after(MethodHookParam param) {
                            Object service = param.args[0];
                            Context mContext = (Context) getObjectField(service, "mContext");
                            if (mContext == null) return;
                            if (!isObserver) {
                                // logE(tag, "isObserver");
                                ContentObserver contentObserver = new ContentObserver(new Handler(mContext.getMainLooper())) {
                                    @Override
                                    public void onChange(boolean selfChange, @Nullable Uri uri, int flags) {
                                        hashMap.clear();
                                        String data = getPin(mContext);
                                        if (data == null) return;
                                        ArrayList<JSONObject> jsonObjects = SettingsData.toArray(data);
                                        for (JSONObject object : jsonObjects) {
                                            String pkg = SettingsData.getPkg(object);
                                            // logE(tag, "add pkg: " + pkg);
                                            hashMap.put(pkg, 1);
                                        }
                                    }
                                };
                                mContext.getContentResolver().registerContentObserver(
                                        Settings.Secure.getUriFor("foreground_pin_param"),
                                        false, contentObserver);
                                isObserver = true;
                            }
                        }
                    }
            );

            findAndHookMethod("com.android.server.wm.MiuiFreeFormGestureController",
                    "needForegroundPin",
                    "com.android.server.wm.MiuiFreeFormActivityStack",
                    new HookAction() {
                        @Override
                        protected void before(XC_MethodHook.MethodHookParam param) {
                            Object mffas = param.args[0];
                            String pkg = (String) callMethod(mffas, "getStackPackageName");
                            Integer state = hashMap.get(pkg);
                            if (state != null && state == 1) {
                                param.setResult(true);
                            }
                        }
                    }
            );
        } catch (Throwable throwable) {
            logE(tag, "Hyper UnForegroundPin E, if you is Miui don't worry : " + throwable);
            /*Miui*/
            findAndHookMethod("com.android.server.am.ActivityManagerService",
                    "systemReady", Runnable.class,
                    "com.android.server.utils.TimingsTraceAndSlog",
                    new HookAction() {
                        @Override
                        protected void after(MethodHookParam param) {
                            Context mContext = (Context) getObjectField(param.thisObject, "mContext");
                            if (mContext == null) return;
                            if (!isObserver) {
                                // logE(tag, "isObserver");
                                ContentObserver contentObserver = new ContentObserver(new Handler(mContext.getMainLooper())) {
                                    @Override
                                    public void onChange(boolean selfChange, @Nullable Uri uri, int flags) {
                                        hashMap.clear();
                                        String data = getPin(mContext);
                                        if (data == null) return;
                                        ArrayList<JSONObject> jsonObjects = SettingsData.toArray(data);
                                        for (JSONObject object : jsonObjects) {
                                            String pkg = SettingsData.getPkg(object);
                                            // logE(tag, "add pkg: " + pkg);
                                            hashMap.put(pkg, 1);
                                        }
                                    }
                                };
                                mContext.getContentResolver().registerContentObserver(
                                        Settings.Secure.getUriFor("foreground_pin_param"),
                                        false, contentObserver);
                                isObserver = true;
                            }
                        }
                    }
            );

            findAndHookMethod("com.android.server.wm.MiuiFreeFormGestureController",
                    "moveTaskToBack",
                    "com.android.server.wm.MiuiFreeFormActivityStack",
                    new HookAction() {
                        @Override
                        protected void before(XC_MethodHook.MethodHookParam param) {
                            Object stack = param.args[0];
                            if (stack != null) {
                                String pkg = (String) callMethod(stack, "getStackPackageName");
                                if (pkg != null) {
                                    Integer state = hashMap.get(pkg);
                                    if (state != null && state == 1) {
                                        // logE(tag, "back pkg: " + pkg);
                                        param.setResult(null);
                                    }
                                }
                            }
                        }
                    }
            );

            hookAllConstructors("com.android.server.wm.Task",
                    new HookAction() {
                        @Override
                        protected void after(MethodHookParam param) {
                            XposedHelpers.setAdditionalInstanceField(param.thisObject, "mLastSurfaceVisibility", true);
                            // logE(tag, "Task add mLastSurfaceVisibility");
                        }
                    }
            );

            findAndHookMethod("com.android.server.wm.Task",
                    "prepareSurfaces", new HookAction() {
                        @Override
                        protected void after(MethodHookParam param) {
                            String pkg = (String) XposedHelpers.callMethod(param.thisObject, "getPackageName");
                            if (pkg != null) {
                                Integer state = hashMap.get(pkg);
                                if (state != null && state == 1) {
                                    // logE(tag, "pkg: " + pkg);
                                } else {
                                    return;
                                }
                            }
                            SurfaceControl.Transaction transaction = (SurfaceControl.Transaction) XposedHelpers.callMethod(param.thisObject, "getSyncTransaction");
                            SurfaceControl mSurfaceControl = (SurfaceControl) XposedHelpers.getObjectField(param.thisObject, "mSurfaceControl");
                            // String mCallingPackage = (String) XposedHelpers.getObjectField(param.thisObject, "mCallingPackage");
                            // Object getTopNonFinishingActivity = XposedHelpers.callMethod(param.thisObject, "getTopNonFinishingActivity");
                            // String pkg = null;
                            /*if (getTopNonFinishingActivity != null) {
                                ActivityInfo activityInfo = (ActivityInfo) XposedHelpers.getObjectField(getTopNonFinishingActivity, "info");
                                if (activityInfo != null) {
                                    pkg = activityInfo.applicationInfo.packageName;
                                }
                            }*/
                            int taskId = (int) XposedHelpers.callMethod(param.thisObject, "getRootTaskId");
                            Object mWmService = XposedHelpers.getObjectField(param.thisObject, "mWmService");
                            Object mAtmService = XposedHelpers.getObjectField(mWmService, "mAtmService");
                            Object mMiuiFreeFormManagerService = XposedHelpers.getObjectField(mAtmService, "mMiuiFreeFormManagerService");
                            Object mffs = XposedHelpers.callMethod(mMiuiFreeFormManagerService, "getMiuiFreeFormActivityStack", taskId);
                            boolean isVisible = (boolean) XposedHelpers.callMethod(param.thisObject, "isVisible");
                            boolean isAnimating = (boolean) XposedHelpers.callMethod(param.thisObject, "isAnimating", 7);
                            boolean inPinMode = false;
                            if (mffs != null) {
                                inPinMode = (boolean) XposedHelpers.callMethod(mffs, "inPinMode");
                            }
                            boolean mLastSurfaceVisibility = (boolean) XposedHelpers.getAdditionalInstanceField(param.thisObject, "mLastSurfaceVisibility");
                            if (mSurfaceControl != null && mffs != null && inPinMode) {
                                if (!isAnimating) {
                                    XposedHelpers.callMethod(transaction, "setVisibility", mSurfaceControl, false);
                                    XposedHelpers.setAdditionalInstanceField(param.thisObject, "mLastSurfaceVisibility", false);
                                }
                                // logE(tag, "setVisibility false pkg2: " + pkg + " taskid: " + taskId + " isVisble: " + isVisible
                                // + " an: " + isAnimating + " la: " + mLastSurfaceVisibility);
                            } else if (mSurfaceControl != null && mffs != null && !inPinMode) {
                                if (!mLastSurfaceVisibility) {
                                    XposedHelpers.callMethod(transaction, "setVisibility", mSurfaceControl, true);
                                    XposedHelpers.setAdditionalInstanceField(param.thisObject, "mLastSurfaceVisibility", true);
                                }
                                // logE(tag, "setVisibility true pkg2: " + pkg + " taskid: " + taskId + " isVisble: " + isVisible + " an: " + isAnimating);
                            }
                            // logE(tag, "sur: " + mSurfaceControl + " tra: " + transaction + " pkg: " + pkg + " inpin: " + inPinMode);
                        }
                    }
            );

            findAndHookMethod("com.android.server.wm.MiuiFreeFormGestureController",
                    "moveTaskToFront",
                    "com.android.server.wm.MiuiFreeFormActivityStack",
                    new HookAction() {
                        @Override
                        protected void before(XC_MethodHook.MethodHookParam param) {
                            Object stack = param.args[0];
                            if (stack != null) {
                                String pkg = (String) callMethod(stack, "getStackPackageName");
                                if (pkg != null) {
                                    Integer state = hashMap.get(pkg);
                                    if (state != null && state == 1) {
                                        // logE(tag, "front pkg: " + pkg);
                                        param.setResult(null);
                                    }
                                }
                            }
                        }
                    }
            );

            findAndHookMethod("com.android.server.wm.MiuiFreeformPinManagerService",
                    "lambda$unPinFloatingWindow$0$com-android-server-wm-MiuiFreeformPinManagerService",
                    "com.android.server.wm.MiuiFreeFormActivityStack",
                    float.class, float.class, boolean.class, "com.android.server.wm.DisplayContent",
                    "com.android.server.wm.MiuiFreeFormFloatIconInfo",
                    new HookAction() {
                        @Override
                        protected void before(XC_MethodHook.MethodHookParam param) {
                            Object mffas = param.args[0];
                            if (mffas != null) {
                                String pkg = (String) callMethod(mffas, "getStackPackageName");
                                if (pkg != null) {
                                    Integer state = hashMap.get(pkg);
                                    if (state != null && state == 1) {
                                        // logE(tag, "1 pkg: " + pkg);
                                    } else {
                                        return;
                                    }
                                }
                            }
                            Object activityRecord = callMethod(XposedHelpers.getObjectField(mffas, "mTask"),
                                    "getTopNonFinishingActivity");
                            /*遵循安卓日志*/
                            Log.i("MiuiFreeformPinManagerService",
                                    "unPinFloatingWindow mffas: " + mffas + " activityRecord: " + activityRecord);
                            if (activityRecord == null) {
                                param.setResult(null);
                                return;
                            }
                            setObject(mffas, "mEnterVelocityX", param.args[1]);
                            setObject(mffas, "mEnterVelocityY", param.args[2]);
                            setObject(mffas, "mIsEnterClick", param.args[3]);
                            setObject(mffas, "mIsPinFloatingWindowPosInit", false);
                            callMethod(param.thisObject, "setUpMiuiFreeWindowFloatIconAnimation",
                                    mffas, activityRecord, param.args[4], param.args[5]);
                            callMethod(param.thisObject, "startUnPinAnimation", mffas);
                            param.setResult(null);
                        }
                    }
            );

            findAndHookMethod("com.android.server.wm.MiuiFreeFormGestureController",
                    "lambda$startFullscreenFromFreeform$2$com-android-server-wm-MiuiFreeFormGestureController",
                    "com.android.server.wm.MiuiFreeFormActivityStack",
                    new HookAction() {
                        @Override
                        protected void before(MethodHookParam param) {
                            Object mffas = param.args[0];
                            if (mffas != null) {
                                String pkg = (String) callMethod(mffas, "getStackPackageName");
                                if (pkg != null) {
                                    Integer state = hashMap.get(pkg);
                                    if (state != null && state == 1) {
                                        // logE(tag, "2 pkg: " + pkg);
                                    } else {
                                        return;
                                    }
                                }
                            }
                            Object mGestureListener = getObjectField(param.thisObject, "mGestureListener");
                            if ((boolean) callMethod(mffas, "isInFreeFormMode")) {
                                callMethod(mGestureListener, "startFullScreenFromFreeFormAnimation", mffas);
                                Object mTrackManager = getObjectField(param.thisObject, "mTrackManager");
                                if (mTrackManager != null) {
                                    callMethod(mTrackManager, "trackSmallWindowPinedQuitEvent",
                                            callMethod(mffas, "getStackPackageName"),
                                            callMethod(mffas, "getApplicationName"),
                                            (long) getObjectField(mffas, "mPinedStartTime") != 0 ?
                                                    ((float) (System.currentTimeMillis() -
                                                            (long) getObjectField(mffas,
                                                                    "mPinedStartTime"))) / 1000.0f : 0.0f
                                    );
                                }
                            } else if ((boolean) callMethod(mffas, "isInMiniFreeFormMode")) {
                                callMethod(mGestureListener, "startFullScreenFromSmallAnimation", mffas);
                                Object mTrackManager = getObjectField(param.thisObject, "mTrackManager");
                                if (mTrackManager != null) {
                                    callMethod(mTrackManager, "trackMiniWindowPinedQuitEvent",
                                            callMethod(mffas, "getStackPackageName"),
                                            callMethod(mffas, "getApplicationName"),
                                            (long) getObjectField(mffas, "mPinedStartTime") != 0 ?
                                                    ((float) (System.currentTimeMillis() -
                                                            (long) getObjectField(mffas,
                                                                    "mPinedStartTime"))) / 1000.0f : 0.0f
                                    );
                                }
                            }
                            setObject(mffas, "mPinedStartTime", 0L);
                            callMethod(mffas, "setInPinMode", false);
                            param.setResult(null);
                        }
                    }
            );

        }
    }

    private String getPin(Context context) {
        String string = Settings.Secure.getString(context.getContentResolver(), "foreground_pin_param");
        if (string == null) {
            logE(tag, "Get Settings is null!!");
        }
        return string;
    }
}
