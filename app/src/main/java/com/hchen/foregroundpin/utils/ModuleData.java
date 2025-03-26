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
