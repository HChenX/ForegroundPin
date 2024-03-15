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
