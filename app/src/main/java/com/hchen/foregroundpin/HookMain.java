package com.hchen.foregroundpin;

import com.hchen.foregroundpin.hookMode.Hook;
import com.hchen.foregroundpin.pinHook.ForegroundPin;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class HookMain implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) {
        if ("android".equals(lpparam.packageName)) {
            initHook(new ForegroundPin(), lpparam);
        }
    }

    public static void initHook(Hook hook, LoadPackageParam param) {
        hook.runHook(param);
    }

}
