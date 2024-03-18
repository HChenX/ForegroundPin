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

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.hchen.foregroundpin.mode.Log;

import java.util.HashMap;

public class HangupHelper extends Handler {
    public HashMap<String, Integer> hangupMap = new HashMap<>();
    public static final int CANCEL_HANGUP = 0;
    public static final int WILL_HANGUP = 1;
    public static final int LOW_TIME_HANGUP = 2;

    Context mContext = null;

    @Override
    public void handleMessage(@NonNull Message msg) {
        switch (msg.what) {
            case WILL_HANGUP -> {
                if (!haveKey((String) msg.obj)) {
                    hangupMap.put((String) msg.obj, 1);
                    Log.logE("SystemUiHangup", "obj: " + msg.obj);
                    ToastHelper.makeText(mContext, "成功进入息屏模式");
                }
            }
            case CANCEL_HANGUP -> {
                ToastHelper.makeText(mContext, "请勿移动手指");
            }
            case LOW_TIME_HANGUP -> {
                ToastHelper.makeText(mContext, "长按时间过短");
            }
        }
    }

    private boolean haveKey(String pkg) {
        Integer result = hangupMap.get(pkg);
        return result != null && result == 1;
    }

    public void setContext(Context context) {
        mContext = context;
    }
}
