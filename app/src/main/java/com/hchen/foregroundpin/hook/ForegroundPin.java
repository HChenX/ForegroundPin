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

import static com.hchen.hooktool.log.XposedLog.logE;
import static com.hchen.hooktool.log.XposedLog.logW;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.view.SurfaceControl;

import androidx.annotation.NonNull;

import com.hchen.foregroundpin.utils.HangupHelper;
import com.hchen.foregroundpin.utils.ObserverHelper;
import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.callback.IAction;
import com.hchen.hooktool.tool.ParamTool;
import com.hchen.hooktool.utils.SystemSDK;

import java.util.HashMap;

public class ForegroundPin extends BaseHC {
    private boolean isObserver = false;
    private final HangupHelper mHandler = new HangupHelper();
    private final HandlerHelper handlerHelper = new HandlerHelper();
    private final ObserverHelper observerHelper = new ObserverHelper();
    private boolean fail = false;
    private final int CANCEL_HANGUP = HangupHelper.CANCEL_HANGUP;
    private final int WILL_HANGUP = HangupHelper.WILL_HANGUP;
    private final int LOW_TIME_HANGUP = HangupHelper.LOW_TIME_HANGUP;
    private static final int TOP_WINDOW_HAS_DRAWN = 1;

    private final HashMap<String, Integer> hashMap = new HashMap<>();

