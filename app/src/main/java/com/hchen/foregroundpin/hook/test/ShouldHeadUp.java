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
package com.hchen.foregroundpin.hook.test;

import com.hchen.hooktool.BaseHC;

/**
 * 这些是手机管家的气泡通知方法，暂时没有用。
 */
public class ShouldHeadUp extends BaseHC {
    @Override
    public void init() {
        /* findAndHookMethod("com.miui.bubbles.services.BubblesNotificationListenerService",
                "onNotificationPosted",
                "android.service.notification.StatusBarNotification",
                "android.service.notification.NotificationListenerService$RankingMap",
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
                        // logE();
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
                        // logE();
                    }
                }
        );
        findAndHookMethod("com.miui.bubbles.settings.BubblesSettings",
                "isSbnBelongToActiveBubbleApp",
                String.class, int.class,
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
                        // param.setResult(true);
                        // logE();
                    }
                }
        ); */
    }
}
