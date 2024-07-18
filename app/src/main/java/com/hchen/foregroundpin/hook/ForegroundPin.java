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
            chain("com.android.server.wm.MiuiFreeFormGestureController",
                    method("onATMSSystemReady")
                            .hook(new IAction() {
                                @Override
                                public void after() throws Throwable {
                                    Object mService = getThisField("mService");
                                    Context mContext = getField(mService, "mContext");
                                    if (mContext == null) return;
                                    if (!isObserver) {
                                        observerHelper.setTAG(TAG);
                                        observerHelper.setObserver(mContext, hashMap, mHandler.hangupMap);
                                        isObserver = true;
                                    }
                                }
                            })

                            .method("needForegroundPin", "com.android.server.wm.MiuiFreeFormActivityStack")
                            .hook(new IAction() {
                                @Override
                                public void before() throws Throwable {
                                    if (observerHelper.findInMap(hashMap, getPackageName(this, 0))) {
                                        setResult(true);
                                        return;
                                    }
                                    setResult(false);
                                }
                            })
            );
        } else {
            logW(TAG, "Hyper UnForegroundPin E, if you is Miui don't worry.");
            /*Miui*/
            hook("com.android.server.am.ActivityManagerService", "systemReady", Runnable.class,
                    "com.android.server.utils.TimingsTraceAndSlog", new IAction() {
                        @Override
                        public void after() throws Throwable {
                            Context mContext = getThisField("mContext");
                            if (mContext == null) return;
                            if (!isObserver) {
                                observerHelper.setObserver(mContext, hashMap, mHandler.hangupMap);
                                isObserver = true;
                            }
                        }
                    });

            hook("com.android.server.wm.MiuiFreeFormManagerService", "dispatchFreeFormStackModeChanged",
                    int.class, "com.android.server.wm.MiuiFreeFormActivityStack",
                    new IAction() {
                        @Override
                        public void after() throws Throwable {
                            String pkg = getPackageName(this, 1);
                            Object mActivityTaskManagerService = getThisField("mActivityTaskManagerService");
                            Context context = getField(mActivityTaskManagerService, "mContext");
                            if (observerHelper.findInMap(hashMap, pkg)) {
                                int action = first();
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
                    });

            hook("com.android.server.wm.MiuiFreeFormWindowMotionHelper",
                    "setLeashPositionAndScale", Rect.class, "com.android.server.wm.MiuiFreeFormActivityStack"
                    , new IAction() {
                        @Override
                        public void after() throws Throwable {
                            if (fail) return;
                            String pkg = getPackageName(this, 1);
                            Object mListener = getThisField("mListener");
                            Object mService = getField(mListener, "mService");
                            Context mContext = getField(mService, "mContext");
                            if (observerHelper.findInMap(hashMap, pkg)) {
                                fail = true;
                                removeHandler();
                                mHandler.setContext(mContext);
                                mHandler.sendMessage(mHandler.obtainMessage(CANCEL_HANGUP));
                            }
                        }
                    });

            chain("com.android.server.wm.MiuiFreeformPinManagerService", method(
                    "hideStack", "com.android.server.wm.MiuiFreeFormActivityStack")
                    .hook(new IAction() {
                        @Override
                        public void before() throws Throwable {
                            String pkg = getPackageName(this, 0);
                            if (observerHelper.findInMap(hashMap, pkg)) {
                                if (observerHelper.findInMap(mHandler.hangupMap, pkg)) {
                                    Parcel obtain = Parcel.obtain();
                                    Parcel obtain1 = Parcel.obtain();
                                    obtain.writeInterfaceToken("android.app.IActivityManager");
                                    obtain.writeString(pkg);
                                    Class<?> clz = findClass("android.os.MiuiBinderTransaction$IActivityManager");
                                    Class<?> clz1 = findClass("android.app.ActivityManager");
                                    Object getService = callStaticMethod(clz1, "getService");
                                    Object asBinder = callMethod(getService, "asBinder");
                                    int TRANSACT_ID_SET_PACKAGE_HOLD_ON = getStaticField(clz, "TRANSACT_ID_SET_PACKAGE_HOLD_ON");
                                    callMethod(asBinder, "transact", new Object[]{TRANSACT_ID_SET_PACKAGE_HOLD_ON, obtain, obtain1, 0});
                                    mHandler.hangupMap.remove(pkg);
                                }
                            }
                        }
                    })

                    .method("lambda$unPinFloatingWindow$0$com-android-server-wm-MiuiFreeformPinManagerService",
                            "com.android.server.wm.MiuiFreeFormActivityStack",
                            float.class, float.class, boolean.class, "com.android.server.wm.DisplayContent",
                            "com.android.server.wm.MiuiFreeFormFloatIconInfo")
                    .hook(new IAction() {
                        @Override
                        public void before() throws Throwable {
                            handlerHelper.sendMessageDelayed(handlerHelper.obtainMessage(TOP_WINDOW_HAS_DRAWN, this), 150);
                        }
                    }));


            chain("com.android.server.wm.MiuiFreeFormGestureController", method(
                    "moveTaskToBack",
                    "com.android.server.wm.MiuiFreeFormActivityStack")
                    .hook(
                            new IAction() {
                                @Override
                                public void before() throws Throwable {
                                    if (observerHelper.findInMap(hashMap, getPackageName(this, 0))) {
                                        // logE(tag, "back pkg: " + pkg);
                                        setResult(null);
                                    }
                                }
                            }
                    )

                    .method("moveTaskToFront",
                            "com.android.server.wm.MiuiFreeFormActivityStack")
                    .hook(new IAction() {
                        @Override
                        public void before() throws Throwable {
                            if (observerHelper.findInMap(hashMap, getPackageName(this, 0))) {
                                // logE(tag, "front pkg: " + pkg);
                                setResult(null);
                            }
                        }
                    })

                    .method("lambda$startFullscreenFromFreeform$2$com-android-server-wm-MiuiFreeFormGestureController",
                            "com.android.server.wm.MiuiFreeFormActivityStack")
                    .hook(new IAction() {
                        @Override
                        public void before() throws Throwable {
                            handlerHelper.sendMessageDelayed(handlerHelper.obtainMessage(TOP_WINDOW_HAS_DRAWN, this), 150);
                        }
                    })
            );


            chain("com.android.server.wm.Task", anyConstructor()
                    .hook(new IAction() {
                        @Override
                        public void after() throws Throwable {
                            setThisAdditionalInstanceField("mLastSurfaceVisibility", false);
                        }
                    })

                    .method("prepareSurfaces")
                    .hook(new IAction() {
                        @Override
                        public void after() throws Throwable {
                            String pkg = callThisMethod("getPackageName");
                            if (pkg != null) {
                                Integer state = hashMap.get(pkg);
                                if (state != null && state == 1) {
                                    // logE(tag, "pkg: " + pkg);
                                } else {
                                    return;
                                }
                            }
                            SurfaceControl.Transaction transaction = callThisMethod("getSyncTransaction");
                            SurfaceControl mSurfaceControl = getThisField("mSurfaceControl");
                            // String mCallingPackage = (String) XposedHelpers.getObjectField(param.thisObject, "mCallingPackage");
                            // Object getTopNonFinishingActivity = XposedHelpers.callMethod(param.thisObject, "getTopNonFinishingActivity");
                            // String pkg = null;
                            // if (getTopNonFinishingActivity != null) {
                            //     ActivityInfo activityInfo = (ActivityInfo) XposedHelpers.getObjectField(getTopNonFinishingActivity, "info");
                            //     if (activityInfo != null) {
                            //         pkg = activityInfo.applicationInfo.packageName;
                            //     }
                            // }
                            int taskId = callThisMethod("getRootTaskId");
                            Object mWmService = getThisField("mWmService");
                            Object mAtmService = getField(mWmService, "mAtmService");
                            Object mMiuiFreeFormManagerService = getField(mAtmService, "mMiuiFreeFormManagerService");
                            Object mffs = callMethod(mMiuiFreeFormManagerService, "getMiuiFreeFormActivityStack", taskId);
                            boolean isVisible = callThisMethod("isVisible");
                            boolean isAnimating = callThisMethod("isAnimating", 7);
                            boolean inPinMode = false;
                            if (mffs != null) {
                                inPinMode = callMethod(mffs, "inPinMode");
                            }
                            boolean mLastSurfaceVisibility = getThisAdditionalInstanceField("mLastSurfaceVisibility");
                            if (mSurfaceControl != null && mffs != null && inPinMode) {
                                if (!isAnimating) {
                                    callMethod(transaction, "setVisibility", new Object[]{mSurfaceControl, false});
                                    setThisAdditionalInstanceField("mLastSurfaceVisibility", false);
                                }
                                // logE(tag, "setVisibility false pkg2: " + pkg + " taskid: " + taskId + " isVisble: " + isVisible
                                // + " an: " + isAnimating + " la: " + mLastSurfaceVisibility);
                            } else if (mSurfaceControl != null && mffs != null && !inPinMode) {
                                if (!mLastSurfaceVisibility) {
                                    callMethod(transaction, "setVisibility", new Object[]{mSurfaceControl, true});
                                    setThisAdditionalInstanceField("mLastSurfaceVisibility", true);
                                }
                                // logE(tag, "setVisibility true pkg2: " + pkg + " taskid: " + taskId + " isVisble: " + isVisible + " an: " + isAnimating);
                            }
                            // logE(tag, "sur: " + mSurfaceControl + " tra: " + transaction + " pkg: " + pkg + " inpin: " + inPinMode);
                        }
                    })
            );
        }
    }

    private void removeHandler() {
        mHandler.removeMessages(WILL_HANGUP);
        mHandler.removeMessages(CANCEL_HANGUP);
        mHandler.removeMessages(LOW_TIME_HANGUP);
    }

    private String getPackageName(IAction iAction, int value) {
        Object mffas = iAction.getParam(value);
        if (mffas != null) {
            return callMethod(mffas, "getStackPackageName");
        }
        return null;
    }

    private static class HandlerHelper extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case TOP_WINDOW_HAS_DRAWN -> {
                    IAction iAction = (IAction) msg.obj;
                    Object mffas = iAction.first();
                    Object mLock = iAction.getThisField("mLock");
                    sCore.setField(mffas, "topWindowHasDrawn", true);
                    try {
                        if (mLock == null) {
                            Object mMiuiFreeformPinManagerService = iAction.getThisField("mMiuiFreeformPinManagerService");
                            mLock = sCore.getField(mMiuiFreeformPinManagerService, "mLock");
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
