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
package com.hchen.foregroundpin.ui;

import static com.hchen.foregroundpin.utils.settings.SettingsHelper.dip2px;

import android.app.Application;

import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.style.IOSStyle;

public class InitApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DialogX.init(this);
        DialogX.globalStyle = new IOSStyle();
        DialogX.autoShowInputKeyboard = true;
        // DialogX.cancelable = false;
        DialogX.useHaptic = true;
        DialogX.touchSlideTriggerThreshold = dip2px(250);
        // DialogX.autoRunOnUIThread = false;
    }
}
