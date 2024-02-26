package com.hchen.foregroundpin.ui;

import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hchen.foregroundpin.R;
import com.hchen.foregroundpin.callback.IThreadWrite;
import com.hchen.foregroundpin.ui.base.AppPicker;
import com.hchen.foregroundpin.utils.SettingsHelper;
import com.hchen.foregroundpin.utils.shell.ShellInit;

public class MainActivity extends AppCompatActivity {
    private String keyword = null;
    private Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ShellInit.init();
        handler = new Handler(getMainLooper());
        SettingsHelper.threadWrite(() ->
                handler.post(() -> {
                    // 获取权限
                    boolean result = ShellInit.getShell().run("pm grant " + getPackageName()
                            + " android.permission.WRITE_SECURE_SETTINGS").sync().isResult();
                    if (result) {
                        Settings.Secure.putString(getContentResolver(), "foreground_pin_param", "[]");
                    }
                })
        );
        new AppPicker(this).search(this, keyword);
    }

    @Override
    protected void onDestroy() {
        ShellInit.destroy();
        super.onDestroy();
    }
}