    @Override
    public void init() {
        if (SystemSDK.isMoreHyperOSVersion(1f)) {
            /*Hyper*/
            hcHook.findClass("mffgc", "com.android.server.wm.MiuiFreeFormGestureController")
                    .getMethod("onATMSSystemReady")
                    .hook(new IAction() {
                        @Override
                        public void after(ParamTool param) {
                            Object mService = param.getField("mService");
                            Context mContext = param.to(mService).getField("mContext");
                            if (mContext == null) return;
                            if (!isObserver) {
                                observerHelper.setTAG(TAG);
                                observerHelper.setObserver(mContext, hashMap, mHandler.hangupMap);
                                isObserver = true;
                            }
                        }
                    }).getMethod("needForegroundPin", "com.android.server.wm.MiuiFreeFormActivityStack")
                    .hook(new IAction() {
                        @Override
                        public void before(ParamTool param) {
                            if (observerHelper.findInMap(hashMap, getPackageName(param, 0))) {
                                param.setResult(true);
                                return;
                            }
                            param.setResult(false);
                        }
                    });
        } else {
            logW(TAG, "Hyper UnForegroundPin E, if you is Miui don't worry.");
            /*Miui*/
            hcHook.findClass("ams", "com.android.server.am.ActivityManagerService")
                    .getMethod("systemReady", Runnable.class,
                            "com.android.server.utils.TimingsTraceAndSlog")
                    .hook(new IAction() {
                        @Override
                        public void after(ParamTool param) {
                            Context mContext = param.getField("mContext");
                            if (mContext == null) return;
                            if (!isObserver) {
                                observerHelper.setObserver(mContext, hashMap, mHandler.hangupMap);
                                isObserver = true;
                            }
                        }
                    })

                    .findClass("mffms", "com.android.server.wm.MiuiFreeFormManagerService")
                    .getMethod("dispatchFreeFormStackModeChanged",
                            int.class, "com.android.server.wm.MiuiFreeFormActivityStack")
                    .hook(new IAction() {
                        @Override
                        public void after(ParamTool param) {
                            String pkg = getPackageName(param, 1);
                            Object mActivityTaskManagerService = param.getField("mActivityTaskManagerService");
                            Context context = param.to(mActivityTaskManagerService).getField("mContext");
                            if (observerHelper.findInMap(hashMap, pkg)) {
                                int action = param.first();
                                if (action == 6) {
                                    fail = false;
                                    removeHandler();
                                    mHandler.setContext(context);
                                    mHandler.sendMessageDelayed(mHandler.obtainMessage(WILL_HANGUP, pkg), 1000);
                                } else if (action == 7) {
                                    if (fail) return;
                                    removeHandler();
                                    mHandler.setContext(context);
                                    if (!observerHelper.findInMap(mHandler.hangupMap, pkg))
                                        mHandler.sendMessage(mHandler.obtainMessage(LOW_TIME_HANGUP));
                                }
                            }
                        }
                    })

                    .findClass("mffwmh", "com.android.server.wm.MiuiFreeFormWindowMotionHelper")
                    .getMethod("setLeashPositionAndScale", Rect.class, "com.android.server.wm.MiuiFreeFormActivityStack")
                    .hook(new IAction() {
                        @Override
                        public void after(ParamTool param) {
                            if (fail) return;
                            String pkg = getPackageName(param, 1);
                            Object mListener = param.getField("mListener");
                            Object mService = param.to(mListener).getField("mService");
                            Context mContext = param.to(mService).getField("mContext");
                            if (observerHelper.findInMap(hashMap, pkg)) {
                                fail = true;
                                removeHandler();
                                mHandler.setContext(mContext);
                                mHandler.sendMessage(mHandler.obtainMessage(CANCEL_HANGUP));
                            }
                        }
                    })

                    .findClass("mffpms", "com.android.server.wm.MiuiFreeformPinManagerService")
                    .getMethod("hideStack", "com.android.server.wm.MiuiFreeFormActivityStack")
                    .hook(new IAction() {
                        @Override
                        public void before(ParamTool param) {
                            String pkg = getPackageName(param, 0);
                            if (observerHelper.findInMap(hashMap, pkg)) {
                                if (observerHelper.findInMap(mHandler.hangupMap, pkg)) {
                                    Parcel obtain = Parcel.obtain();
                                    Parcel obtain1 = Parcel.obtain();
                                    obtain.writeInterfaceToken("android.app.IActivityManager");
                                    obtain.writeString(pkg);
                                    Class<?> clz = param.findClass("android.os.MiuiBinderTransaction$IActivityManager");
                                    Class<?> clz1 = param.findClass("android.app.ActivityManager");
                                    Object getService = param.to(clz1).callStaticMethod("getService");
                                    Object asBinder = param.to(getService).callMethod("asBinder");
                                    int TRANSACT_ID_SET_PACKAGE_HOLD_ON = param.to(clz).getStaticField("TRANSACT_ID_SET_PACKAGE_HOLD_ON");
                                    param.to(asBinder).callMethod("transact", new Object[]{TRANSACT_ID_SET_PACKAGE_HOLD_ON, obtain, obtain1, 0});
                                    mHandler.hangupMap.remove(pkg);
                                }
                            }
                        }
                    })
                    /*.getMethod("unPinFloatingWindow", "com.android.server.wm.MiuiFreeFormActivityStack",
                            float.class, float.class, boolean.class)
                    .hook(new IAction() {
                        @Override
                        public void before(ParamTool param) {
                            String pkg = getPackageName(param, 0);
                            if (observerHelper.findInMap(hashMap, pkg)) {
                                if (observerHelper.findInMap(mHandler.hangupMap, pkg)) {
                                    mHandler.hangupMap.remove(pkg);
                                }
                            }
                        }
                    })*/

                    .findClass("mffgc", "com.android.server.wm.MiuiFreeFormGestureController")
                    .getMethod("moveTaskToBack",
                            "com.android.server.wm.MiuiFreeFormActivityStack")
                    .hook(new IAction() {
                        @Override
                        public void before(ParamTool param) {
                            if (observerHelper.findInMap(hashMap, getPackageName(param, 0))) {
                                // logE(tag, "back pkg: " + pkg);
                                param.setResult(null);
                            }
                        }
                    })

                    .findClass("task", "com.android.server.wm.Task")
                    .getAnyConstructor()
                    .hook(new IAction() {
                        @Override
                        public void after(ParamTool param) {
                            param.setAdditionalInstanceField("mLastSurfaceVisibility", false);
                        }
                    })
                    .getMethod("prepareSurfaces")
                    .hook(new IAction() {
                        @Override
                        public void after(ParamTool param) {
                            String pkg = param.callMethod("getPackageName");
                            if (pkg != null) {
                                Integer state = hashMap.get(pkg);
                                if (state != null && state == 1) {
                                    // logE(tag, "pkg: " + pkg);
                                } else {
                                    return;
                                }
                            }
                            SurfaceControl.Transaction transaction = param.callMethod("getSyncTransaction");
                            SurfaceControl mSurfaceControl = param.getField("mSurfaceControl");
                            // String mCallingPackage = (String) XposedHelpers.getObjectField(param.thisObject, "mCallingPackage");
                            // Object getTopNonFinishingActivity = XposedHelpers.callMethod(param.thisObject, "getTopNonFinishingActivity");
                            // String pkg = null;
                            // if (getTopNonFinishingActivity != null) {
                            //     ActivityInfo activityInfo = (ActivityInfo) XposedHelpers.getObjectField(getTopNonFinishingActivity, "info");
                            //     if (activityInfo != null) {
                            //         pkg = activityInfo.applicationInfo.packageName;
                            //     }
                            // }
                            int taskId = param.callMethod("getRootTaskId");
                            Object mWmService = param.getField("mWmService");
                            Object mAtmService = param.to(mWmService).getField("mAtmService");
                            Object mMiuiFreeFormManagerService = param.to(mAtmService).getField("mMiuiFreeFormManagerService");
                            Object mffs = param.to(mMiuiFreeFormManagerService).callMethod("getMiuiFreeFormActivityStack", taskId);
                            boolean isVisible = param.callMethod("isVisible");
                            boolean isAnimating = param.callMethod("isAnimating", 7);
                            boolean inPinMode = false;
                            if (mffs != null) {
                                inPinMode = param.to(mffs).callMethod("inPinMode");
                            }
                            boolean mLastSurfaceVisibility = param.getAdditionalInstanceField("mLastSurfaceVisibility");
                            if (mSurfaceControl != null && mffs != null && inPinMode) {
                                if (!isAnimating) {
                                    param.to(transaction).callMethod("setVisibility", new Object[]{mSurfaceControl, false});
                                    param.setAdditionalInstanceField("mLastSurfaceVisibility", false);
                                }
                                // logE(tag, "setVisibility false pkg2: " + pkg + " taskid: " + taskId + " isVisble: " + isVisible
                                // + " an: " + isAnimating + " la: " + mLastSurfaceVisibility);
                            } else if (mSurfaceControl != null && mffs != null && !inPinMode) {
                                if (!mLastSurfaceVisibility) {
                                    param.to(transaction).callMethod("setVisibility", new Object[]{mSurfaceControl, true});
                                    param.setAdditionalInstanceField("mLastSurfaceVisibility", true);
                                }
                                // logE(tag, "setVisibility true pkg2: " + pkg + " taskid: " + taskId + " isVisble: " + isVisible + " an: " + isAnimating);
                            }
                            // logE(tag, "sur: " + mSurfaceControl + " tra: " + transaction + " pkg: " + pkg + " inpin: " + inPinMode);
                        }
                    })

                    .to("mffgc")
                    .getMethod("moveTaskToFront",
                            "com.android.server.wm.MiuiFreeFormActivityStack")
                    .hook(new IAction() {
                        @Override
                        public void before(ParamTool param) {
                            if (observerHelper.findInMap(hashMap, getPackageName(param, 0))) {
                                // logE(tag, "front pkg: " + pkg);
                                param.setResult(null);
                            }
                        }
                    })

                    .to("mffpms")
                    .getMethod("lambda$unPinFloatingWindow$0$com-android-server-wm-MiuiFreeformPinManagerService",
                            "com.android.server.wm.MiuiFreeFormActivityStack",
                            float.class, float.class, boolean.class, "com.android.server.wm.DisplayContent",
                            "com.android.server.wm.MiuiFreeFormFloatIconInfo")
                    .hook(new IAction() {
                        @Override
                        public void before(ParamTool param) {
                            handlerHelper.sendMessageDelayed(handlerHelper.obtainMessage(TOP_WINDOW_HAS_DRAWN, param), 150);
                        }
                    })

                    .to("mffgc")
                    .getMethod("lambda$startFullscreenFromFreeform$2$com-android-server-wm-MiuiFreeFormGestureController",
                            "com.android.server.wm.MiuiFreeFormActivityStack")
                    .hook(new IAction() {
                        @Override
                        public void before(ParamTool param) {
                            handlerHelper.sendMessageDelayed(handlerHelper.obtainMessage(TOP_WINDOW_HAS_DRAWN, param), 150);
                        }
                    });
        }
    }

