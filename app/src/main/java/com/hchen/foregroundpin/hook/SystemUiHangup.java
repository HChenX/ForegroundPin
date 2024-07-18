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
import android.os.Parcel;
import android.util.SparseArray;
import android.view.View;

import com.hchen.foregroundpin.utils.HangupHelper;
import com.hchen.foregroundpin.utils.ObserverHelper;
import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.callback.IAction;
import com.hchen.hooktool.utils.SystemSDK;

import java.util.HashMap;

/**
 * @noinspection DataFlowIssue
 */
public class SystemUiHangup extends BaseHC {
    private boolean isObserver = false;
    private boolean isFreeFrom = false;
    private final HangupHelper mHandler = new HangupHelper();
    private final ObserverHelper observerHelper = new ObserverHelper();
    private final int WILL_HANGUP = HangupHelper.WILL_HANGUP;
    private final HashMap<String, Integer> hashMap = new HashMap<>();

    @Override
    public void init() {
        // Hyper 专用
        if (SystemSDK.isMoreHyperOSVersion(1f)) {
            hook("com.android.wm.shell.miuifreeform.MiuiFreeformModeGestureHandler", "onInit", new IAction() {
                @Override
                public void after() throws Throwable {
                    Context mContext = getThisField("mContext");
                    if (!isObserver) {
                        mHandler.setContext(mContext);
                        observerHelper.setTAG(TAG);
                        observerHelper.setObserver(mContext, hashMap, mHandler.hangupMap);
                        isObserver = true;
                    }
                }
            });

            hook("com.android.wm.shell.miuimultiwinswitch.MiuiMultiWinTrackUtils", "trackWindowControlButtonClick", Context.class,
                    "android.app.ActivityManager$RunningTaskInfo", String.class, String.class, new IAction() {
                        @Override
                        public void before() throws Throwable {
                            String str = third();
                            String str1 = fourth();
                            if ("\u5c0f\u7a97".equals(str) && "\u5c0f\u7a97".equals(str1)) {
                                isFreeFrom = true;
                            }
                        }
                    });

            hook("com.android.wm.shell.miuimultiwinswitch.miuiwindowdecor.MiuiWindowDecorViewModel$MiuiCaptionTouchEventListener",
                    "onClick", View.class, new IAction() {
                        @Override
                        public void after() throws Throwable {
                            if (isFreeFrom) {
                                Object MiuiWindowDecorViewModel = getThisField("this$0");
                                int mTaskId = getThisField("mTaskId");
                                SparseArray mWindowDecorByTaskId = getField(MiuiWindowDecorViewModel, "mWindowDecorByTaskId");
                                Object miuiWindowDecoration = mWindowDecorByTaskId.get(mTaskId);
                                ActivityManager.RunningTaskInfo runningTaskInfo =
                                        getField(miuiWindowDecoration, "mTaskInfo");
                                Object mTaskInfo = getField(miuiWindowDecoration, "mTaskInfo");
                                int mode = callMethod(mTaskInfo, "getWindowingMode");
                                if (mode == 5) {
                                    String pkg = runningTaskInfo.baseActivity.getPackageName();
                                    if (observerHelper.findInMap(hashMap, pkg)) {
                                        removeHandler();
                                        if (!observerHelper.findInMap(mHandler.hangupMap, pkg))
                                            mHandler.handleMessage(mHandler.obtainMessage(WILL_HANGUP, pkg));
                                    }
                                }
                                isFreeFrom = false;
                            }
                        }
                    });

            hook("com.android.wm.shell.miuifreeform.MiuiFreeformModePinHandler",
                    "hideTask", "com.android.wm.shell.miuifreeform.MiuiFreeformModeTaskInfo", new IAction() {
                        @Override
                        public void after() throws Throwable {
                            Object miuiFreeformModeTaskInfo = first();
                            String pkg = callMethod(miuiFreeformModeTaskInfo, "getPackageName");
                            if (pkg == null) return;
                            if (observerHelper.findInMap(hashMap, pkg)) {
                                if (observerHelper.findInMap(mHandler.hangupMap, pkg)) {
                                    // logI(TAG, "pkg: " + mHandler.hangupMap + " :" + pkg);
                                    Parcel obtain = Parcel.obtain();
                                    Parcel obtain1 = Parcel.obtain();
                                    obtain.writeInterfaceToken("android.app.IActivityManager");
                                    obtain.writeString(pkg);
                                    Class<?> clz = findClass("android.os.MiuiBinderTransaction$IActivityManager",
                                            ClassLoader.getSystemClassLoader());
                                    Class<?> clz1 = findClass("android.app.ActivityManager",
                                            ClassLoader.getSystemClassLoader());
                                    Object getService = callStaticMethod(clz1, "getService");
                                    Object asBinder = callMethod(getService, "asBinder");
                                    int TRANSACT_ID_SET_PACKAGE_HOLD_ON = getStaticField(clz, "TRANSACT_ID_SET_PACKAGE_HOLD_ON");
                                    callMethod(asBinder, "transact", new Object[]{TRANSACT_ID_SET_PACKAGE_HOLD_ON, obtain, obtain1, 0});
                                    mHandler.hangupMap.remove(pkg);
                                }
                            }
                        }
                    });
        }
    }

    private void removeHandler() {
        mHandler.removeMessages(WILL_HANGUP);
        int CANCEL_HANGUP = HangupHelper.CANCEL_HANGUP;
        mHandler.removeMessages(CANCEL_HANGUP);
        int LOW_TIME_HANGUP = HangupHelper.LOW_TIME_HANGUP;
        mHandler.removeMessages(LOW_TIME_HANGUP);
    }
}
