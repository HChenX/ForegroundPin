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
package com.hchen.foregroundpin.hook;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Looper;
import android.os.Parcel;

import com.hchen.foregroundpin.utils.HangupHandler;
import com.hchen.foregroundpin.utils.ModuleData;
import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.hook.IHook;
import com.hchen.hooktool.tool.additional.DeviceTool;

public class HyperHangup extends BaseHC {
    private final HangupHandler mHangupHandler = new HangupHandler(Looper.getMainLooper());

    @Override
    public void init() {
        if (!DeviceTool.isMoreHyperOSVersion(1f)) return; // 不是 Hyper 跳过

        hookMethod(DeviceTool.isMoreHyperOSVersion(2f) ? "com.miui.analytics.MiuiMultiWinTrackUtils" : "com.android.wm.shell.miuimultiwinswitch.MiuiMultiWinTrackUtils",
            "trackWindowControlButtonClick",
            Context.class, ActivityManager.RunningTaskInfo.class, String.class, String.class,
            new IHook() {
                @Override
                public void before() {
                    if (!ModuleData.isModuleEnable() || !ModuleData.isHangupEnable()) return;

                    String str = (String) getArgs(2);
                    String str1 = (String) getArgs(3);
                    if ("小窗".equals(str) && "小窗".equals(str1)) {
                        Context context = (Context) getArgs(0);
                        ActivityManager.RunningTaskInfo runningTaskInfo = (ActivityManager.RunningTaskInfo) getArgs(1);
                        if (context == null || runningTaskInfo == null) return;

                        String packageName = (String) callStaticMethod(
                            DeviceTool.isMoreHyperOSVersion(2f) ?
                                "com.android.wm.shell.multitasking.utils.MultiTaskingPackageUtils" : "com.android.wm.shell.miuimultiwinswitch.MiuiMultiWinUtils",
                            DeviceTool.isMoreHyperOSVersion(2f) ?
                                "getRunningTaskPackageName" : "getPackageName",
                            runningTaskInfo);

                        if (ModuleData.shouldForegroundPin(packageName)) {
                            removeHandler();
                            if (!HangupHandler.mHangupSet.contains(packageName))
                                mHangupHandler.handleMessage(mHangupHandler.obtainMessage(HangupHandler.HANGUP_READY, new Object[]{context, packageName}));
                        }
                    }
                }
            }
        );

        hookMethod(DeviceTool.isMoreHyperOSVersion(2f) ?
                "com.android.wm.shell.multitasking.miuifreeform.MiuiFreeformModePinHandler" : "com.android.wm.shell.miuifreeform.MiuiFreeformModePinHandler",
            "hideTask",
            DeviceTool.isMoreHyperOSVersion(2f) ?
                "com.android.wm.shell.multitasking.taskmanager.MiuiFreeformModeTaskInfo" : "com.android.wm.shell.miuifreeform.MiuiFreeformModeTaskInfo",
            new IHook() {
                @Override
                public void after() {
                    if (!ModuleData.isModuleEnable() || !ModuleData.isHangupEnable()) return;

                    Object miuiFreeformModeTaskInfo = getArgs(0);
                    String packageName = (String) callMethod(miuiFreeformModeTaskInfo, "getPackageName");
                    if (packageName == null || packageName.isEmpty()) return;

                    if (ModuleData.shouldForegroundPin(packageName)) {
                        if (HangupHandler.mHangupSet.contains(packageName)) {
                            Parcel obtain = Parcel.obtain();
                            Parcel obtain1 = Parcel.obtain();
                            obtain.writeInterfaceToken("android.app.IActivityManager");
                            obtain.writeString(packageName);
                            Class<?> clz = findClass("android.os.MiuiBinderTransaction$IActivityManager", ClassLoader.getSystemClassLoader());
                            Class<?> clz1 = findClass("android.app.ActivityManager", ClassLoader.getSystemClassLoader());
                            Object getService = callStaticMethod(clz1, "getService");
                            Object asBinder = callMethod(getService, "asBinder");
                            int TRANSACT_ID_SET_PACKAGE_HOLD_ON = (int) getStaticField(clz, "TRANSACT_ID_SET_PACKAGE_HOLD_ON");
                            callMethod(asBinder, "transact", TRANSACT_ID_SET_PACKAGE_HOLD_ON, obtain, obtain1, 0);
                            HangupHandler.mHangupSet.remove(packageName);
                        }
                    }
                }
            }
        );
    }

    private void removeHandler() {
        mHangupHandler.removeMessages(HangupHandler.HANGUP_READY);
        mHangupHandler.removeMessages(HangupHandler.HANGUP_LOW_TIME);
        mHangupHandler.removeMessages(HangupHandler.HANGUP_CANCELED);
    }
}
