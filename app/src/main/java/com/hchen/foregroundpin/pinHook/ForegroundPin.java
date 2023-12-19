package com.hchen.foregroundpin.pinHook;

import android.content.res.Configuration;
import android.graphics.Rect;
import android.util.Log;

import com.hchen.foregroundpin.hookMode.Hook;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class ForegroundPin extends Hook {
    public int lastOri = -1;

    public int ori = -1;

    public int num = 0;

    @Override
    public void init() {
        try {
            getDeclaredMethod("com.android.server.wm.MiuiFreeFormGestureController",
                    "needForegroundPin",
                    "com.android.server.wm.MiuiFreeFormActivityStack");
            /*Hyper*/
            findAndHookMethod("com.android.server.wm.MiuiFreeFormGestureController",
                    "needForegroundPin",
                    "com.android.server.wm.MiuiFreeFormActivityStack",
                    new HookAction() {
                        @Override
                        protected void before(XC_MethodHook.MethodHookParam param) {
                            param.setResult(true);
                        }
                    }
            );
        } catch (Throwable throwable) {
            logE(tag, "Hyper UnForegroundPin E, if you is Miui don't worry : " + throwable);
            /*Miui*/
            findAndHookMethod("com.android.server.wm.MiuiFreeFormGestureController",
                    "moveTaskToBack",
                    "com.android.server.wm.MiuiFreeFormActivityStack",
                    new HookAction() {
                        @Override
                        protected void before(XC_MethodHook.MethodHookParam param) {
                            param.setResult(null);
                        }
                    }
            );

            findAndHookMethod("com.android.server.wm.MiuiFreeFormGesturePointerEventListener",
                    "updateScreenParams",
                    "com.android.server.wm.DisplayContent", Configuration.class,
                    new HookAction() {
                        @Override
                        protected void after(MethodHookParam param) {
                            Configuration configuration = (Configuration) param.args[1];
                            ori = configuration.orientation;
                            if (lastOri == -1) lastOri = ori;
//                            logE(tag, "updateScreenParams: ori: " + ori + " la: " + lastOri);
                        }
                    }
            );

            findAndHookMethod("com.android.server.wm.MiuiFreeformPinManagerService",
                    "updatePinFloatingWindowPos",
                    "com.android.server.wm.MiuiFreeFormActivityStack",
                    Rect.class, boolean.class,
                    new HookAction() {
                        @Override
                        protected void before(MethodHookParam param) {
                            if (lastOri != -1 && ori != -1) {
                                if (lastOri != ori) {
                                    if (num == 0) {
                                        num = 1;
                                    } else if (num == 1) {
                                        lastOri = ori;
                                        num = 0;
                                    }
                                    if ((Boolean) XposedHelpers.callMethod(param.args[0], "inPinMode")) {
                                        Object mGestureAnimator = XposedHelpers.getObjectField(
                                                XposedHelpers.getObjectField(
                                                        XposedHelpers.getObjectField(
                                                                param.thisObject,
                                                                "mController"),
                                                        "mGestureListener"),
                                                "mGestureAnimator");
                                        XposedHelpers.callMethod(mGestureAnimator, "hideStack", param.args[0]);
                                        XposedHelpers.callMethod(mGestureAnimator, "applyTransaction");
//                                        logE(tag, "updatePinFloatingWindowPos: 1: " + param.args[0] + " 2: " + param.args[1] + " 3: " + param.args[2]);
                                    }
                                }
                            }
//                            logE(tag, "updatePinFloatingWindowPos: 1: " + param.args[0] + " 2: " + param.args[1] + " 3: " + param.args[2]);
                        }
                    }
            );

            findAndHookMethod("com.android.server.wm.MiuiFreeFormGestureController"
                    , "moveTaskToFront",
                    "com.android.server.wm.MiuiFreeFormActivityStack",
                    new HookAction() {
                        @Override
                        protected void before(XC_MethodHook.MethodHookParam param) {
                            param.setResult(null);
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
                            Object activityRecord = XposedHelpers.callMethod(XposedHelpers.getObjectField(mffas, "mTask"),
                                    "getTopNonFinishingActivity");
                            /*遵循安卓日志*/
                            Log.i("MiuiFreeformPinManagerService",
                                    "unPinFloatingWindow mffas: " + mffas + " activityRecord: " + activityRecord);
                            if (activityRecord == null) {
                                param.setResult(null);
                                return;
                            }
                            XposedHelpers.setObjectField(mffas, "mEnterVelocityX", param.args[1]);
                            XposedHelpers.setObjectField(mffas, "mEnterVelocityY", param.args[2]);
                            XposedHelpers.setObjectField(mffas, "mIsEnterClick", param.args[3]);
                            XposedHelpers.setObjectField(mffas, "mIsPinFloatingWindowPosInit", false);
                            XposedHelpers.callMethod(param.thisObject, "setUpMiuiFreeWindowFloatIconAnimation",
                                    mffas, activityRecord, param.args[4], param.args[5]);
                            XposedHelpers.callMethod(param.thisObject, "startUnPinAnimation", mffas);
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
                            Object mGestureListener = XposedHelpers.getObjectField(param.thisObject, "mGestureListener");
                            if ((boolean) XposedHelpers.callMethod(mffas, "isInFreeFormMode")) {
                                XposedHelpers.callMethod(mGestureListener, "startFullScreenFromFreeFormAnimation", mffas);
                                Object mTrackManager = XposedHelpers.getObjectField(param.thisObject, "mTrackManager");
                                if (mTrackManager != null) {
                                    XposedHelpers.callMethod(mTrackManager, "trackSmallWindowPinedQuitEvent",
                                            XposedHelpers.callMethod(mffas, "getStackPackageName"),
                                            XposedHelpers.callMethod(mffas, "getApplicationName"),
                                            (long) XposedHelpers.getObjectField(mffas, "mPinedStartTime") != 0 ?
                                                    ((float) (System.currentTimeMillis() -
                                                            (long) XposedHelpers.getObjectField(mffas,
                                                                    "mPinedStartTime"))) / 1000.0f : 0.0f
                                    );
                                }
                            } else if ((boolean) XposedHelpers.callMethod(mffas, "isInMiniFreeFormMode")) {
                                XposedHelpers.callMethod(mGestureListener, "startFullScreenFromSmallAnimation", mffas);
                                Object mTrackManager = XposedHelpers.getObjectField(param.thisObject, "mTrackManager");
                                if (mTrackManager != null) {
                                    XposedHelpers.callMethod(mTrackManager, "trackMiniWindowPinedQuitEvent",
                                            XposedHelpers.callMethod(mffas, "getStackPackageName"),
                                            XposedHelpers.callMethod(mffas, "getApplicationName"),
                                            (long) XposedHelpers.getObjectField(mffas, "mPinedStartTime") != 0 ?
                                                    ((float) (System.currentTimeMillis() -
                                                            (long) XposedHelpers.getObjectField(mffas,
                                                                    "mPinedStartTime"))) / 1000.0f : 0.0f
                                    );
                                }
                            }
                            XposedHelpers.setObjectField(mffas, "mPinedStartTime", 0L);
                            XposedHelpers.callMethod(mffas, "setInPinMode", false);
                            param.setResult(null);
                        }
                    }
            );

        }
    }
}
