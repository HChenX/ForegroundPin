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

import static com.hchen.foregroundpin.utils.HangupHandler.HANGUP_CANCELED;
import static com.hchen.foregroundpin.utils.HangupHandler.HANGUP_LOW_TIME;
import static com.hchen.foregroundpin.utils.HangupHandler.HANGUP_READY;

import android.content.Context;
import android.graphics.Rect;
import android.os.Looper;
import android.os.Parcel;

import androidx.annotation.Nullable;

import com.hchen.foregroundpin.utils.HangupHandler;
import com.hchen.foregroundpin.utils.ModuleData;
import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.hook.IHook;
import com.hchen.hooktool.tool.additional.DeviceTool;

/**
 * Miui 息屏听剧
 *
 * @author 焕晨HChen
 */
public class MiuiHangup extends BaseHC {
    private static boolean isCancel = false;
    private static final HangupHandler mHangupHandler = new HangupHandler(Looper.getMainLooper());

    @Override
    protected void init() {
        if (DeviceTool.isMoreHyperOSVersion(1f)) return; // 不是 Miui 跳过

        hookMethod("com.android.server.wm.MiuiFreeFormManagerService",
            "dispatchFreeFormStackModeChanged",
            int.class, "com.android.server.wm.MiuiFreeFormActivityStack",
            new IHook() {
                @Override
                public void after() {
                    if (!ModuleData.isModuleEnable() || !ModuleData.isHangupEnable()) return;

                    String packageName = getPackageName(getArgs(1));
                    Object mActivityTaskManagerService = getThisField("mActivityTaskManagerService");
                    Context mContext = (Context) getField(mActivityTaskManagerService, "mContext");
                    if (ModuleData.shouldForegroundPin(packageName)) {
                        Integer action = (Integer) getArgs(0);
                        if (action == null) return;

                        if (action == 6) {
                            isCancel = false;
                            removeHandler();
                            mHangupHandler.sendMessageDelayed(
                                mHangupHandler.obtainMessage(HANGUP_READY, new Object[]{mContext, packageName}),
                                1000
                            );
                        } else if (action == 7) {
                            if (isCancel) return;
                            removeHandler();
                            mHangupHandler.sendMessage(
                                mHangupHandler.obtainMessage(HANGUP_LOW_TIME, mContext)
                            );
                        }
                    }
                }
            }
        );

        hookMethod("com.android.server.wm.MiuiFreeFormWindowMotionHelper",
            "setLeashPositionAndScale",
            Rect.class, "com.android.server.wm.MiuiFreeFormActivityStack",
            new IHook() {
                @Override
                public void after() {
                    if (!ModuleData.isModuleEnable() || !ModuleData.isHangupEnable()) return;
                    if (isCancel) return;

                    String packageName = getPackageName(getArgs(1));
                    Object mListener = getThisField("mListener");
                    Object mService = getField(mListener, "mService");
                    Context mContext = (Context) getField(mService, "mContext");
                    if (ModuleData.shouldForegroundPin(packageName)) {
                        isCancel = true;
                        removeHandler();
                        mHangupHandler.sendMessage(mHangupHandler.obtainMessage(HANGUP_CANCELED, mContext));
                    }
                }
            }
        );

        hookMethod("com.android.server.wm.MiuiFreeformPinManagerService",
            "hideStack", "com.android.server.wm.MiuiFreeFormActivityStack",
            new IHook() {
                @Override
                public void before() {
                    if (!ModuleData.isModuleEnable() || !ModuleData.isHangupEnable()) return;

                    String packageName = getPackageName(getArgs(0));
                    if (ModuleData.shouldForegroundPin(packageName)) {
                        if (HangupHandler.mHangupSet.contains(packageName)) {
                            Parcel obtain = Parcel.obtain();
                            obtain.writeInterfaceToken("android.app.IActivityManager");
                            obtain.writeString(packageName);

                            Class<?> clz = findClass("android.os.MiuiBinderTransaction$IActivityManager");
                            Class<?> clz1 = findClass("android.app.ActivityManager");
                            Object getService = callStaticMethod(clz1, "getService");
                            Object asBinder = callMethod(getService, "asBinder");
                            callMethod(asBinder, "transact", getStaticField(clz, "TRANSACT_ID_SET_PACKAGE_HOLD_ON"), obtain, Parcel.obtain(), 0);
                            HangupHandler.mHangupSet.remove(packageName);
                        }
                    }
                }
            }
        );
    }

    @Nullable
    private String getPackageName(Object mffas) {
        if (mffas != null)
            return (String) callMethod(mffas, "getStackPackageName");

        return null;
    }

    private void removeHandler() {
        mHangupHandler.removeMessages(HANGUP_CANCELED);
        mHangupHandler.removeMessages(HANGUP_READY);
        mHangupHandler.removeMessages(HANGUP_LOW_TIME);
    }
}
