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

 * Copyright (C) 2023-2025 HChenX
 */
package com.hchen.foregroundpin.ui;

import com.hchen.foregroundpin.BuildConfig;
import com.hchen.hooktool.HCInit;
import com.hchen.hooktool.tool.additional.PrefsTool;

public class Application extends android.app.Application {
    private static final String TAG = "mForegroundPin";

    @Override
    public void onCreate() {
        super.onCreate();

        HCInit.initBasicData(
            new HCInit.BasicData()
                .setModulePackageName(BuildConfig.APPLICATION_ID)
                .setPrefsName("ForegroundPin")
                .setTag(TAG)
                .setLogLevel(HCInit.LOG_D)
        );
        PrefsTool.prefs(this);
    }
}
