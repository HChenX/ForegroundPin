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
package com.hchen.foregroundpin.utils.settings;

import com.hchen.foregroundpin.mode.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SettingsData {
    private static final String TAG = "SettingsData";
    private final String mPackage;

    public SettingsData(String pkg) {
        mPackage = pkg;
    }

    public JSONObject toJSON() {
        JSONObject object = new JSONObject();
        try {
            object.put("pkg", mPackage);
        } catch (JSONException e) {
            Log.logSE(TAG, "toJSON", e);
        }
        return object;
    }

    public static String getPkg(JSONObject jsonObject) {
        try {
            return jsonObject.getString("pkg");
        } catch (JSONException e) {
            Log.logSE(TAG, "getPkg", e);
        }
        return "";
    }

    public static JSONObject setPkg(JSONObject jsonObject, String pkg) {
        try {
            jsonObject.put("pkg", pkg);
        } catch (JSONException e) {
            Log.logSE(TAG, "getPkg", e);
        }
        return jsonObject;
    }

    public static JSONObject restore(String json) {
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            Log.logSE(TAG, "restore: " + e);
        }
        return new JSONObject();
    }

    public static String toString(String s) {
        ArrayList<JSONObject> jsonObjects = new ArrayList<>();
        JSONObject object = new SettingsData(s).toJSON();
        jsonObjects.add(object);
        return jsonObjects.toString();
    }

    public static ArrayList<JSONObject> toArray(String json) {
        try {
            ArrayList<JSONObject> arrayList = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                arrayList.add(object);
            }
            return arrayList;
        } catch (JSONException e) {
            Log.logSE(TAG, "toArray: " + e);
        }
        return new ArrayList<>();
    }
}
