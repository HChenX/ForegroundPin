package com.hchen.foregroundpin.pinHook;

import android.util.Log;

import com.hchen.foregroundpin.hookMode.Hook;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class ForegroundPin extends Hook {
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
        }
    }
}
