package com.hchen.foregroundpin;

import com.hchen.foregroundpin.hookMode.Hook;
import com.hchen.foregroundpin.pinHook.ForegroundPin;
import com.hchen.foregroundpin.pinHook.ShouldHeadUp;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class HookMain implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) {
        switch (lpparam.packageName) {
            case "android" -> {
                initHook(new ForegroundPin(), lpparam);
            }
            case "com.miui.securitycenter" -> {
//                initHook(new ShouldHeadUp(), lpparam);
            }
        }
    }

    public static void initHook(Hook hook, LoadPackageParam param) {
        hook.runHook(param);
    }

}