    private void removeHandler() {
        mHandler.removeMessages(WILL_HANGUP);
        mHandler.removeMessages(CANCEL_HANGUP);
        mHandler.removeMessages(LOW_TIME_HANGUP);
    }

    private String getPackageName(ParamTool param, int value) {
        Object mffas = param.getParam(value);
        if (mffas != null) {
            return param.to(mffas).callMethod("getStackPackageName");
        }
        return null;
    }

    private static class HandlerHelper extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case TOP_WINDOW_HAS_DRAWN -> {
                    ParamTool param = (ParamTool) msg.obj;
                    Object mffas = param.first();
                    Object mLock = param.getField("mLock");
                    param.to(mffas).setField("topWindowHasDrawn", true);
                    try {
                        if (mLock == null) {
                            Object mMiuiFreeformPinManagerService = param.getField("mMiuiFreeformPinManagerService");
                            mLock = param.to(mMiuiFreeformPinManagerService).getField("mLock");
                            if (mLock != null) {
                                synchronized (mLock) {
                                    mLock.notifyAll();
                                }
                            }
                        } else {
                            synchronized (mLock) {
                                mLock.notifyAll();
                            }
                        }
                    } catch (Throwable e) {
                        logE("ForegroundPin", e);
                    }
                }
            }
        }
    }
}
