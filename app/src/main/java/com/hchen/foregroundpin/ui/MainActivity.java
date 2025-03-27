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

 * Copyright (C) 2023-2025 HChenX
 */
package com.hchen.foregroundpin.ui;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.hchen.foregroundpin.R;
import com.hchen.foregroundpin.ui.fragment.HomeFragment;
import com.hchen.foregroundpin.ui.fragment.OtherFragment;
import com.hchen.himiuix.DialogInterface;
import com.hchen.himiuix.MiuiAlertDialog;
import com.hchen.hooktool.HCState;
import com.hchen.hooktool.log.AndroidLog;
import com.hchen.hooktool.tool.additional.ShellTool;

import java.util.Arrays;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "mForegroundPin";
    public static final HashMap<String, String> mRestartMap = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        EdgeToEdge.enable(this, SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT), SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), new OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            }
        });

        Fragment[] fragments = new Fragment[]{new HomeFragment(), new OtherFragment()};
        ViewPager2 viewPager2 = findViewById(R.id.view_pager2);
        viewPager2.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return fragments[position];
            }

            @Override
            public int getItemCount() {
                return fragments.length;
            }
        });
        viewPager2.setUserInputEnabled(false);

        RadioGroup radioGroup = findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.home) {
                    viewPager2.setCurrentItem(0);
                } else if (checkedId == R.id.other) {
                    viewPager2.setCurrentItem(1);
                }
            }
        });

        initMenu();

        if (!HCState.isEnabled()) {
            new MiuiAlertDialog(this)
                .setTitle("提示")
                .setMessage("请启用模块后使用！")
                .setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.finish();
                    }
                })
                .setHapticFeedbackEnabled(true)
                .setCanceledOnTouchOutside(false)
                .setCancelable(false)
                .show();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void initMenu() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        int mode = this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (mode == Configuration.UI_MODE_NIGHT_YES) {
            toolbar.inflateMenu(R.menu.action_night);
        } else {
            toolbar.inflateMenu(R.menu.action);
        }

        mRestartMap.put("重启系统", "android");
        mRestartMap.put("重启系统界面", "com.android.systemui");
        toolbar.setOnMenuItemClickListener(item -> {
            toolbar.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            if (item.getItemId() == R.id.action_restart) {
                new MiuiAlertDialog(this)
                    .setTitle("重新启动")
                    .setMessage("请选择需要重新启动的作用域")
                    .setHapticFeedbackEnabled(true)
                    .setEnableListSelectView(true)
                    .setUseCheckBoxButtonStyle(true)
                    .setEnableListSpringBack(true)
                    .setItems(new CharSequence[]{"重启系统", "重启系统界面"}, new DialogInterface.OnItemsClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, CharSequence item, int which) {
                            AndroidLog.logI(TAG, "item: " + item + ", which: " + which);
                        }

                        @Override
                        public void onResult(DialogInterface dialog, CharSequence[] items, CharSequence[] selectedItems) {
                            AndroidLog.logI(TAG, "items: " + Arrays.toString(items) + ", selectedItems: " + Arrays.toString(selectedItems));

                            if (selectedItems.length == 0) return;
                            if (selectedItems.length > 1) return;
                            String item = (String) selectedItems[0];
                            String action = mRestartMap.get(item);
                            if (action == null) return;

                            ShellTool.isRootAvailable();
                            ShellTool.builder().isRoot(true).create().cmd(action.equals("android") ? "reboot" : "killall " + action).exec();
                            ShellTool.obtain().close();

                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton("重启", null)
                    .setNegativeButton("取消", null)
                    .setCanceledOnTouchOutside(false)
                    .show();
            }
            return true;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
