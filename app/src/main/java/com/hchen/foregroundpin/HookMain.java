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
package com.hchen.foregroundpin;

import static com.hchen.hooktool.HCInit.LOG_D;

import com.hchen.foregroundpin.hook.ForegroundPin;
import com.hchen.foregroundpin.hook.HangupApi;
import com.hchen.foregroundpin.hook.HyperHangup;
import com.hchen.foregroundpin.hook.MiuiHangup;
import com.hchen.foregroundpin.utils.ModuleData;
import com.hchen.hooktool.HCEntrance;
import com.hchen.hooktool.HCInit;

import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * Hook 入口
 *
 * @author 焕晨HChen
 */
public class HookMain extends HCEntrance {
    public static String TAG = "mForegroundPin";

    @Override
    public HCInit.BasicData initHC(HCInit.BasicData basicData) {
        return basicData.setModulePackageName(BuildConfig.APPLICATION_ID)
            .setTag(TAG)
            .setLogLevel(LOG_D)
            .setPrefsName("ForegroundPin")
            .initLogExpand(new String[]{
                "com.hchen.foregroundpin.hook"
            });
    }

    @Override
    public String[] ignorePackageNameList() {
        return new String[]{"com.miui.contentcatcher", "com.android.providers.settings", "com.android.server.telecom"};
    }

    @Override
    public void onLoadPackage(LoadPackageParam lpparam) throws Throwable {
        HCInit.initLoadPackageParam(lpparam);
        switch (lpparam.packageName) {
            case "android" -> {
                new ForegroundPin().onLoadPackage();
                new MiuiHangup().onLoadPackage();
            }
            case "com.android.systemui" -> {
                new HyperHangup().onLoadPackage();

                if (ModuleData.isHangupApiEnable())
                    new HangupApi().onApplicationCreate().onLoadPackage();
            }
        }
    }
}
