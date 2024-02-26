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

import static de.robv.android.xposed.callbacks.XCallback.PRIORITY_LOWEST;

import android.app.ActivityManager;
import android.graphics.Rect;
import android.view.SurfaceControl;

import com.hchen.foregroundpin.mode.Hook;

import java.util.List;

import de.robv.android.xposed.XposedHelpers;

/**
 * 这是测试类，已经弃用的
 */
public class Test extends Hook {
    @Override
    public void init() {
        findAndHookMethod("com.android.server.wm.MiuiFreeformPinManagerService",
                "hideStack", "com.android.server.wm.MiuiFreeFormActivityStack",
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
                        Object stack = param.args[0];
                        Object mainWindow;
                        Object mController = XposedHelpers.getObjectField(param.thisObject, "mController");
                        Object mGestureListener = XposedHelpers.getObjectField(mController, "mGestureListener");
                        Object mGestureAnimator = XposedHelpers.getObjectField(mGestureListener, "mGestureAnimator");
                        XposedHelpers.callMethod(mGestureAnimator, "hideStack", stack);
                        XposedHelpers.callMethod(mGestureAnimator, "applyTransaction");
                        if (stack != null) {
                            Object mTask = XposedHelpers.getObjectField(stack, "mTask");
                            if (mTask != null) {
                                SurfaceControl surfaceControl;
                                if (mTask != null && (surfaceControl =
                                        (SurfaceControl) XposedHelpers.getObjectField(mTask,
                                                "mSurfaceControl")) != null
                                        && surfaceControl.isValid()) {
                                    SurfaceControl.Transaction transaction = new SurfaceControl.Transaction();
                                    // XposedHelpers.callMethod(transaction, "setCrop", surfaceControl, null);
                                    XposedHelpers.callMethod(transaction, "hide", surfaceControl);
                                    XposedHelpers.callMethod(transaction, "apply");
                                    logE(tag, "moveTaskToBack: s: " + surfaceControl);
                                }
                            }
                        }
                        Object activityRecord = XposedHelpers.getObjectField(stack, "mLastIconLayerWindowToken");
                        if (activityRecord != null) {
                            if (XposedHelpers.getObjectField(activityRecord, "mFloatWindwoIconSurfaceControl") != null &&
                                    (mainWindow = XposedHelpers.callMethod(activityRecord, "findMainWindow")) != null) {
                                XposedHelpers.callMethod(mGestureAnimator, "setAlphaInTransaction",
                                        XposedHelpers.getObjectField(mainWindow, "mSurfaceControl"), 1.0f);
                                XposedHelpers.callMethod(mGestureAnimator, "applyTransaction");
                            }
                        }
                        XposedHelpers.callMethod(param.thisObject, "clearMiuiFreeWindowFloatIconLayer", stack);
                        boolean isInMiniFreeFormMode = (boolean) XposedHelpers.callMethod(stack, "isInMiniFreeFormMode");
                        if (!isInMiniFreeFormMode) {
                            XposedHelpers.callMethod(mGestureAnimator, "removeAnimationControlLeash", stack);
                        } else {
                            XposedHelpers.callMethod(mGestureAnimator, "setWindowCropInTransaction", stack, null);
                        }
                        XposedHelpers.callMethod(stack, "updateCornerRadius");
                        XposedHelpers.setObjectField(stack, "mIsRunningPinAnim", false);
                        Object service = XposedHelpers.getObjectField(mController, "mMiuiFreeFormManagerService");
                        Object mActivityTaskManagerService = XposedHelpers.getObjectField(service, "mActivityTaskManagerService");
                        Object getInstance = XposedHelpers.callStaticMethod(findClassIfExists("android.app.ActivityTaskManager"),
                                "getInstance");
                        List<ActivityManager.RunningTaskInfo> tasks = (List<ActivityManager.RunningTaskInfo>)
                                XposedHelpers.callMethod(getInstance, "getTasks", 1, false);
                        if (!tasks.isEmpty()) {
                            ActivityManager.RunningTaskInfo runningTaskInfo = tasks.get(0);
                            int taskId = runningTaskInfo.taskId;
                            Object nextFocusdTask = null;
                            Object curentTask = XposedHelpers.callMethod(XposedHelpers.getObjectField(mActivityTaskManagerService,
                                            "mRootWindowContainer"),
                                    "anyTaskForId", taskId, 0);
                            if (curentTask != null && (boolean) XposedHelpers.callMethod(curentTask, "isFocused")
                                    && (nextFocusdTask = XposedHelpers.callMethod(curentTask, "getNextFocusableTask", false)) != null) {
                                XposedHelpers.callMethod(mActivityTaskManagerService, "setFocusedRootTask",
                                        XposedHelpers.callMethod(nextFocusdTask, "getRootTaskId"));
                            }
                            logE(tag, "run: " + runningTaskInfo + " id: " + taskId + " cur: " + curentTask + " next: " + nextFocusdTask);
                        }
                        logE(tag, "run here");
                        param.setResult(null);
                    }
                }
        );

        findAndHookMethod("com.android.server.wm.MiuiFreeFormManagerService",
                "updatePinFloatingWindowPos",
                Rect.class, int.class, boolean.class, new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
                        if ((boolean) param.args[2]) {
                            Object mffas = callMethod(param.thisObject, "getMiuiFreeFormActivityStackForMiuiFB", param.args[1]);
                            Object mMiuiFreeFormGestureController = getObjectField(
                                    getObjectField(
                                            getObjectField(
                                                    param.thisObject,
                                                    "mActivityTaskManagerService"),
                                            "mWindowManager"),
                                    "mMiuiFreeFormGestureController");
                            Object mGestureAnimator = getObjectField(
                                    getObjectField(mMiuiFreeFormGestureController,
                                            "mGestureListener"),
                                    "mGestureAnimator");
                            callMethod(getObjectField(mffas, "mLastIconLayerWindowToken"), "setVisibility", false);
                            callMethod(mGestureAnimator, "hideStack", mffas);
                            callMethod(mGestureAnimator, "applyTransaction");
                        }
                    }
                }
        );

        findAndHookMethod("com.android.server.wm.MiuiMultiWindowSwitchManager",
                "dropToFreeForm",
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
                        logE(tag, "dropToFreeForm im run");
                    }
                }
        );

        findAndHookMethod("com.android.server.wm.MiuiFreeformPinManagerService",
                "updatePinFloatingWindowPos",
                "com.android.server.wm.MiuiFreeFormActivityStack",
                Rect.class, boolean.class,
                new HookAction(PRIORITY_LOWEST * 2) {
                    @Override
                    protected void after(MethodHookParam param) {
                        if ((boolean) param.args[2]) {
                            Object mGestureAnimator = getObjectField(
                                    getObjectField(
                                            getObjectField(
                                                    param.thisObject,
                                                    "mController"),
                                            "mGestureListener"),
                                    "mGestureAnimator");
                            /*尽量提升效率*/
                            callMethod(
                                    getObjectField(param.args[0],
                                            "mLastIconLayerWindowToken"),
                                    "setVisibility", false, false);
                            // callMethod(mGestureAnimator, "hideStack", param.args[0]);
                            // callMethod(mGestureAnimator, "applyTransaction");
                        }
                    }
                }
        );

        findAndHookMethod("com.android.server.wm.MiuiFreeformTrackManager",
                "trackSmallWindowPinedMoveEvent",
                String.class, String.class, int.class, int.class,
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
                        logE(tag, "trackSmallWindowPinedMoveEvent: 1: " + param.args[0]
                                + " 2: " + param.args[1] + " 3: " + param.args[2] + " 4: " + param.args[3]);
                        param.setResult(null);
                    }
                }
        );

        findAndHookMethod("com.android.server.wm.MiuiFreeformTrackManager",
                "trackMiniWindowPinedMoveEvent", String.class, String.class, int.class, int.class,
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
                        logE(tag, "trackMiniWindowPinedMoveEvent: 1: " + param.args[0]
                                + " 2: " + param.args[1] + " 3: " + param.args[2] + " 4: " + param.args[3]);
                        param.setResult(null);
                    }
                }
        );

        findAndHookMethod("com.android.server.wm.MiuiFreeFormGestureAnimator",
                "hideStack",
                "com.android.server.wm.MiuiFreeFormActivityStack",
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
                        logE(tag, "hideStack: " + param.args[0]);
                        param.setResult(null);
                    }
                }
        );

        findAndHookMethod("com.android.server.wm.MiuiFreeFormGestureAnimator",
                "hide",
                "com.android.server.wm.WindowContainer",
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
                        logE(tag, "hide: " + param.args[0]);
                    }
                }
        );

        findAndHookMethod("com.android.server.wm.MiuiFreeFormGestureAnimator",
                "showStack",
                "com.android.server.wm.MiuiFreeFormActivityStack",
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
                        logE(tag, "showStack: " + param.args[0]);
                        param.setResult(null);
                    }
                }
        );
    }
}
