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

public class ModuleData {
    private static final String KEY_FOREGROUND_PIN = "foreground_pin_app_list";

    public static boolean shouldForegroundPin(String packageName) {
        return getForegroundPinAppSet().contains(packageName);
    }

    private static HashSet<String> getForegroundPinAppSet() {
        return (HashSet<String>) PrefsTool.prefs().getStringSet(KEY_FOREGROUND_PIN, new HashSet<>());
    }
}
