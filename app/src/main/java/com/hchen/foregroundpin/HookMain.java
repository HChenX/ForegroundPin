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
package com.hchen.foregroundpin;

import com.hchen.foregroundpin.hook.ForegroundPin;
import com.hchen.foregroundpin.hook.SystemUiHangup;
import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.HCInit;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class HookMain implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    public static String modulePath;

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) {
        HCInit.setTAG("ForegroundPin");
        switch (lpparam.packageName) {
            case "android" -> {
                HCInit.initLoadPackageParam(lpparam);
                initHook(new ForegroundPin());
            }
            case "com.miui.securitycenter" -> {
                // initHook(new ShouldHeadUp(), lpparam);
            }
            case "com.android.systemui" -> {
                HCInit.initLoadPackageParam(lpparam);
                initHook(new SystemUiHangup());
            }
        }
    }

    public static void initHook(BaseHC hook) {
        hook.onCreate();
    }

    @Override
    public void initZygote(StartupParam startupParam) {
        modulePath = startupParam.modulePath;
    }
}
