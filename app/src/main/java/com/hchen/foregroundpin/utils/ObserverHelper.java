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
package com.hchen.foregroundpin.utils;


import static com.hchen.hooktool.log.XposedLog.logE;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;

import androidx.annotation.Nullable;

import com.hchen.foregroundpin.utils.settings.SettingsData;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ObserverHelper {
    private String TAG;

    public void setObserver(Context mContext, HashMap<String, Integer> hashMap, HashMap<String, Integer> hangupMap) {
        setHashMap(mContext, hashMap);
        hangupMap.clear();
        ContentObserver contentObserver = new ContentObserver(new Handler(mContext.getMainLooper())) {
            @Override
            public void onChange(boolean selfChange, @Nullable Uri uri, int flags) {
                setHashMap(mContext, hashMap);
                hangupMap.clear();
            }
        };
        mContext.getContentResolver().registerContentObserver(
                Settings.Secure.getUriFor("foreground_pin_param"),
                false, contentObserver);
    }

    public boolean findInMap(HashMap<String, Integer> map, String pkg) {
        if (pkg != null) {
            Integer result = map.get(pkg);
            return result != null && result == 1;
        }
        return false;
    }

    public void setHashMap(Context mContext, HashMap<String, Integer> hashMap) {
        hashMap.clear();
        String data = getPin(mContext);
        if (data == null) return;
        ArrayList<JSONObject> jsonObjects = SettingsData.toArray(data);
        for (JSONObject object : jsonObjects) {
            String pkg = SettingsData.getPkg(object);
            // logE(tag, "add pkg: " + pkg);
            hashMap.put(pkg, 1);
        }
    }

    public String getPin(Context context) {
        String string = Settings.Secure.getString(context.getContentResolver(), "foreground_pin_param");
        if (string == null) {
            logE(TAG, "Get Settings is null!!");
        }
        return string;
    }

    public void setTAG(String tag) {
        TAG = tag;
    }
}
