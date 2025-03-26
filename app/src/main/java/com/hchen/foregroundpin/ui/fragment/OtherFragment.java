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
package com.hchen.foregroundpin.ui.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.hchen.hooktool.tool.additional.SystemPropTool.getProp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.hchen.foregroundpin.BuildConfig;
import com.hchen.foregroundpin.R;
import com.hchen.himiuix.DialogInterface;
import com.hchen.himiuix.MiuiAlertDialog;
import com.hchen.himiuix.MiuiPreference;
import com.hchen.himiuix.MiuiXUtils;

public class OtherFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.other_pref, rootKey);
        initPreference();
    }

    private void initPreference() {
        MiuiPreference module = findPreference("module");
        module.setLayoutViewBindListener(new MiuiPreference.OnBindViewListener() {
            @Override
            public void onBindView(View view) {
                TextView textView = view.findViewById(R.id.module);
                textView.setText(BuildConfig.VERSION_NAME + "_" + BuildConfig.VERSION_CODE + " | " + BuildConfig.BUILD_TYPE);
            }
        });

        MiuiPreference info = findPreference("info");

        String deviceName = getProp("persist.sys.device_name");
        String marketName = getProp("ro.product.marketname");
        String androidVersion = getProp("ro.build.version.release");
        String osVersion = getProp("ro.mi.os.version.incremental").isEmpty() ?
            getProp("ro.system.build.version.incremental") :
            getProp("ro.mi.os.version.incremental");
        if (deviceName.isEmpty()) deviceName = marketName;

        info.setTitle(deviceName);
        info.setSummary("设备型号: " + marketName + "\n安卓版本: Android " + androidVersion + "\n系统版本: " + osVersion);

        MiuiPreference contributor = findPreference("contributor");
        contributor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                Toast.makeText(getContext(), "期待你的贡献~", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        MiuiPreference open = findPreference("open_source");
        open.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                new MiuiAlertDialog(getContext())
                    .setTitle("开源项目引用")
                    .setEnableCustomView(true)
                    .setCustomView(R.layout.open_source_layout,
                        new DialogInterface.OnBindView() {
                            @Override
                            public void onBindView(ViewGroup root, View view) {
                                CardInfo.builder(view.findViewById(R.id.miuix))
                                    .setTitle("HiMiuiX")
                                    .setSummary("仿 MiuiX 的 Xml 式布局！")
                                    .setArrowRight()
                                    .setClickListen(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent();
                                            intent.setAction("android.intent.action.VIEW");
                                            intent.setData(Uri.parse("https://github.com/HChenX/HiMiuiX"));
                                            startActivity(intent);
                                        }
                                    });

                                CardInfo.builder(view.findViewById(R.id.hooktool))
                                    .setTitle("HookTool")
                                    .setSummary("使用 Java 编写的 Hook 工具！帮助你减轻编写 Hook 代码的复杂度！")
                                    .setArrowRight()
                                    .setClickListen(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent();
                                            intent.setAction("android.intent.action.VIEW");
                                            intent.setData(Uri.parse("https://github.com/HChenX/HookTool"));
                                            startActivity(intent);
                                        }
                                    });
                            }
                        })
                    .setHapticFeedbackEnabled(true)
                    .setPositiveButton("知道了", null)
                    .show();

                return true;
            }
        });

        MiuiPreference thanks = findPreference("thanks");
        thanks.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                new MiuiAlertDialog(getContext())
                    .setTitle("特别鸣谢")
                    .setEnableCustomView(true)
                    .setCustomView(R.layout.thanks_layout,
                        new DialogInterface.OnBindView() {
                            @Override
                            public void onBindView(ViewGroup root, View view) {
                                CardInfo.builder(view.findViewById(R.id.zjw2017))
                                    .setTitle("柚稚的孩纸")
                                    .setSummary("@zjw2017")
                                    .setArrowRight()
                                    .setIcon(
                                        MiuiXUtils.RoundedDrawable.fromBitmap(
                                            MiuiXUtils.drawableToBitmap(MiuiXUtils.getDrawable(getContext(), R.drawable.ic_youzhi)),
                                            MiuiXUtils.dp2px(
                                                getContext(),
                                                10
                                            )
                                        )
                                    )
                                    .setClickListen(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent();
                                            intent.setAction("android.intent.action.VIEW");
                                            intent.setData(Uri.parse("https://github.com/zjw2017"));
                                            startActivity(intent);
                                        }
                                    });

                                CardInfo.builder(view.findViewById(R.id.yife))
                                    .setTitle("YifePlayte")
                                    .setSummary("@YifePlayte")
                                    .setArrowRight()
                                    .setIcon(
                                        MiuiXUtils.RoundedDrawable.fromBitmap(
                                            MiuiXUtils.drawableToBitmap(MiuiXUtils.getDrawable(getContext(), R.drawable.ic_yife)),
                                            MiuiXUtils.dp2px(
                                                getContext(),
                                                10
                                            )
                                        )
                                    )
                                    .setClickListen(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent();
                                            intent.setAction("android.intent.action.VIEW");
                                            intent.setData(Uri.parse("https://github.com/YifePlayte"));
                                            startActivity(intent);
                                        }
                                    });
                            }
                        })
                    .setHapticFeedbackEnabled(true)
                    .setPositiveButton("知道了", null)
                    .show();
                return true;
            }
        });
    }

    private static class CardInfo {
        private final View mView;
        private final ImageView mIcon;
        private final TextView mTitle;
        private final TextView mSummary;
        private final TextView mTip;
        private final ImageView mArrowRight;

        private CardInfo(View view) {
            mView = view;
            mView.setPadding(
                MiuiXUtils.dp2px(mView.getContext(), 2),
                mView.getPaddingTop(),
                MiuiXUtils.dp2px(mView.getContext(), 2),
                mView.getPaddingBottom()
            );

            mIcon = mView.findViewById(com.hchen.himiuix.R.id.pref_icon);
            mTitle = mView.findViewById(com.hchen.himiuix.R.id.pref_title);
            mSummary = mView.findViewById(com.hchen.himiuix.R.id.pref_summary);
            mTip = mView.findViewById(com.hchen.himiuix.R.id.pref_tip);
            mArrowRight = mView.findViewById(com.hchen.himiuix.R.id.pref_arrow_right);

            mIcon.setVisibility(GONE);
            mTitle.setVisibility(GONE);
            mSummary.setVisibility(GONE);
            mTip.setVisibility(GONE);
            mArrowRight.setVisibility(GONE);
        }

        public static CardInfo builder(View view) {
            return new CardInfo(view);
        }

        public CardInfo setIcon(int resId) {
            mIcon.setVisibility(VISIBLE);
            mIcon.setImageResource(resId);
            return this;
        }

        public CardInfo setIcon(Drawable drawable) {
            mIcon.setVisibility(VISIBLE);
            mIcon.setImageDrawable(drawable);
            return this;
        }

        public CardInfo setTitle(String title) {
            mTitle.setVisibility(VISIBLE);
            mTitle.setText(title);
            return this;
        }

        public CardInfo setSummary(String summary) {
            mSummary.setVisibility(VISIBLE);
            mSummary.setText(summary);
            return this;
        }

        public CardInfo setTip(String tip) {
            mTip.setVisibility(VISIBLE);
            mTip.setText(tip);
            return this;
        }

        public CardInfo setArrowRight() {
            mArrowRight.setVisibility(VISIBLE);
            return this;
        }

        public CardInfo setClickListen(View.OnClickListener listener) {
            mView.setOnClickListener(listener);
            return this;
        }
    }
}
