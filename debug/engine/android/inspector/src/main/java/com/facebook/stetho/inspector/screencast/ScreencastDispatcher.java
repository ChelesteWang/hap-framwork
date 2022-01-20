/*
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.stetho.inspector.screencast;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.view.View;
import com.facebook.stetho.common.LogUtil;
import com.facebook.stetho.inspector.elements.android.ActivityTracker;
import com.facebook.stetho.inspector.jsonrpc.DisconnectReceiver;
import com.facebook.stetho.inspector.jsonrpc.JsonRpcPeer;
import com.facebook.stetho.inspector.protocol.module.Page;
import java.io.ByteArrayOutputStream;
import org.hapjs.debug.log.DebuggerLogUtil;

public final class ScreencastDispatcher {
    private static final long FRAME_DELAY = 200L;

    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private final BitmapFetchRunnable mBitmapFetchRunnable = new BitmapFetchRunnable();
    private final ActivityTracker mActivityTracker = ActivityTracker.get();
    private final EventDispatchRunnable mEventDispatchRunnable = new EventDispatchRunnable();
    private final RectF mTempSrc = new RectF();
    private final RectF mTempDst = new RectF();

    private boolean mIsRunning;
    private Handler mBackgroundHandler;
    private JsonRpcPeer mPeer;
    private HandlerThread mHandlerThread;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Page.StartScreencastRequest mRequest;
    private ByteArrayOutputStream mStream;
    private Page.ScreencastFrameEvent mEvent = new Page.ScreencastFrameEvent();
    private Page.ScreencastFrameEventMetadata mMetadata = new Page.ScreencastFrameEventMetadata();

    private DisconnectReceiver mDisconnectReceiver;

    public ScreencastDispatcher() {
    }

    public void startScreencast(JsonRpcPeer peer, Page.StartScreencastRequest request) {
        LogUtil.d("Starting screencast");
        mRequest = request;
        mHandlerThread = new HandlerThread("Screencast Thread");
        mHandlerThread.start();
        mPeer = peer;
        mIsRunning = true;
        mStream = new ByteArrayOutputStream();
        mBackgroundHandler = new Handler(mHandlerThread.getLooper());
        mMainHandler.postDelayed(mBitmapFetchRunnable, FRAME_DELAY);
        mDisconnectReceiver =
                new DisconnectReceiver() {
                    @Override
                    public void onDisconnect() {
                        stopScreencast();
                    }
                };
        mPeer.registerDisconnectReceiver(mDisconnectReceiver);
    }

    public void stopScreencast() {
        LogUtil.d("Stopping screencast");
        mBackgroundHandler.post(new CancellationRunnable());
    }

    private class BitmapFetchRunnable implements Runnable {
        @Override
        public void run() {
            updateScreenBitmap();
            mBackgroundHandler.post(mEventDispatchRunnable.withEndAction(this));
        }

        private void updateScreenBitmap() {
            if (!mIsRunning) {
                return;
            }
            Activity activity = mActivityTracker.tryGetTopActivity();
            if (activity == null) {
                return;
            }
            // This stuff needs to happen in the UI thread
            View rootView = activity.getWindow().getDecorView();
            try {
                if (mBitmap == null) {
                    int viewWidth = rootView.getWidth();
                    int viewHeight = rootView.getHeight();
                    float scale =
                            Math.min(
                                    (float) mRequest.maxWidth / (float) viewWidth,
                                    (float) mRequest.maxHeight / (float) viewHeight);
                    int destWidth = (int) (viewWidth * scale);
                    int destHeight = (int) (viewHeight * scale);
                    mBitmap = Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.RGB_565);
                    mCanvas = new Canvas(mBitmap);
                    Matrix matrix = new Matrix();
                    mTempSrc.set(0, 0, viewWidth, viewHeight);
                    mTempDst.set(0, 0, destWidth, destHeight);
                    matrix.setRectToRect(mTempSrc, mTempDst, Matrix.ScaleToFit.CENTER);
                    mCanvas.setMatrix(matrix);
                }
                rootView.draw(mCanvas);
            } catch (OutOfMemoryError e) {
                LogUtil.w("Out of memory trying to allocate screencast Bitmap.");
                DebuggerLogUtil.logMessage("ENGINE_SCREENSHOT_ERROR");
                DebuggerLogUtil.logException(e);
            }
        }
    }

    private class EventDispatchRunnable implements Runnable {
        private Runnable mEndAction;

        private EventDispatchRunnable withEndAction(Runnable endAction) {
            mEndAction = endAction;
            return this;
        }

        @Override
        public void run() {
            if (!mIsRunning || mBitmap == null) {
                return;
            }
            int width = mBitmap.getWidth();
            int height = mBitmap.getHeight();
            mStream.reset();
            Base64OutputStream base64Stream = new Base64OutputStream(mStream, Base64.DEFAULT);
            // request format is either "jpeg" or "png"
            Bitmap.CompressFormat format =
                    Bitmap.CompressFormat.valueOf(mRequest.format.toUpperCase());
            mBitmap.compress(format, mRequest.quality, base64Stream);
            mEvent.data = mStream.toString();
            mMetadata.pageScaleFactor = 1;
            mMetadata.deviceWidth = width;
            mMetadata.deviceHeight = height;
            mEvent.metadata = mMetadata;
            mPeer.invokeMethod("Page.screencastFrame", mEvent, null);
            mMainHandler.postDelayed(mEndAction, FRAME_DELAY);
        }
    }

    private class CancellationRunnable implements Runnable {
        @Override
        public void run() {
            if (!mIsRunning) {
                return;
            }

            mHandlerThread.interrupt();
            mMainHandler.removeCallbacks(mBitmapFetchRunnable);
            mBackgroundHandler.removeCallbacks(mEventDispatchRunnable);
            mPeer.unregisterDisconnectReceiver(mDisconnectReceiver);
            mIsRunning = false;
            mHandlerThread = null;
            mBitmap = null;
            mCanvas = null;
            mStream = null;
            mDisconnectReceiver = null;
        }
    }
}
