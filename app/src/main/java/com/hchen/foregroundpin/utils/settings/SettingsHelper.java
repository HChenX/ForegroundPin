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

import static com.hchen.hooktool.log.AndroidLog.logE;

import android.content.Context;
import android.content.res.Resources;
import android.provider.Settings;

import com.hchen.foregroundpin.callback.IThreadWrite;
import com.hchen.foregroundpin.utils.shell.ShellExec;
import com.hchen.foregroundpin.utils.shell.ShellInit;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsHelper {
    private final static String TAG = "SettingsHelper";

    public static boolean init(Context context) {
        String data = getPin(context);
        if (data == null || "".equals(data) || "[]".equals(data)) {
            return setPin(context, "[]");
        }
        return false;
    }

    public static void write(String command) {
        ShellExec shellExec = ShellInit.getShell();
        shellExec.run(command).sync();
    }

    public static void thread(IThreadWrite threadWrite) {
        ExecutorService executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors());
        executorService.submit(threadWrite::thread);
    }

    public static int dip2px(float dpValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static boolean addData(Context context, String pkg) {
        if (context == null || pkg == null) {
            logE(TAG, "Context or pkg can't is null!! Can't add anything!!");
            return false;
        }
        String data = getPin(context);
        if (data == null) {
            logE(TAG, "Data is null!!");
            return false;
        }
        if ("".equals(data) || "[]".equals(data)) {
            data = SettingsData.toString(pkg);
            return setPin(context, data);
        }
        ArrayList<JSONObject> object = SettingsData.toArray(data);
        object.add(new SettingsData(pkg).toJSON());
        return setPin(context, object.toString());
    }

    public static boolean removeData(Context context, String pkg) {
        if (context == null || pkg == null || pkg.equals("")) {
            logE(TAG, "Context or pkg can't is null!! Can't remove anything!!");
            return false;
        }
        String data = getPin(context);
        if (data == null || "".equals(data) || "[]".equals(data)) {
            logE(TAG, "No any data can remove!!");
            return false;
        }
        ArrayList<JSONObject> jsonObjects = SettingsData.toArray(data);
        int count = -1;
        for (JSONObject object : jsonObjects) {
            count = count + 1;
            String mPkg = SettingsData.getPkg(object);
            if (mPkg.equals(pkg)) {
                break;
            }
        }
        jsonObjects.remove(count);
        return setPin(context, jsonObjects.toString());
    }

    public static boolean setPin(Context context, String value) {
        boolean result = false;
        try {
            result = Settings.Secure.putString(context.getContentResolver(), "foreground_pin_param", value);
        } catch (Throwable e) {

        }
        if (!result) logE(TAG, "Put Settings E!!");
        return result;
    }

    public static String getPin(Context context) {
        String string = Settings.Secure.getString(context.getContentResolver(), "foreground_pin_param");
        if (string == null) {
            logE(TAG, "Get Settings is null!!");
            setPin(context, "[]");
            string = Settings.Secure.getString(context.getContentResolver(), "foreground_pin_param");
            if (string == null) {
                string = "[]";
            }
        }
        return string;
    }
}
