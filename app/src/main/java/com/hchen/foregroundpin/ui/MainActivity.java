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
package com.hchen.foregroundpin.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.hchen.foregroundpin.R;
import com.hchen.foregroundpin.callback.IResult;
import com.hchen.foregroundpin.ui.base.AppPicker;
import com.hchen.foregroundpin.utils.ToastHelper;
import com.hchen.foregroundpin.utils.settings.SettingsHelper;
import com.hchen.foregroundpin.utils.shell.ShellInit;
import com.hchen.hooktool.HCInit;
import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.dialogs.MessageDialog;
import com.kongzue.dialogx.util.TextInfo;

public class MainActivity extends AppCompatActivity implements IResult {
    private String keyword = null;
    private Handler handler;
    private AppPicker appPicker;
    private static final String TAG = "mForegroundPin";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HCInit.initOther(
                "com.hchen.foregroundpin", 
                "mForegroundPin", HCInit.LOG_D);
        setContentView(R.layout.activity_main);
        ShellInit.init(this);
        // handler = new Handler(getMainLooper());
        SettingsHelper.thread(() -> {
                    // 获取权限
                    boolean result = ShellInit.getShell().run("pm grant " + getPackageName()
                            + " android.permission.WRITE_SECURE_SETTINGS").sync().isResult();
                    if (result) {
                        SettingsHelper.init(this);
                    } else {
                        ToastHelper.makeText(this, "获取必要权限失败！模块可能无法正常工作！");
                    }
                }
        );
        appPicker = new AppPicker(this);
        appPicker.search(this, keyword);
        initMenu();
        initEditView(this);
    }

    @Override
    public void error(String reason) {
        MessageDialog.build()
                .setTitle("警告")
                .setCancelable(false)
                .setHapticFeedbackEnabled(true)
                .setMessage("""
                        未给予模块Root权限无法正常使用！
                        不给模块Root用什么用？
                        要不是安卓权限限制我会申请吗？
                        不相信模块就不要用。
                        """)
                .setOkButton("确定", (dialog, v) -> {
                    ShellInit.destroy();
                    finish();
                    return false;
                }).show();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // appPicker.search(this, keyword);
        DialogX.touchSlideTriggerThreshold = SettingsHelper.dip2px(130);
    }

    private void initMenu() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        int mode = this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (mode == Configuration.UI_MODE_NIGHT_YES) {
            toolbar.inflateMenu(R.menu.main_night);
        } else {
            toolbar.inflateMenu(R.menu.main);
        }
        toolbar.setOnMenuItemClickListener(item -> {
            MessageDialog.build()
                    .setTitle("设置")
                    .setTitleTextInfo(new TextInfo().setFontSize(25))
                    .setOkTextInfo(new TextInfo().setFontColor(Color.RED).setFontSize(15))
                    .setOtherTextInfo(new TextInfo().setFontSize(15))
                    .setButtonOrientation(LinearLayout.VERTICAL)
                    .setOnBackPressedListener(dialog -> {
                        dialog.dismiss();
                        return false;
                    })
                    .setOkButton("重启作用域", (dialog, v) -> {
                        dialog.dismiss();
                        MessageDialog.build()
                                .setTitle("注意")
                                .setMessage("请选择你要重启的作用域")
                                .setOkTextInfo(new TextInfo().setFontColor(Color.RED).setFontSize(15))
                                .setOtherTextInfo(new TextInfo().setFontColor(Color.RED).setFontSize(15))
                                .setCancelTextInfo(new TextInfo().setFontSize(15))
                                .setOkButton("系统界面", (messageDialog, view) -> {
                                    ShellInit.getShell().run("killall -15 com.android.systemui");
                                    // ToastHelper.makeText(MainActivity.this, "按下确定");
                                    return false;
                                })
                                .setCancelButton("取消", (messageDialog, view) -> {
                                    ToastHelper.makeText(MainActivity.this, "已经取消重启");
                                    return false;
                                }).setOtherButton("系统", (messageDialog, view) -> {
                                    ShellInit.getShell().run("reboot");
                                    // ToastHelper.makeText(MainActivity.this, "按下确定");
                                    return false;
                                }).show();
                        return false;
                    })
                    .setOtherButton("关于模块", (dialog, v) -> {
                        dialog.dismiss();
                        MessageDialog.build()
                                .setTitle("关于")
                                .setTitleTextInfo(new TextInfo().setFontSize(20))
                                .setMessageTextInfo((new TextInfo().setFontSize(15)))
                                .setCancelable(false)
                                .setHapticFeedbackEnabled(true)
                                .setMessage("""

                                        - 本模块由 焕晨HChen 开发。
                                        - 用于使贴边小窗保持前台！
                                        - 和使小窗实现“息屏听剧”效果！
                                        - 我真的不会写UI，就这样吧~

                                        - 模块开发者：
                                        @焕晨HChen

                                        - 感谢名单：
                                        Hyper Hook 方法来源：
                                        @柚稚的孩纸(@zjw2017)
                                        @YifePlayte
                                        @PedroZ
                                        @焕晨HChen
                                                                                
                                        Miui Hook 方法来源：
                                        @焕晨HChen
                                                                                
                                        模块UI来源：
                                        @myflavor
                                        DialogX项目
                                        """
                                )
                                .setOkButton("确定", null)
                                .show();
                        return false;
                    })
                    .show();

            return true;
        });
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
