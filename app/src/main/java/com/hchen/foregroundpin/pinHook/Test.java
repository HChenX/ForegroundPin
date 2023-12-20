package com.hchen.foregroundpin.pinHook;

import static de.robv.android.xposed.callbacks.XCallback.PRIORITY_LOWEST;

import android.graphics.Rect;

import com.hchen.foregroundpin.hookMode.Hook;

public class Test extends Hook {
    @Override
    public void init() {
        findAndHookMethod("com.android.server.wm.MiuiMultiWindowSwitchManager",
                "dropToFreeForm",
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
                        logE(tag, "dropToFreeForm im run");
                    }
                }
        );

        findAndHookMethod("com.android.server.wm.MiuiFreeformPinManagerService",
                "updatePinFloatingWindowPos",
                "com.android.server.wm.MiuiFreeFormActivityStack",
                Rect.class, boolean.class,
                new HookAction(PRIORITY_LOWEST * 2) {
                    @Override
                    protected void after(MethodHookParam param) throws Throwable {
                        if ((boolean) param.args[2]) {
                            Object mGestureAnimator = getObjectField(
                                    getObjectField(
                                            getObjectField(
                                                    param.thisObject,
                                                    "mController"),
                                            "mGestureListener"),
                                    "mGestureAnimator");
                            /*尽量提升效率*/
                            callMethod(
                                    getObjectField(param.args[0],
                                            "mLastIconLayerWindowToken"),
                                    "setVisibility", false, false);
//                                callMethod(mGestureAnimator, "hideStack", param.args[0]);
//                                callMethod(mGestureAnimator, "applyTransaction");
                        }
                    }
                }
        );

        findAndHookMethod("com.android.server.wm.MiuiFreeformTrackManager",
                "trackSmallWindowPinedMoveEvent",
                String.class, String.class, int.class, int.class,
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
                        logE(tag, "trackSmallWindowPinedMoveEvent: 1: " + param.args[0]
                                + " 2: " + param.args[1] + " 3: " + param.args[2] + " 4: " + param.args[3]);
                        param.setResult(null);
                    }
                }
        );

        findAndHookMethod("com.android.server.wm.MiuiFreeformTrackManager",
                "trackMiniWindowPinedMoveEvent", String.class, String.class, int.class, int.class,
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
                        logE(tag, "trackMiniWindowPinedMoveEvent: 1: " + param.args[0]
                                + " 2: " + param.args[1] + " 3: " + param.args[2] + " 4: " + param.args[3]);
                        param.setResult(null);
                    }
                }
        );

        findAndHookMethod("com.android.server.wm.MiuiFreeFormGestureAnimator",
                "hideStack",
                "com.android.server.wm.MiuiFreeFormActivityStack",
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
                        logE(tag, "hideStack: " + param.args[0]);
                            param.setResult(null);
                    }
                }
        );

        findAndHookMethod("com.android.server.wm.MiuiFreeFormGestureAnimator",
                "hide",
                "com.android.server.wm.WindowContainer",
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
                            logE(tag, "hide: " + param.args[0]);
                    }
                }
        );

        findAndHookMethod("com.android.server.wm.MiuiFreeFormGestureAnimator",
                "showStack",
                "com.android.server.wm.MiuiFreeFormActivityStack",
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
                        logE(tag, "showStack: " + param.args[0]);
                            param.setResult(null);
                    }
                }
        );
    }
}
