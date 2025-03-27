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

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Parcel;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hchen.foregroundpin.utils.HangupHandler;
import com.hchen.hooktool.BaseHC;

/**
 * 息屏听剧 Api
 *
 * @author 焕晨HChen
 */
public class HangupApi extends BaseHC {
    private static final String HANGUP_API = "hangup_api";

    @Override
    protected void init() {
    }

    @Override
    protected void onApplicationAfter(Context context) {
        context.getContentResolver().registerContentObserver(Settings.Global.getUriFor(HANGUP_API),
            false, new ContentObserver(new Handler(context.getMainLooper())) {
                @Override
                public void onChange(boolean selfChange) {
                    if (selfChange) return;
                    String hangupApi = getHangupApi(context);
                    if (hangupApi == null || hangupApi.isEmpty()) return;
                    doHangup(hangupApi);
                    clearHangupApi(context);
                }
            }
        );
    }

    @Nullable
    private String getHangupApi(@NonNull Context context) {
        return Settings.Global.getString(context.getContentResolver(), HANGUP_API);
    }

    private void clearHangupApi(@NonNull Context context) {
        Settings.Global.putString(context.getContentResolver(), HANGUP_API, "");
    }

    private void doHangup(String packageName) {
        Parcel obtain = Parcel.obtain();
        obtain.writeInterfaceToken("android.app.IActivityManager");
        obtain.writeString(packageName);

        Class<?> clz = findClass("android.os.MiuiBinderTransaction$IActivityManager");
        Class<?> clz1 = findClass("android.app.ActivityManager");
        Object getService = callStaticMethod(clz1, "getService");
        Object asBinder = callMethod(getService, "asBinder");
        int TRANSACT_ID_SET_PACKAGE_HOLD_ON = (int) getStaticField(clz, "TRANSACT_ID_SET_PACKAGE_HOLD_ON");
        callMethod(asBinder, "transact", TRANSACT_ID_SET_PACKAGE_HOLD_ON, obtain, Parcel.obtain(), 0);
        HangupHandler.mHangupSet.remove(packageName);
    }
}
