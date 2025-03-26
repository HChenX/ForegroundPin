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

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.SurfaceControl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hchen.foregroundpin.utils.ModuleData;
import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.hook.IHook;
import com.hchen.hooktool.tool.additional.DeviceTool;

public class ForegroundPin extends BaseHC {
    private final HandlerHelper handlerHelper = new HandlerHelper(Looper.getMainLooper());
    private static final int TOP_WINDOW_HAS_DRAWN = 1;

    @Override
    public void init() {
        if (DeviceTool.isMoreHyperOSVersion(1f)) {
            hookMethod("com.android.server.wm.MiuiFreeFormGestureController",
                "needForegroundPin",
                "com.android.server.wm.MiuiFreeFormActivityStack",
                new IHook() {
                    @Override
                    public void after() {
                        String packageName = getPackageName(getArgs(0));
                        if (ModuleData.shouldForegroundPin(packageName)) {
                            setResult(true);
                            return;
                        }
                        setResult(false);
                    }
                }
            );
        } else {
            hookMethod("com.android.server.wm.MiuiFreeformPinManagerService",
                "lambda$unPinFloatingWindow$0$com-android-server-wm-MiuiFreeformPinManagerService",
                "com.android.server.wm.MiuiFreeFormActivityStack",
                float.class, float.class, boolean.class,
                "com.android.server.wm.DisplayContent",
                "com.android.server.wm.MiuiFreeFormFloatIconInfo",
                new IHook() {
                    @Override
                    public void before() {
                        handlerHelper.sendMessageDelayed(
                            handlerHelper.obtainMessage(
                                TOP_WINDOW_HAS_DRAWN,
                                new Object[]{getArgs(0), thisObject()}
                            ),
                            150
                        );
                    }
                }
            );

            chain("com.android.server.wm.MiuiFreeFormGestureController",
                method("moveTaskToBack", "com.android.server.wm.MiuiFreeFormActivityStack")
                    .hook(
                        new IHook() {
                            @Override
                            public void before() {
                                if (ModuleData.shouldForegroundPin(getPackageName(getArgs(0)))) {
                                    setResult(null);
                                }
                            }
                        }
                    )

                    .method("moveTaskToFront", "com.android.server.wm.MiuiFreeFormActivityStack")
                    .hook(new IHook() {
                        @Override
                        public void before() {
                            if (ModuleData.shouldForegroundPin(getPackageName(getArgs(0)))) {
                                setResult(null);
                            }
                        }
                    })

                    .method("lambda$startFullscreenFromFreeform$2$com-android-server-wm-MiuiFreeFormGestureController",
                        "com.android.server.wm.MiuiFreeFormActivityStack")
                    .hook(new IHook() {
                        @Override
                        public void before() {
                            handlerHelper.sendMessageDelayed(
                                handlerHelper.obtainMessage(
                                    TOP_WINDOW_HAS_DRAWN,
                                    new Object[]{getArgs(0), thisObject()}
                                ),
                                150
                            );
                        }
                    })
            );


            chain("com.android.server.wm.Task",
                anyConstructor()
                    .hook(new IHook() {
                        @Override
                        public void after() {
                            setThisAdditionalInstanceField("mLastSurfaceVisibility", false);
                        }
                    })

                    .method("prepareSurfaces")
                    .hook(new IHook() {
                        @Override
                        public void after() {
                            String packageName = (String) callThisMethod("getPackageName");
                            if (!ModuleData.shouldForegroundPin(packageName))
                                return;

                            SurfaceControl.Transaction transaction = (SurfaceControl.Transaction) callThisMethod("getSyncTransaction");
                            SurfaceControl mSurfaceControl = (SurfaceControl) getThisField("mSurfaceControl");
                            Object mffas = callMethod(
                                getField(
                                    getField(
                                        getThisField("mWmService"),
                                        "mAtmService"
                                    ),
                                    "mMiuiFreeFormManagerService"
                                ),
                                "getMiuiFreeFormActivityStack",
                                callThisMethod("getRootTaskId")
                            );

                            boolean isVisible = (boolean) callThisMethod("isVisible");
                            boolean isAnimating = (boolean) callThisMethod("isAnimating", 7);
                            boolean inPinMode = mffas != null && (boolean) callMethod(mffas, "inPinMode");
                            boolean mLastSurfaceVisibility = (boolean) getThisAdditionalInstanceField("mLastSurfaceVisibility");

                            if (mSurfaceControl != null && mffas != null && inPinMode) {
                                if (!isAnimating) {
                                    callMethod(transaction, "setVisibility", mSurfaceControl, false);
                                    setThisAdditionalInstanceField("mLastSurfaceVisibility", false);
                                }
                            } else if (mSurfaceControl != null && mffas != null && !inPinMode) {
                                if (!mLastSurfaceVisibility) {
                                    callMethod(transaction, "setVisibility", mSurfaceControl, true);
                                    setThisAdditionalInstanceField("mLastSurfaceVisibility", true);
                                }
                            }
                        }
                    })
            );
        }
    }

    @Nullable
    private String getPackageName(Object mffas) {
        if (mffas != null)
            return (String) callMethod(mffas, "getStackPackageName");

        return null;
    }

    private static class HandlerHelper extends Handler {
        public HandlerHelper(@NonNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == TOP_WINDOW_HAS_DRAWN) {
                Object[] objs = (Object[]) msg.obj;
                Object mffas = objs[0];
                Object mLock = getField(objs[1], "mLock");
                setField(mffas, "topWindowHasDrawn", true);

                try {
                    if (mLock == null) {
                        Object mMiuiFreeformPinManagerService = getField(objs[1], "mMiuiFreeformPinManagerService");
                        mLock = getField(mMiuiFreeformPinManagerService, "mLock");
                        if (mLock != null) {
                            synchronized (mLock) {
                                mLock.notifyAll();
                            }
                        }
                    } else {
                        synchronized (mLock) {
                            mLock.notifyAll();
                        }
                    }
                } catch (Throwable e) {
                    logE("ForegroundPin", e);
                }
            }
        }
    }
}
