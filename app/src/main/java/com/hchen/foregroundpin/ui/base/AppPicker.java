package com.hchen.foregroundpin.ui.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import com.hchen.foregroundpin.R;
import com.hchen.foregroundpin.callback.IAppListView;
import com.hchen.foregroundpin.utils.ToastHelper;
import com.hchen.foregroundpin.utils.settings.SettingsData;
import com.hchen.foregroundpin.utils.settings.SettingsHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppPicker implements IAppListView {
    private final ListView listView;
    private final AppDataAdapter appDataAdapter;
    private final Handler handler;
    private final List<AppData> appDataList = new ArrayList<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final HashMap<String, Integer> hashMap = new HashMap<>();

    public AppPicker(Activity activity) {
        handler = new Handler(activity.getMainLooper());
        hashMap.clear();
        listView = activity.findViewById(R.id.app_list);
        appDataAdapter = new AppDataAdapter(activity, R.layout.user_app, appDataList);
        listView.setAdapter(appDataAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AppData appData = appDataList.get((int) id);
                CheckBox checkBox = view.findViewById(R.id.check_box);
                if (checkBox.isChecked()) {
                    boolean result = SettingsHelper.removeData(activity.getApplicationContext(), appData.packageName);
                    if (result) {
                        // appDataAdapter.initData(null, appData.packageName);
                        appDataAdapter.hashMap.remove(appData.packageName);
                        checkBox.setChecked(false);
                    } else
                        ToastHelper.makeText(activity.getApplicationContext(), "关闭: " + appData.label + " 失败！");
                } else {
                    boolean result = SettingsHelper.addData(activity.getApplicationContext(), appData.packageName);
                    if (!result) {
                        ToastHelper.makeText(activity.getApplicationContext(), "开启: " + appData.label + " 失败！");
                    } else {
                        // appDataAdapter.initData(new SettingsData(appData.packageName).toJSON(), null);
                        appDataAdapter.hashMap.put(appData.packageName, "on");
                        checkBox.setChecked(true);
                    }
                }
            }
        });
    }

    @Override
    public void search(Context context, String keyword) {
        listView.setVisibility(View.VISIBLE);
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                List<AppData> appData = listByKeyword(context, keyword);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (appDataList) {
                            appDataList.clear();
                            appDataAdapter.clear();
                            for (AppData ap : appData) {
                                appDataList.add(ap);
                                appDataAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
            }
        });
    }

    private List<AppData> listByKeyword(Context context, String keyword) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, PackageManager.GET_ACTIVITIES);
        // List<PackageInfo> installedPackages = packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES);
        String str = Optional.ofNullable(keyword).orElse("");
        List<AppData> list = new ArrayList<>();
        for (ResolveInfo pk : resolveInfos) {
            AppData appData = new AppData();
            appData.icon = pk.activityInfo.applicationInfo.loadIcon(packageManager);
            appData.label = pk.activityInfo.applicationInfo.loadLabel(packageManager).toString();
            appData.packageName = pk.activityInfo.applicationInfo.packageName;
            Integer added = hashMap.get(appData.packageName);
            if (added == null || added != 1) {
                hashMap.put(appData.packageName, 1);
            } else {
                continue;
            }
            if (appData.label.toLowerCase().contains(str.toLowerCase())) {
                list.add(appData);
            }
        }
        return list;
    }

    @Override
    public void hide() {
        listView.setVisibility(View.GONE);
    }
}
