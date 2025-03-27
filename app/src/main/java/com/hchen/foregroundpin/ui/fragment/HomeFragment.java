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

import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hchen.foregroundpin.R;
import com.hchen.foregroundpin.ui.MainActivity;
import com.hchen.himiuix.widget.MiuiCheckBox;
import com.hchen.himiuix.widget.MiuiEditText;
import com.hchen.hooktool.data.AppData;
import com.hchen.hooktool.log.AndroidLog;
import com.hchen.hooktool.tool.additional.PackageTool;
import com.hchen.hooktool.tool.additional.PrefsTool;
import com.hchen.hooktool.tool.itool.IPackageInfoGetter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Home
 *
 * @author 焕晨HChen
 */
public class HomeFragment extends Fragment {
    MiuiEditText mMiuiEditText;
    EditText mEditText;
    RecyclerView mAppListView;
    Handler mHandler = new Handler(Looper.getMainLooper());
    private static final String FOREGROUND_KEY = "foreground_pin_app_list";
    private static HashSet<String> mForegroundPinAppSet = new HashSet<>();
    private static List<AppData> mAppDataList = new ArrayList<>();
    private static List<AppData> mAppDataCacheList = new ArrayList<>();
    private static final SparseBooleanArray mAppCheckedArray = new SparseBooleanArray();
    private static final List<String> mCheckedAppList = new ArrayList<>();
    private static boolean isSearchMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_home, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mForegroundPinAppSet = (HashSet<String>) PrefsTool.prefs(getContext()).getStringSet(FOREGROUND_KEY, new HashSet<>());

        mMiuiEditText = view.findViewById(R.id.search_bar);
        mEditText = mMiuiEditText.getEditTextView();
        mAppListView = view.findViewById(R.id.app_list);
        mAppListView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAppListView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mAppListView.setVerticalScrollBarEnabled(false);
        mAppListView.setHorizontalScrollBarEnabled(false);

        mAppListView.setAdapter(new AppListAdapter());
        getAppData();
        initEditView();

        mAppListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (event.getToolType(0) == MotionEvent.TOOL_TYPE_FINGER) {
                    if (action == MotionEvent.ACTION_UP) {
                        mEditText.clearFocus();
                    }
                }
                return false;
            }
        });
    }

    private void initEditView() {
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                searchApp(HomeFragment.this, s.toString());
            }
        });
    }

    private void searchApp(HomeFragment home, String label) {
        AndroidLog.logI(MainActivity.TAG, "label: " + label);

        mAppDataList = mAppDataCacheList;
        if (label.isEmpty()) {
            isSearchMode = false;
            home.mMiuiEditText.setErrorBorderState(false);
            updateForegroundPinAppList(getContext());
            mAppListView.getAdapter().notifyDataSetChanged();
            return;
        }

        isSearchMode = true;
        mAppDataList = mAppDataList.stream().filter(new Predicate<AppData>() {
            @Override
            public boolean test(AppData appData) {
                return appData.label.toLowerCase().contains(label.toLowerCase());
            }
        }).collect(Collectors.toCollection(ArrayList::new));

        home.mMiuiEditText.setErrorBorderState(mAppDataList.isEmpty());
        updateForegroundPinAppList(getContext());
        mAppListView.getAdapter().notifyDataSetChanged();
    }


    private void getAppData() {
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                mAppDataList.addAll(Arrays.asList(PackageTool.getPackagesByCode(getContext(), new IPackageInfoGetter() {
                    @Override
                    public Parcelable[] packageInfoGetter(PackageManager pm) throws PackageManager.NameNotFoundException {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);

                        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, PackageManager.GET_ACTIVITIES);
                        return resolveInfos.toArray(new Parcelable[0]);
                    }
                })));

                HashSet<String> packageNameSet = new HashSet<>();
                mAppDataList = mAppDataList.stream().filter(new Predicate<AppData>() {
                    @Override
                    public boolean test(AppData appData) {
                        if (packageNameSet.contains(appData.packageName))
                            return false;
                        else {
                            packageNameSet.add(appData.packageName);
                            return true;
                        }
                    }
                }).collect(Collectors.toCollection(ArrayList::new));
                mAppDataCacheList = mAppDataList;

                mHandler.post(new Runnable() {
                    @Override
                    @SuppressLint("NotifyDataSetChanged")
                    public void run() {
                        updateForegroundPinAppList(getContext());

                        mAppListView.getAdapter().notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private static void updateForegroundPinAppList(Context context) {
        updatePrefForegroundPinAppSet(context);
        if (mForegroundPinAppSet.isEmpty()) return;

        mAppCheckedArray.clear();
        for (int i = 0; i < mAppDataList.size(); i++) {
            if (!mForegroundPinAppSet.contains(mAppDataList.get(i).packageName))
                continue;

            mAppCheckedArray.put(i, true);
        }
    }

    private static void updatePrefForegroundPinAppSet(Context context) {
        mForegroundPinAppSet = (HashSet<String>) PrefsTool.prefs(context).getStringSet(FOREGROUND_KEY, new HashSet<>());
    }

    private static void updateCheckedAppList(Context context) {
        if (isSearchMode) {
            for (int i = 0; i < mAppDataList.size(); i++) {
                boolean isChecked = mAppCheckedArray.get(i);
                if (isChecked) {
                    if (!mCheckedAppList.contains(mAppDataList.get(i).packageName)) {
                        mCheckedAppList.add(mAppDataList.get(i).packageName);
                    }
                } else {
                    mCheckedAppList.remove(mAppDataList.get(i).packageName);
                }
            }
        } else {
            mCheckedAppList.clear();

            for (int i = 0; i < mAppDataList.size(); i++) {
                boolean isChecked = mAppCheckedArray.get(i);
                if (isChecked) {
                    mCheckedAppList.add(mAppDataList.get(i).packageName);
                }
            }
        }

        PrefsTool.prefs(context).editor().putStringSet(FOREGROUND_KEY, new HashSet<>(mCheckedAppList)).apply();

        AndroidLog.logI(MainActivity.TAG, "Checked app: " + mCheckedAppList);
    }


    private static class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppViewHolder> {

        @NonNull
        @Override
        public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new AppViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.app_item, parent, false));
        }

        @Override
        public int getItemCount() {
            return mAppDataList.size();
        }

        @Override
        @SuppressLint("ClickableViewAccessibility")
        public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
            holder.mMain.setOnTouchListener(null);
            holder.mIcon.setOnTouchListener(null);
            holder.mName.setOnTouchListener(null);
            holder.mMiuiCheckBox.setOnCheckedChangeListener(null);

            AppData appData = mAppDataList.get(position);
            holder.mIcon.setImageBitmap(appData.icon);
            holder.mName.setText(appData.label);

            boolean isChecked = mAppCheckedArray.get(position);
            holder.mMiuiCheckBox.setChecked(isChecked);

            holder.mMain.setOnTouchListener((v, event) -> holder.mMiuiCheckBox.onTouchEvent(event));
            holder.mIcon.setOnTouchListener((v, event) -> holder.mMiuiCheckBox.onTouchEvent(event));
            holder.mName.setOnTouchListener((v, event) -> holder.mMiuiCheckBox.onTouchEvent(event));
            holder.mMiuiCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mAppCheckedArray.put(holder.getAbsoluteAdapterPosition(), isChecked);
                    holder.mMain.performHapticFeedback(HapticFeedbackConstants.CONFIRM);

                    updateCheckedAppList(holder.mMain.getContext());
                }
            });

            // AndroidLog.logI(MainActivity.TAG, "App Name: " + appData.packageName);
        }

        private static class AppViewHolder extends RecyclerView.ViewHolder {
            View mMain;
            ImageView mIcon;
            TextView mName;
            MiuiCheckBox mMiuiCheckBox;

            public AppViewHolder(@NonNull View itemView) {
                super(itemView);

                mMain = itemView;
                mIcon = itemView.findViewById(com.hchen.himiuix.R.id.list_image);
                mName = itemView.findViewById(com.hchen.himiuix.R.id.list_item);
                mMiuiCheckBox = itemView.findViewById(com.hchen.himiuix.R.id.list_checkbox);

                mIcon.setVisibility(VISIBLE);
            }
        }
    }

}
