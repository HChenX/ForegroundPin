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
package com.hchen.foregroundpin.hook;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Looper;
import android.os.Parcel;
import android.util.SparseArray;

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

        hookMethod("com.android.wm.shell.miuimultiwinswitch.MiuiMultiWinTrackUtils",
            "trackWindowControlButtonClick",
            Context.class, ActivityManager.RunningTaskInfo.class, String.class, String.class,
            new IHook() {
                @Override
                public void before() {
                    String str = (String) getArgs(2);
                    String str1 = (String) getArgs(3);

                    if ("小窗".equals(str) && "小窗".equals(str1)) {
                        Context context = (Context) getArgs(0);
                        Object MiuiWindowDecorViewModel = getThisField("this$0");
                        int mTaskId = (int) getThisField("mTaskId");
                        SparseArray<?> mWindowDecorByTaskId = (SparseArray<?>) getField(MiuiWindowDecorViewModel, "mWindowDecorByTaskId");
                        Object miuiWindowDecoration = mWindowDecorByTaskId.get(mTaskId);
                        ActivityManager.RunningTaskInfo runningTaskInfo = (ActivityManager.RunningTaskInfo) getField(miuiWindowDecoration, "mTaskInfo");
                        Object mTaskInfo = getField(miuiWindowDecoration, "mTaskInfo");
                        int mode = (int) callMethod(mTaskInfo, "getWindowingMode");

                        if (mode == 5) {
                            String packageName = runningTaskInfo.baseActivity.getPackageName();
                            if (ModuleData.shouldForegroundPin(packageName)) {
                                removeHandler();
                                if (!HangupHandler.mHangupSet.contains(packageName))
                                    mHangupHandler.handleMessage(mHangupHandler.obtainMessage(HangupHandler.HANGUP_READY, new Object[]{context, packageName}));
                            }
                        }
                    }
                }
            }
        );

        hookMethod("com.android.wm.shell.miuifreeform.MiuiFreeformModePinHandler",
            "hideTask",
            "com.android.wm.shell.miuifreeform.MiuiFreeformModeTaskInfo",
            new IHook() {
                @Override
                public void after() {
                    Object miuiFreeformModeTaskInfo = getArgs(0);
                    String packageName = (String) callMethod(miuiFreeformModeTaskInfo, "getPackageName");
                    if (packageName == null) return;
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
