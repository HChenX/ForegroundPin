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
package com.hchen.foregroundpin.hook.test;

import android.app.ActivityManager;
import android.graphics.Rect;
import android.view.SurfaceControl;

import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.hook.IHook;

import java.util.List;

/**
 * 测试类
 *
 * @deprecated
 */
@Deprecated
public class TestHook extends BaseHC {
    @Override
    public void init() {
        hookMethod("com.android.server.wm.MiuiFreeformPinManagerService",
            "hideStack", "com.android.server.wm.MiuiFreeFormActivityStack",
            new IHook() {
                @Override
                public void before() {
                    Object stack = getArgs(0);
                    Object mainWindow;
                    Object mController = getThisField("mController");
                    Object mGestureListener = getField(mController, "mGestureListener");
                    Object mGestureAnimator = getField(mGestureListener, "mGestureAnimator");
                    callMethod(mGestureAnimator, "hideStack", stack);
                    callMethod(mGestureAnimator, "applyTransaction");
                    if (stack != null) {
                        Object mTask = getField(stack, "mTask");
                        if (mTask != null) {
                            SurfaceControl surfaceControl;
                            if (
                                mTask != null &&
                                    (surfaceControl = (SurfaceControl) getField(mTask, "mSurfaceControl")) != null
                                    && surfaceControl.isValid()
                            ) {
                                SurfaceControl.Transaction transaction = new SurfaceControl.Transaction();
                                // XposedHelpers.callMethod(transaction, "setCrop", surfaceControl, null);
                                callMethod(transaction, "hide", surfaceControl);
                                callMethod(transaction, "apply");
                                logE(TAG, "moveTaskToBack: s: " + surfaceControl);
                            }
                        }
                    }
                    Object activityRecord = getField(stack, "mLastIconLayerWindowToken");
                    if (activityRecord != null) {
                        if (
                            getField(activityRecord, "mFloatWindwoIconSurfaceControl") != null &&
                                (mainWindow = callMethod(activityRecord, "findMainWindow")) != null
                        ) {
                            callMethod(mGestureAnimator, "setAlphaInTransaction",
                                getField(mainWindow, "mSurfaceControl"), 1.0f);
                            callMethod(mGestureAnimator, "applyTransaction");
                        }
                    }
                    callThisMethod("clearMiuiFreeWindowFloatIconLayer", stack);
                    boolean isInMiniFreeFormMode = (boolean) callMethod(stack, "isInMiniFreeFormMode");
                    if (!isInMiniFreeFormMode) {
                        callMethod(mGestureAnimator, "removeAnimationControlLeash", stack);
                    } else {
                        callMethod(mGestureAnimator, "setWindowCropInTransaction", stack, null);
                    }
                    callMethod(stack, "updateCornerRadius");
                    setField(stack, "mIsRunningPinAnim", false);
                    Object service = getField(mController, "mMiuiFreeFormManagerService");
                    Object mActivityTaskManagerService = getField(service, "mActivityTaskManagerService");
                    Object getInstance = callStaticMethod("android.app.ActivityTaskManager", "getInstance");
                    List<ActivityManager.RunningTaskInfo> tasks = (List<ActivityManager.RunningTaskInfo>) callMethod(getInstance, "getTasks", 1, false);
                    if (!tasks.isEmpty()) {
                        ActivityManager.RunningTaskInfo runningTaskInfo = tasks.get(0);
                        int taskId = runningTaskInfo.taskId;
                        Object nextFocusdTask = null;
                        Object curentTask = callMethod(getField(mActivityTaskManagerService, "mRootWindowContainer"), "anyTaskForId", taskId, 0);

                        if (
                            curentTask != null &&
                                (boolean) callMethod(curentTask, "isFocused")
                                && (nextFocusdTask = callMethod(curentTask, "getNextFocusableTask", false)) != null
                        ) {
                            callMethod(mActivityTaskManagerService, "setFocusedRootTask",
                                callMethod(nextFocusdTask, "getRootTaskId"));
                        }
                        logE(TAG, "run: " + runningTaskInfo + " id: " + taskId + " cur: " + curentTask + " next: " + nextFocusdTask);
                    }
                    logE(TAG, "run here");
                    setResult(null);
                }
            }
        );

        hookMethod("com.android.server.wm.MiuiFreeFormManagerService",
            "updatePinFloatingWindowPos",
            Rect.class, int.class, boolean.class, new IHook() {
                @Override
                public void before() {
                    if ((boolean) getArgs(2)) {
                        Object mffas = callThisMethod("getMiuiFreeFormActivityStackForMiuiFB", getArgs(1));
                        Object mMiuiFreeFormGestureController = getField(
                            getField(
                                getThisField(
                                    "mActivityTaskManagerService"
                                ),
                                "mWindowManager"
                            ),
                            "mMiuiFreeFormGestureController"
                        );
                        Object mGestureAnimator = getField(
                            getField(
                                mMiuiFreeFormGestureController,
                                "mGestureListener"
                            ),
                            "mGestureAnimator"
                        );
                        callMethod(getField(mffas, "mLastIconLayerWindowToken"), "setVisibility", false);
                        callMethod(mGestureAnimator, "hideStack", mffas);
                        callMethod(mGestureAnimator, "applyTransaction");
                    }
                }
            }
        );

        hookMethod("com.android.server.wm.MiuiMultiWindowSwitchManager",
            "dropToFreeForm",
            new IHook() {
                @Override
                public void before() {
                    logE(TAG, "dropToFreeForm im run");
                }
            }
        );

        hookMethod("com.android.server.wm.MiuiFreeformPinManagerService",
            "updatePinFloatingWindowPos",
            "com.android.server.wm.MiuiFreeFormActivityStack",
            Rect.class, boolean.class,
            new IHook() {
                @Override
                public void after() {
                    if ((boolean) getArgs(2)) {
                        Object mGestureAnimator = getField(
                            getField(
                                getThisField(
                                    "mController"
                                ),
                                "mGestureListener"
                            ),
                            "mGestureAnimator"
                        );
                        // 尽量提升效率
                        callMethod(
                            getField(
                                getArgs(0),
                                "mLastIconLayerWindowToken"
                            ),
                            "setVisibility",
                            false, false
                        );
                        // callMethod(mGestureAnimator, "hideStack", param.args[0]);
                        // callMethod(mGestureAnimator, "applyTransaction");
                    }
                }
            }
        );

        hookMethod("com.android.server.wm.MiuiFreeformTrackManager",
            "trackSmallWindowPinedMoveEvent",
            String.class, String.class, int.class, int.class,
            new IHook() {
                @Override
                public void before() {
                    observeCall();
                    setResult(null);
                }
            }
        );

        hookMethod("com.android.server.wm.MiuiFreeformTrackManager",
            "trackMiniWindowPinedMoveEvent", String.class, String.class, int.class, int.class,
            new IHook() {
                @Override
                public void before() {
                    observeCall();
                    setResult(null);
                }
            }
        );

        hookMethod("com.android.server.wm.MiuiFreeFormGestureAnimator",
            "hideStack",
            "com.android.server.wm.MiuiFreeFormActivityStack",
            new IHook() {
                @Override
                public void before() {
                    logE(TAG, "hideStack: " + getArgs(0));
                    setResult(null);
                }
            }
        );

        hookMethod("com.android.server.wm.MiuiFreeFormGestureAnimator",
            "hide",
            "com.android.server.wm.WindowContainer",
            new IHook() {
                @Override
                public void before() {
                    logE(TAG, "hide: " + getArgs(0));
                }
            }
        );

        hookMethod("com.android.server.wm.MiuiFreeFormGestureAnimator",
            "showStack",
            "com.android.server.wm.MiuiFreeFormActivityStack",
            new IHook() {
                @Override
                public void before() {
                    logE(TAG, "showStack: " + getArgs(0));
                    setResult(null);
                }
            }
        );
    }
}
