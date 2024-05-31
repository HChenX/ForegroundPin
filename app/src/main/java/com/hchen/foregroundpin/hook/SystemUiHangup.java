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
import com.hchen.hooktool.tool.ParamTool;
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
            classTool.findClass("mffmgh", "com.android.wm.shell.miuifreeform.MiuiFreeformModeGestureHandler")
                    .getMethod("onInit")
                    .hook(new IAction() {
                        @Override
                        public void after(ParamTool param) {
                            Context mContext = param.getField("mContext");
                            if (!isObserver) {
                                mHandler.setContext(mContext);
                                observerHelper.setTAG(TAG);
                                observerHelper.setObserver(mContext, hashMap, mHandler.hangupMap);
                                isObserver = true;
                            }
                        }
                    })

                    .findClass("mmwtu", "com.android.wm.shell.miuimultiwinswitch.MiuiMultiWinTrackUtils")
                    .getMethod("trackWindowControlButtonClick", Context.class,
                            "android.app.ActivityManager$RunningTaskInfo", String.class, String.class)
                    .hook(new IAction() {
                        @Override
                        public void before(ParamTool param) {
                            String str = param.third();
                            String str1 = param.fourth();
                            if ("\u5c0f\u7a97".equals(str) && "\u5c0f\u7a97".equals(str1)) {
                                isFreeFrom = true;
                            }
                        }
                    })

                    .findClass("mwdvm$mctel",
                            "com.android.wm.shell.miuimultiwinswitch.miuiwindowdecor.MiuiWindowDecorViewModel$MiuiCaptionTouchEventListener")
                    .getMethod("onClick", View.class)
                    .hook(new IAction() {
                        @Override
                        public void after(ParamTool param) {
                            if (isFreeFrom) {
                                Object MiuiWindowDecorViewModel = param.getField("this$0");
                                int mTaskId = param.getField("mTaskId");
                                SparseArray mWindowDecorByTaskId = param.to(MiuiWindowDecorViewModel).getField("mWindowDecorByTaskId");
                                Object miuiWindowDecoration = mWindowDecorByTaskId.get(mTaskId);
                                ActivityManager.RunningTaskInfo runningTaskInfo =
                                        param.to(miuiWindowDecoration).getField("mTaskInfo");
                                Object mTaskInfo = param.to(miuiWindowDecoration).getField("mTaskInfo");
                                int mode = param.to(mTaskInfo).callMethod("getWindowingMode");
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
                    })

                    .findClass("mffmph", "com.android.wm.shell.miuifreeform.MiuiFreeformModePinHandler")
                    .getMethod("hideTask", "com.android.wm.shell.miuifreeform.MiuiFreeformModeTaskInfo")
                    .hook(new IAction() {
                        @Override
                        public void after(ParamTool param) {
                            Object miuiFreeformModeTaskInfo = param.first();
                            String pkg = param.to(miuiFreeformModeTaskInfo).callMethod("getPackageName");
                            if (pkg == null) return;
                            if (observerHelper.findInMap(hashMap, pkg)) {
                                if (observerHelper.findInMap(mHandler.hangupMap, pkg)) {
                                    // logI(TAG, "pkg: " + mHandler.hangupMap + " :" + pkg);
                                    Parcel obtain = Parcel.obtain();
                                    Parcel obtain1 = Parcel.obtain();
                                    obtain.writeInterfaceToken("android.app.IActivityManager");
                                    obtain.writeString(pkg);
                                    Class<?> clz = param.findClass("android.os.MiuiBinderTransaction$IActivityManager",
                                            ClassLoader.getSystemClassLoader());
                                    Class<?> clz1 = param.findClass("android.app.ActivityManager",
                                            ClassLoader.getSystemClassLoader());
                                    Object getService = param.to(clz1).callStaticMethod("getService");
                                    Object asBinder = param.to(getService).callMethod("asBinder");
                                    int TRANSACT_ID_SET_PACKAGE_HOLD_ON = param.to(clz).getStaticField("TRANSACT_ID_SET_PACKAGE_HOLD_ON");
                                    param.to(asBinder).callMethod("transact", new Object[]{TRANSACT_ID_SET_PACKAGE_HOLD_ON, obtain, obtain1, 0});
                                    mHandler.hangupMap.remove(pkg);
                                }
                            }
                        }
                    })

                    /*.to("mffmph")
                    .getMethod("unPinFloatingWindow",
                            Rect.class, int.class, boolean.class, boolean.class, boolean.class)
                    .hook(new IAction() {
                        @Override
                        public void after(ParamTool param) {
                            int i = param.second();
                            Object mMiuiFreeformModeTaskRepository = param.getField("mMiuiFreeformModeTaskRepository");
                            Object miuiFreeformTaskInfo =
                                    param.callMethod(mMiuiFreeformModeTaskRepository, "getMiuiFreeformTaskInfo", i);
                            String pkg = param.callMethod(miuiFreeformTaskInfo, "getPackageName");
                            if (pkg == null) return;
                            if (observerHelper.findInMap(hashMap, pkg)) {
                                if (observerHelper.findInMap(mHandler.hangupMap, pkg)) {
                                    mHandler.hangupMap.remove(pkg);
                                }
                            }
                        }
                    })*/;
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
