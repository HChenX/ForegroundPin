package com.hchen.foregroundpin.pinHook;

import com.hchen.foregroundpin.hookMode.Hook;

public class ShouldHeadUp extends Hook {
    @Override
    public void init() {
        findAndHookMethod("com.miui.bubbles.services.BubblesNotificationListenerService",
                "onNotificationPosted",
                "android.service.notification.StatusBarNotification",
                "android.service.notification.NotificationListenerService$RankingMap",
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
//                        logE(tag, "kkkk: " + param.args[0] + " 2: " + param.args[1]);
                    }
                }
        );

        findAndHookMethod("com.miui.bubbles.utils.BubbleUpManager",
                "shouldHeadUp",
                "android.service.notification.StatusBarNotification",
                "android.service.notification.NotificationListenerService$RankingMap",
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
//                        logE(tag, "ttt1: " + param.args[0] + " 2: " + param.args[1]);
                    }
                }
        );
        findAndHookMethod("com.miui.bubbles.settings.BubblesSettings",
                "isSbnBelongToActiveBubbleApp",
                String.class, int.class,
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
//                        param.setResult(true);
//                        logE(tag, "ppp1: " + param.args[0] + " 2: " + param.args[1]);
                    }
                }
        );

    }
}
