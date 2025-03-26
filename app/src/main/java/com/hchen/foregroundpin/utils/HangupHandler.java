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

import static com.hchen.hooktool.log.XposedLog.logI;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.HashSet;

public class HangupHandler extends Handler {
    public static final HashSet<String> mHangupSet = new HashSet<>();
    public static final int HANGUP_CANCELED = 0;
    public static final int HANGUP_READY = 1;
    public static final int HANGUP_LOW_TIME = 2;

    public HangupHandler(@NonNull Looper looper) {
        super(looper);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        switch (msg.what) {
            case HANGUP_READY -> {
                Object[] objects = (Object[]) msg.obj;
                if (mHangupSet.contains((String) objects[1])) {
                    return;
                }
                mHangupSet.add((String) objects[1]);
                logI("HangupHelper", "Successfully entered the screen-off mode: " + msg.obj);
                Toast.makeText((Context) objects[0], "成功进入息屏模式", Toast.LENGTH_SHORT).show();
            }
            case HANGUP_CANCELED -> {
                Toast.makeText((Context) msg.obj, "请勿移动手指", Toast.LENGTH_SHORT).show();
            }
            case HANGUP_LOW_TIME -> {
                Toast.makeText((Context) msg.obj, "长按时间过短", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
