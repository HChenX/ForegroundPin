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

import static com.hchen.hooktool.log.XposedLog.logE;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Rect;
import android.os.Parcel;
import android.util.SparseArray;
import android.view.View;

import com.hchen.foregroundpin.utils.HangupHelper;
import com.hchen.foregroundpin.utils.ObserverHelper;
import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.callback.IAction;
import com.hchen.hooktool.tool.ParamTool;
import com.hchen.hooktool.tool.StaticTool;
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
                        public void after(ParamTool param, StaticTool staticTool) {
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
                        public void before(ParamTool param, StaticTool staticTool) {
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
                        public void after(ParamTool param, StaticTool staticTool) {
                            if (isFreeFrom) {
                                Object MiuiWindowDecorViewModel = param.getField("this$0");
                                int mTaskId = param.getField("mTaskId");
                                SparseArray mWindowDecorByTaskId = expandTool.getField(MiuiWindowDecorViewModel, "mWindowDecorByTaskId");
                                Object miuiWindowDecoration = mWindowDecorByTaskId.get(mTaskId);
                                ActivityManager.RunningTaskInfo runningTaskInfo =
                                        expandTool.getField(miuiWindowDecoration, "mTaskInfo");
                                int mode = expandTool.callMethod(expandTool.getField(miuiWindowDecoration, "mTaskInfo"), "getWindowingMode");
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
                        public void after(ParamTool param, StaticTool staticTool) {
                            Object miuiFreeformModeTaskInfo = param.first();
                            String pkg = expandTool.callMethod(miuiFreeformModeTaskInfo, "getPackageName");
                            if (pkg == null) return;
                            if (observerHelper.findInMap(hashMap, pkg)) {
                                if (observerHelper.findInMap(mHandler.hangupMap, pkg)) {
                                    logE(TAG, "pkg: " + mHandler.hangupMap + " :" + pkg);
                                    Parcel obtain = Parcel.obtain();
                                    Parcel obtain1 = Parcel.obtain();
                                    obtain.writeInterfaceToken("android.app.IActivityManager");
                                    obtain.writeString(pkg);
                                    Class<?> clz = expandTool.findClass("android.os.MiuiBinderTransaction$IActivityManager",
                                            ClassLoader.getSystemClassLoader());
                                    Class<?> clz1 = expandTool.findClass("android.app.ActivityManager",
                                            ClassLoader.getSystemClassLoader());
                                    Object getService = expandTool.callStaticMethod(clz1, "getService");
                                    Object asBinder = expandTool.callMethod(getService, "asBinder");
                                    int TRANSACT_ID_SET_PACKAGE_HOLD_ON = expandTool.getStaticField(clz, "TRANSACT_ID_SET_PACKAGE_HOLD_ON");
                                    expandTool.callMethod(asBinder, "transact", new Object[]{TRANSACT_ID_SET_PACKAGE_HOLD_ON, obtain, obtain1, 0});
                                }
                            }
                        }
                    })

                    .to("mffmph")
                    .getMethod("unPinFloatingWindow",
                            Rect.class, int.class, boolean.class, boolean.class, boolean.class)
                    .hook(new IAction() {
                        @Override
                        public void after(ParamTool param, StaticTool staticTool) {
                            int i = param.second();
                            Object mMiuiFreeformModeTaskRepository = param.getField("mMiuiFreeformModeTaskRepository");
                            Object miuiFreeformTaskInfo =
                                    expandTool.callMethod(mMiuiFreeformModeTaskRepository, "getMiuiFreeformTaskInfo", i);
                            String pkg = expandTool.callMethod(miuiFreeformTaskInfo, "getPackageName");
                            if (pkg == null) return;
                            if (observerHelper.findInMap(hashMap, pkg)) {
                                if (observerHelper.findInMap(mHandler.hangupMap, pkg)) {
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
