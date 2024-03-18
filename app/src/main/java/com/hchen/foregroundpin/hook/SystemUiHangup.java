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
import android.graphics.Rect;
import android.os.Parcel;
import android.util.SparseArray;
import android.view.View;

import com.hchen.foregroundpin.mode.Hook;
import com.hchen.foregroundpin.utils.HangupHelper;
import com.hchen.foregroundpin.utils.ObserverHelper;

import java.util.HashMap;

import de.robv.android.xposed.XposedHelpers;

public class SystemUiHangup extends Hook {
    private boolean isObserver = false;
    private final HangupHelper mHandler = new HangupHelper();
    private final ObserverHelper observerHelper = new ObserverHelper();
    private final int CANCEL_HANGUP = HangupHelper.CANCEL_HANGUP;
    private final int WILL_HANGUP = HangupHelper.WILL_HANGUP;
    private final int LOW_TIME_HANGUP = HangupHelper.LOW_TIME_HANGUP;
    private final HashMap<String, Integer> hashMap = new HashMap<>();

    @Override
    public void init() {
        findAndHookMethod("com.android.wm.shell.miuifreeform.MiuiFreeformModeGestureHandler",
                "onInit",
                new HookAction() {
                    @Override
                    protected void after(MethodHookParam param) {
                        Context mContext = (Context) getObjectField(param.thisObject, "mContext");
                        if (!isObserver) {
                            mHandler.setContext(mContext);
                            observerHelper.setTAG(tag);
                            observerHelper.setObserver(mContext, hashMap, mHandler.hangupMap);
                            isObserver = true;
                        }
                    }
                }
        );

        findAndHookMethod("com.android.wm.shell.miuimultiwinswitch.miuiwindowdecor.MiuiWindowDecorViewModel$MiuiCaptionTouchEventListener",
                "onClick", View.class,
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
                        int id = ((View) param.args[0]).getId();
                        if (id == 2131364049) {
                            Object MiuiWindowDecorViewModel = getObjectField(param.thisObject, "this$0");
                            int mTaskId = (int) getObjectField(param.thisObject, "mTaskId");
                            SparseArray mWindowDecorByTaskId = (SparseArray) getObjectField(MiuiWindowDecorViewModel, "mWindowDecorByTaskId");
                            Object miuiWindowDecoration = mWindowDecorByTaskId.get(mTaskId);
                            ActivityManager.RunningTaskInfo runningTaskInfo = (ActivityManager.RunningTaskInfo)
                                    getObjectField(miuiWindowDecoration, "mTaskInfo");
                            int mode = (int) callMethod(getObjectField(miuiWindowDecoration, "mTaskInfo"), "getWindowingMode");
                            if (mode == 5) {
                                String pkg = runningTaskInfo.baseActivity.getPackageName();
                                if (pkg == null) return;
                                if (observerHelper.findInMap(hashMap, pkg)) {
                                    removeHandler();
                                    if (!observerHelper.findInMap(mHandler.hangupMap, pkg))
                                        mHandler.handleMessage(mHandler.obtainMessage(WILL_HANGUP, pkg));
                                }
                            }
                        }
                    }
                }
        );

        findAndHookMethod("com.android.wm.shell.miuifreeform.MiuiFreeformModePinHandler",
                "hideTask", "com.android.wm.shell.miuifreeform.MiuiFreeformModeTaskInfo",
                new HookAction() {
                    @Override
                    protected void after(MethodHookParam param) {
                        Object miuiFreeformModeTaskInfo = param.args[0];
                        String pkg = (String) callMethod(miuiFreeformModeTaskInfo, "getPackageName");
                        if (pkg == null) return;
                        if (observerHelper.findInMap(hashMap, pkg)) {
                            if (observerHelper.findInMap(mHandler.hangupMap, pkg)) {
                                logE(tag, "pkg: " + mHandler.hangupMap + " :" + pkg);
                                Parcel obtain = Parcel.obtain();
                                Parcel obtain1 = Parcel.obtain();
                                obtain.writeInterfaceToken("android.app.IActivityManager");
                                obtain.writeString(pkg);
                                Class<?> clz = findClassIfExists("android.os.MiuiBinderTransaction$IActivityManager",
                                        ClassLoader.getSystemClassLoader());
                                Class<?> clz1 = findClassIfExists("android.app.ActivityManager",
                                        ClassLoader.getSystemClassLoader());
                                Object getService = callStaticMethod(clz1, "getService");
                                Object asBinder = callMethod(getService, "asBinder");
                                int TRANSACT_ID_SET_PACKAGE_HOLD_ON = XposedHelpers.getStaticIntField(clz, "TRANSACT_ID_SET_PACKAGE_HOLD_ON");
                                XposedHelpers.callMethod(asBinder, "transact", TRANSACT_ID_SET_PACKAGE_HOLD_ON, obtain, obtain1, 0);
                            }
                        }
                    }
                }
        );

        findAndHookMethod("com.android.wm.shell.miuifreeform.MiuiFreeformModePinHandler",
                "unPinFloatingWindow",
                Rect.class, int.class, boolean.class, boolean.class, boolean.class,
                new HookAction() {
                    @Override
                    protected void after(MethodHookParam param) {
                        int i = (int) param.args[1];
                        Object mMiuiFreeformModeTaskRepository = getObjectField(param.thisObject, "mMiuiFreeformModeTaskRepository");
                        Object miuiFreeformTaskInfo =
                                callMethod(mMiuiFreeformModeTaskRepository, "getMiuiFreeformTaskInfo", i);
                        String pkg = (String) callMethod(miuiFreeformTaskInfo, "getPackageName");
                        if (pkg == null) return;
                        if (observerHelper.findInMap(hashMap, pkg)) {
                            if (observerHelper.findInMap(mHandler.hangupMap, pkg)) {
                                mHandler.hangupMap.remove(pkg);
                            }
                        }
                    }
                }
        );
    }

    private void removeHandler() {
        mHandler.removeMessages(WILL_HANGUP);
        mHandler.removeMessages(CANCEL_HANGUP);
        mHandler.removeMessages(LOW_TIME_HANGUP);
    }
}
