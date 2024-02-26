package com.hchen.foregroundpin.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hchen.foregroundpin.R;
import com.hchen.foregroundpin.ui.base.AppPicker;
import com.hchen.foregroundpin.utils.ToastHelper;
import com.hchen.foregroundpin.utils.settings.SettingsHelper;
import com.hchen.foregroundpin.utils.shell.ShellInit;

public class MainActivity extends AppCompatActivity {
    private String keyword = null;
    private Handler handler;
    private AppPicker appPicker;

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
                        SettingsHelper.init(this);
                    } else {
                        ToastHelper.makeText(this, "获取必要权限失败！模块可能无法正常工作！");
                    }
                })
        );
        appPicker = new AppPicker(this);
        appPicker.search(this, keyword);
        initEditView(this);
    }

    /*@Override
    public void onBackPressed() {
        if (keyword == null || keyword.isEmpty()) {
            super.onBackPressed();
        } else {
            keyword = "";
            EditText editText = findViewById(R.id.search_txt);
            editText.setText(keyword);
            appPicker.search(this,keyword);
        }
    }*/

    public void initEditView(Context context) {
        EditText editText = findViewById(R.id.search_txt);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable input) {
                keyword = input.toString();
                appPicker.search(context, keyword);
            }
        });
    }

    @Override
    protected void onDestroy() {
        ShellInit.destroy();
        super.onDestroy();
    }
}
