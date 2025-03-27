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
package com.hchen.foregroundpin.utils;

import com.hchen.hooktool.tool.additional.PrefsTool;

import java.util.HashSet;

/**
 * 模块数据
 *
 * @author 焕晨HChen
 */
public class ModuleData {
    private static final String KEY_FOREGROUND_PIN = "foreground_pin_app_list";
    private static final String KEY_MODULE_ENABLE = "foreground_pin_main_switch";
    private static final String KEY_FOREGROUND_PIN_ENABLE = "foreground_pin_switch";
    private static final String KEY_HANGUP_ENABLE = "hangup_switch";
    private static final String KEY_HANGUP_API_ENABLE = "hangup_api_switch";

    public static boolean isModuleEnable() {
        return PrefsTool.prefs().getBoolean(KEY_MODULE_ENABLE, true);
    }

    public static boolean isForegroundPinEnable() {
        return PrefsTool.prefs().getBoolean(KEY_FOREGROUND_PIN_ENABLE, true);
    }

    public static boolean isHangupEnable() {
        return PrefsTool.prefs().getBoolean(KEY_HANGUP_ENABLE, true);
    }

    public static boolean isHangupApiEnable() {
        return PrefsTool.prefs().getBoolean(KEY_HANGUP_API_ENABLE, true);
    }

    public static boolean shouldForegroundPin(String packageName) {
        if (packageName == null || packageName.isEmpty()) return false;
        return getForegroundPinAppSet().contains(packageName);
    }

    private static HashSet<String> getForegroundPinAppSet() {
        return (HashSet<String>) PrefsTool.prefs().getStringSet(KEY_FOREGROUND_PIN, new HashSet<>());
    }
}
