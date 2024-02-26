package com.hchen.foregroundpin.ui.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hchen.foregroundpin.R;
import com.hchen.foregroundpin.utils.settings.SettingsData;
import com.hchen.foregroundpin.utils.settings.SettingsHelper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AppDataAdapter extends ArrayAdapter<AppData> {
    private final int resource;
    public HashMap<String, String> hashMap = new HashMap<>();
    // private final ExecutorService executorService;
    // private final Handler handler;

    public AppDataAdapter(@NonNull Context context, int resource, @NonNull List<AppData> objects) {
        super(context, resource, objects);
        this.resource = resource;
        String data = SettingsHelper.getPin(context);
        ArrayList<JSONObject> jsonObjects = SettingsData.toArray(data);
        // executorService = Executors.newSingleThreadExecutor();
        // handler = new Handler(context.getMainLooper());
        hashMap.clear();
        for (JSONObject object : jsonObjects) {
            String pkg = SettingsData.getPkg(object);
            hashMap.put(pkg, "on");
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        AppData appData = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(resource, parent, false);
        }
        if (appData == null) return convertView;
        ImageView imageView = convertView.findViewById(R.id.app_icon);
        TextView name = convertView.findViewById(R.id.app_name);
        CheckBox checkBox = convertView.findViewById(R.id.check_box);
        String pkg = appData.packageName;
        if (pkg == null) pkg = "";
        if (hashMap.isEmpty()) {
            checkBox.setChecked(false);
        } else {
            String state = hashMap.get(pkg);
            if (state != null) {
                if (state.equals("on")) {
                    checkBox.setChecked(true);
                }
            } else checkBox.setChecked(false);
        }
        imageView.setImageDrawable(appData.icon);
        name.setText(appData.label);

        return convertView;
    }

    /*private static void refresh(JSONObject object, String pkg) {
        if (object != null) {
            jsonObjects.add(object);
            return;
        }
        int count = -1;
        for (JSONObject jsonObject : jsonObjects) {
            count = count + 1;
            String mPkg = SettingsData.getPkg(jsonObject);
            if (mPkg.equals(pkg)) {
                break;
            }
        }
        jsonObjects.remove(count);
    }

    public void initData(JSONObject object, String pkg) {
        executorService.submit(() -> {
            handler.post(() -> {
                if (pkg != null) hashMap.remove(pkg);
                if (object != null) {
                    hashMap.put(SettingsData.getPkg(object), 1);
                }
                // refresh(object, pkg);
            });
        });
    }*/
}
