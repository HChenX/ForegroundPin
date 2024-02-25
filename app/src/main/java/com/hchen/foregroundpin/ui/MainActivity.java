package com.hchen.foregroundpin.ui;

import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hchen.foregroundpin.R;
import com.hchen.foregroundpin.callback.IThreadWrite;
import com.hchen.foregroundpin.ui.base.AppPicker;
import com.hchen.foregroundpin.utils.SettingsHelper;
import com.hchen.foregroundpin.utils.shell.ShellInit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private String keyword = null;
    private Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ShellInit.init();
        handler = new Handler(getMainLooper());
        SettingsHelper.threadWrite(new IThreadWrite() {
            @Override
            public void thread() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ShellInit.getShell().run("settings put system foreground_pin_param \"[]\"").sync();
                    }
                });
            }
        });
        new AppPicker(this).search(this, keyword);
    }

    @Override
    protected void onDestroy() {
        ShellInit.destroy();
        super.onDestroy();
    }
}
