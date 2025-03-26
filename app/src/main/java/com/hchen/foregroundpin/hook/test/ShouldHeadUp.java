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
import com.hchen.hooktool.hook.IHook;

/**
 * 手机管家的气泡通知
 *
 * @deprecated
 */
@Deprecated
public class ShouldHeadUp extends BaseHC {
    @Override
    public void init() {
         hookMethod("com.miui.bubbles.services.BubblesNotificationListenerService",
                "onNotificationPosted",
                "android.service.notification.StatusBarNotification",
                "android.service.notification.NotificationListenerService$RankingMap",
                new IHook() {
                    @Override
                    public void before() {
                    }
                }
        );

        hookMethod("com.miui.bubbles.utils.BubbleUpManager",
                "shouldHeadUp",
                "android.service.notification.StatusBarNotification",
                "android.service.notification.NotificationListenerService$RankingMap",
                new IHook() {
                    @Override
                    public void before() {
                    }
                }
        );

        hookMethod("com.miui.bubbles.settings.BubblesSettings",
                "isSbnBelongToActiveBubbleApp",
                String.class, int.class,
                new IHook() {
                    @Override
                    public void before() {
                    }
                }
        );
    }
}
