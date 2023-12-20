package com.hchen.foregroundpin.pinHook;

import android.content.res.Configuration;
import android.graphics.Rect;
import android.util.Log;

import com.hchen.foregroundpin.hookMode.Hook;

import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class ForegroundPin extends Hook {
    public int lastOri = -1;

    public int ori = -1;

    public int size = -1;
    public int num = 0;

    @Override
    public void init() {
        try {
            /*Hyper*/
            getDeclaredMethod("com.android.server.wm.MiuiFreeFormGestureController",
                    "needForegroundPin",
                    "com.android.server.wm.MiuiFreeFormActivityStack");
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
                            logE(tag, "updateScreenParams: ori: " + ori + " la: " + lastOri);
                        }
                    }
            );

            findAndHookMethod("com.android.server.wm.MiuiFreeformPinManagerService",
                    "updatePinFloatingWindowPos",
                    "com.android.server.wm.MiuiFreeFormActivityStack",
                    Rect.class, boolean.class,
                    new HookAction() {
                        @Override
                        protected void before(MethodHookParam param) throws Throwable {
                            if (lastOri != -1 && ori != -1) {
                                if (lastOri != ori) {
                                    Object mMiuiFreeFormManagerService = getObjectField(
                                            getObjectField(param.thisObject,
                                                    "mController"),
                                            "mMiuiFreeFormManagerService");
                                    List<?> stackList = (List<?>) callMethod(mMiuiFreeFormManagerService,
                                            "getAllMiuiFreeFormActivityStack");
                                    Object mGestureAnimator = getObjectField(
                                            getObjectField(
                                                    getObjectField(
                                                            param.thisObject,
                                                            "mController"),
                                                    "mGestureListener"),
                                            "mGestureAnimator");
                                    if (stackList.isEmpty()) return;
                                    /*for (Object stack : stackList) {
                                        if (stack != null) {
                                            if ((boolean) callMethod(stack, "inPinMode")) {
                                                callMethod(mGestureAnimator, "hideStack", stack);
                                                callMethod(mGestureAnimator, "applyTransaction");
                                            }
                                        }
                                    }*/
                                    /*尽量提升效率*/
                                    if ((boolean) callMethod(param.args[0], "inPinMode")) {
                                        callMethod(mGestureAnimator, "hideStack", param.args[0]);
                                        callMethod(mGestureAnimator, "applyTransaction");
                                    }
                                    if (size == -1)
                                        size = stackList.size();
                                    if (size == 1) {
                                        if (num == 0) {
                                            num = 1;
                                        } else if (num == 1) {
                                            lastOri = ori;
                                            num = 0;
                                            size = -1;
                                        }
                                    } else {
                                        if (num == 0) {
                                            num = 1;
                                        } else if (num == 1) {
                                            size = --size;
                                            num = 0;
                                        }
                                    }
                                }
                            }
                            /*logE(tag, "updatePinFloatingWindowPos: 1: " + param.args[0]
                                    + " 2: " + param.args[1] + " 3: " + param.args[2] + " la " + lastOri + " oo: " + ori
                                    + " ss: " + size);*/
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
                        protected void before(XC_MethodHook.MethodHookParam param) throws Throwable {
                            Object mffas = param.args[0];
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
                        protected void before(MethodHookParam param) throws Throwable {
                            Object mffas = param.args[0];
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
}
