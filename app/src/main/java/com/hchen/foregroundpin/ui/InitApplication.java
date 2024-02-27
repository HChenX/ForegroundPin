package com.hchen.foregroundpin.ui;

import android.app.Application;

import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.style.IOSStyle;

public class InitApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DialogX.init(this);
        DialogX.globalStyle = new IOSStyle();
        // DialogX.autoShowInputKeyboard = true;
        // DialogX.cancelable = false;
        // DialogX.useHaptic = true;
        DialogX.autoRunOnUIThread = false;
    }
}
