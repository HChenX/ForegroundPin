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

import static com.hchen.foregroundpin.HookMain.modulePath;
import static com.hchen.hooktool.log.XposedLog.logE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ResHelper {
    /**
     * @noinspection JavaReflectionMemberAccess
     */
    public static Resources addModuleRes(Context context) {
        String tag = "addModuleRes";
        try {
            @SuppressLint("DiscouragedPrivateApi")
            Method AssetPath = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
            AssetPath.setAccessible(true);
            AssetPath.invoke(context.getResources().getAssets(), modulePath);
            return context.getResources();
        } catch (NoSuchMethodException e) {
            logE(tag, "Method addAssetPath is null: " + e);
        } catch (InvocationTargetException e) {
            logE(tag, "InvocationTargetException: " + e);
        } catch (IllegalAccessException e) {
            logE(tag, "IllegalAccessException: " + e);
        }
        return null;
    }
}

