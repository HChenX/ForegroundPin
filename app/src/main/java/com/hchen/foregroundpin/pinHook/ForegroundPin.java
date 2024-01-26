package com.hchen.foregroundpin.pinHook;

import android.util.Log;
import android.view.SurfaceControl;

import com.hchen.foregroundpin.hookMode.Hook;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class ForegroundPin extends Hook {
    @Override
    public void init() {
        try {
            /*Hyper*/
            getDeclaredMethod("com.android.server.wm.MiuiFreeFormGestureController",
                    "needForegroundPin",
                    "com.android.server.wm.MiuiFreeFormActivityStack");
            findAndHookMethod("com.android.server.wm.MiuiFreeFormGestureController",
                    "needForegroundPin",
                    "com.android.server.wm.MiuiFreeFormActivityStack",
                    new HookAction() {
                        @Override
                        protected void before(XC_MethodHook.MethodHookParam param) {
                            param.setResult(true);
                        }
                    }
            );
        } catch (Throwable throwable) {
            logE(tag, "Hyper UnForegroundPin E, if you is Miui don't worry : " + throwable);
            /*Miui*/
            findAndHookMethod("com.android.server.wm.MiuiFreeFormGestureController",
                    "moveTaskToBack",
                    "com.android.server.wm.MiuiFreeFormActivityStack",
                    new HookAction() {
                        @Override
                        protected void before(XC_MethodHook.MethodHookParam param) {
                            Object stack = param.args[0];
                            if (stack != null) {
                                Object mTask = XposedHelpers.getObjectField(stack, "mTask");
                                if (mTask != null) {
                                    SurfaceControl surfaceControl;
                                    if (mTask != null && (surfaceControl =
                                            (SurfaceControl) XposedHelpers.getObjectField(mTask,
                                                    "mSurfaceControl")) != null
                                            && surfaceControl.isValid()) {
                                        SurfaceControl.Transaction transaction = new SurfaceControl.Transaction();
                                        XposedHelpers.callMethod(transaction, "hide", surfaceControl);
                                        XposedHelpers.callMethod(transaction, "apply");
                                        // logE(tag, "moveTaskToBack: s: " + surfaceControl);
                                        param.setResult(null);
                                    }
                                }
                            }
                        }
                    }
            );

            /*findAndHookMethod("com.android.server.wm.MiuiFreeformPinManagerService",
                    "hideStack", "com.android.server.wm.MiuiFreeFormActivityStack",
                    new HookAction() {
                        @Override
                        protected void before(MethodHookParam param) throws Throwable {
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
//                                        XposedHelpers.callMethod(transaction, "setCrop", surfaceControl, null);
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
            );*/

            hookAllConstructors("com.android.server.wm.Task",
                    new HookAction() {
                        @Override
                        protected void after(MethodHookParam param) {
                            XposedHelpers.setAdditionalInstanceField(param.thisObject, "mLastSurfaceVisibility", true);
                            // logE(tag, "Task add mLastSurfaceVisibility");
                        }
                    }
            );

            findAndHookMethod("com.android.server.wm.Task",
                    "prepareSurfaces", new HookAction() {
                        @Override
                        protected void after(MethodHookParam param) {
                            SurfaceControl.Transaction transaction = (SurfaceControl.Transaction) XposedHelpers.callMethod(param.thisObject, "getSyncTransaction");
                            SurfaceControl mSurfaceControl = (SurfaceControl) XposedHelpers.getObjectField(param.thisObject, "mSurfaceControl");
                            // String pkg = (String) XposedHelpers.callMethod(param.thisObject, "getPackageName");
                            // String mCallingPackage = (String) XposedHelpers.getObjectField(param.thisObject, "mCallingPackage");
                            // Object getTopNonFinishingActivity = XposedHelpers.callMethod(param.thisObject, "getTopNonFinishingActivity");
                            // String pkg = null;
                            /*if (getTopNonFinishingActivity != null) {
                                ActivityInfo activityInfo = (ActivityInfo) XposedHelpers.getObjectField(getTopNonFinishingActivity, "info");
                                if (activityInfo != null) {
                                    pkg = activityInfo.applicationInfo.packageName;
                                }
                            }*/
                            int taskId = (int) XposedHelpers.callMethod(param.thisObject, "getRootTaskId");
                            Object mWmService = XposedHelpers.getObjectField(param.thisObject, "mWmService");
                            Object mAtmService = XposedHelpers.getObjectField(mWmService, "mAtmService");
                            Object mMiuiFreeFormManagerService = XposedHelpers.getObjectField(mAtmService, "mMiuiFreeFormManagerService");
                            Object mffs = XposedHelpers.callMethod(mMiuiFreeFormManagerService, "getMiuiFreeFormActivityStack", taskId);
                            boolean isVisible = (boolean) XposedHelpers.callMethod(param.thisObject, "isVisible");
                            boolean isAnimating = (boolean) XposedHelpers.callMethod(param.thisObject, "isAnimating", 7);
                            boolean inPinMode = false;
                            if (mffs != null) {
                                inPinMode = (boolean) XposedHelpers.callMethod(mffs, "inPinMode");
                            }
                            boolean mLastSurfaceVisibility = (boolean) XposedHelpers.getAdditionalInstanceField(param.thisObject, "mLastSurfaceVisibility");
                            if (mSurfaceControl != null && mffs != null && inPinMode) {
                                if (!isAnimating) {
                                    XposedHelpers.callMethod(transaction, "setVisibility", mSurfaceControl, false);
                                    XposedHelpers.setAdditionalInstanceField(param.thisObject, "mLastSurfaceVisibility", false);
                                }
                                // logE(tag, "setVisibility false pkg2: " + pkg + " taskid: " + taskId + " isVisble: " + isVisible
                                // + " an: " + isAnimating + " la: " + mLastSurfaceVisibility);
                            } else if (mSurfaceControl != null && mffs != null && !inPinMode) {
                                if (!mLastSurfaceVisibility) {
                                    XposedHelpers.callMethod(transaction, "setVisibility", mSurfaceControl, true);
                                    XposedHelpers.setAdditionalInstanceField(param.thisObject, "mLastSurfaceVisibility", true);
                                }
                                // logE(tag, "setVisibility true pkg2: " + pkg + " taskid: " + taskId + " isVisble: " + isVisible + " an: " + isAnimating);
                            }
                            // logE(tag, "sur: " + mSurfaceControl + " tra: " + transaction + " pkg: " + pkg + " inpin: " + inPinMode);
                        }
                    }
            );

            /*findAndHookMethod("com.android.server.wm.MiuiFreeFormManagerService",
                    "updatePinFloatingWindowPos",
                    Rect.class, int.class, boolean.class, new HookAction() {
                        @Override
                        protected void before(MethodHookParam param) throws Throwable {
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
            );*/

            findAndHookMethod("com.android.server.wm.MiuiFreeFormGestureController",
                    "moveTaskToFront",
                    "com.android.server.wm.MiuiFreeFormActivityStack",
                    new HookAction() {
                        @Override
                        protected void before(XC_MethodHook.MethodHookParam param) {
                            param.setResult(null);
                        }
                    }
            );

            findAndHookMethod("com.android.server.wm.MiuiFreeformPinManagerService",
                    "lambda$unPinFloatingWindow$0$com-android-server-wm-MiuiFreeformPinManagerService",
                    "com.android.server.wm.MiuiFreeFormActivityStack",
                    float.class, float.class, boolean.class, "com.android.server.wm.DisplayContent",
                    "com.android.server.wm.MiuiFreeFormFloatIconInfo",
                    new HookAction() {
                        @Override
                        protected void before(XC_MethodHook.MethodHookParam param) throws Throwable {
                            Object mffas = param.args[0];
                            Object activityRecord = callMethod(XposedHelpers.getObjectField(mffas, "mTask"),
                                    "getTopNonFinishingActivity");
                            /*遵循安卓日志*/
                            Log.i("MiuiFreeformPinManagerService",
                                    "unPinFloatingWindow mffas: " + mffas + " activityRecord: " + activityRecord);
                            if (activityRecord == null) {
                                param.setResult(null);
                                return;
                            }
                            setObject(mffas, "mEnterVelocityX", param.args[1]);
                            setObject(mffas, "mEnterVelocityY", param.args[2]);
                            setObject(mffas, "mIsEnterClick", param.args[3]);
                            setObject(mffas, "mIsPinFloatingWindowPosInit", false);
                            callMethod(param.thisObject, "setUpMiuiFreeWindowFloatIconAnimation",
                                    mffas, activityRecord, param.args[4], param.args[5]);
                            callMethod(param.thisObject, "startUnPinAnimation", mffas);
                            param.setResult(null);
                        }
                    }
            );

            findAndHookMethod("com.android.server.wm.MiuiFreeFormGestureController",
                    "lambda$startFullscreenFromFreeform$2$com-android-server-wm-MiuiFreeFormGestureController",
                    "com.android.server.wm.MiuiFreeFormActivityStack",
                    new HookAction() {
                        @Override
                        protected void before(MethodHookParam param) throws Throwable {
                            Object mffas = param.args[0];
                            Object mGestureListener = getObjectField(param.thisObject, "mGestureListener");
                            if ((boolean) callMethod(mffas, "isInFreeFormMode")) {
                                callMethod(mGestureListener, "startFullScreenFromFreeFormAnimation", mffas);
                                Object mTrackManager = getObjectField(param.thisObject, "mTrackManager");
                                if (mTrackManager != null) {
                                    callMethod(mTrackManager, "trackSmallWindowPinedQuitEvent",
                                            callMethod(mffas, "getStackPackageName"),
                                            callMethod(mffas, "getApplicationName"),
                                            (long) getObjectField(mffas, "mPinedStartTime") != 0 ?
                                                    ((float) (System.currentTimeMillis() -
                                                            (long) getObjectField(mffas,
                                                                    "mPinedStartTime"))) / 1000.0f : 0.0f
                                    );
                                }
                            } else if ((boolean) callMethod(mffas, "isInMiniFreeFormMode")) {
                                callMethod(mGestureListener, "startFullScreenFromSmallAnimation", mffas);
                                Object mTrackManager = getObjectField(param.thisObject, "mTrackManager");
                                if (mTrackManager != null) {
                                    callMethod(mTrackManager, "trackMiniWindowPinedQuitEvent",
                                            callMethod(mffas, "getStackPackageName"),
                                            callMethod(mffas, "getApplicationName"),
                                            (long) getObjectField(mffas, "mPinedStartTime") != 0 ?
                                                    ((float) (System.currentTimeMillis() -
                                                            (long) getObjectField(mffas,
                                                                    "mPinedStartTime"))) / 1000.0f : 0.0f
                                    );
                                }
                            }
                            setObject(mffas, "mPinedStartTime", 0L);
                            callMethod(mffas, "setInPinMode", false);
                            param.setResult(null);
                        }
                    }
            );

        }
    }
}
