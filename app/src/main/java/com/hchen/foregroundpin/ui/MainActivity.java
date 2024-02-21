package com.hchen.foregroundpin.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hchen.foregroundpin.R;
import com.hchen.foregroundpin.ui.base.AppDataAdapter;
import com.hchen.foregroundpin.ui.base.AppPicker;
import com.hchen.foregroundpin.utils.ToastHelper;
import com.hchen.foregroundpin.utils.shell.ShellInit;

public class MainActivity extends AppCompatActivity {
    private String keyword = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ShellInit.init();
        boolean ready = ShellInit.ready();
        if (!ready) {
            ToastHelper.makeText(this, "Don't have Root!");
            finish();
        }
        new AppPicker(this).search(this, keyword);
    }

    @Override
    protected void onDestroy() {
        ShellInit.destroy();
        super.onDestroy();
    }
}
