package com.google.zxing.client.android;

import java.util.Collection;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.camera.CameraManager;
import com.google.zxing.client.android.camera.CameraSettings;

public class ScannerRelativeLayout extends RelativeLayout implements
		SurfaceHolder.Callback {
	private static final long BULK_MODE_SCAN_DELAY_MS = 1000L;
	private int cwidth = 0;
	private int cheight = 0;
	private Context context;
	private CameraManager cameraManager;
	private ScannerHandler handler;
	private Result savedResultToShow;
	private boolean hasSurface;
	private Collection<BarcodeFormat> decodeFormats;
	private Map<DecodeHintType, ?> decodeHints;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private BeepManager beepManager;
	private AmbientLightManager ambientLightManager;
	private ViewfinderView viewfinderView;
	private SurfaceView surfaceView;
	private IScanEvent mIScanSuccessListener;

	public ScannerRelativeLayout(Context paramContext) {
		super(paramContext);
		initialization(paramContext);
	}

	public ScannerRelativeLayout(Context paramContext, AttributeSet attrs,
			int paramInt) {
		super(paramContext, attrs, paramInt);
		initialization(paramContext);
	}

	public ScannerRelativeLayout(Context paramContext, AttributeSet attrs) {
		super(paramContext, attrs);
		initialization(paramContext);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	private void initialization(Context paramContext) {
		this.context = paramContext;

		inactivityTimer = new InactivityTimer(context);
		beepManager = new BeepManager(context);
		ambientLightManager = new AmbientLightManager(context);
		cameraManager = new CameraManager(context);

		this.surfaceView = new SurfaceView(context);
		addView(this.surfaceView, new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		this.viewfinderView = new ViewfinderView(context);
		addView(this.viewfinderView, new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		viewfinderView.setCameraManager(cameraManager);
		hasSurface = false;
		handler = null;
		decodeFormats = null;
		characterSet = null;
	}

	public void startScan() {
		SurfaceHolder surfaceHolder = surfaceView.getHolder();

		if (hasSurface) {
			// The activity was paused but not stopped, so the surface still
			// exists. Therefore
			// surfaceCreated() won't be called, so init the camera here.

			initCamera(surfaceHolder);
			if (cameraManager != null && cwidth > 0 && cheight > 0)
				cameraManager.setManualFramingRect(cwidth, cheight);
		} else {
			// Install the callback and wait for surfaceCreated() to init the
			// camera.
			surfaceHolder.addCallback(this);
		}

		resetStatusView();
		beepManager.updatePrefs();
		ambientLightManager.start(cameraManager);
		inactivityTimer.onResume();
		if (handler != null) {
			restartPreviewAfterDelay(0);
		}
	}

	public void pauseScan() {
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		inactivityTimer.onPause();
		ambientLightManager.stop();
		cameraManager.closeDriver();
		if (!hasSurface) {
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
	}

	public void stopScan() {
		inactivityTimer.shutdown();
	}

	ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	CameraManager getCameraManager() {
		return cameraManager;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			cheight = surfaceView.getHeight();
			cwidth = surfaceView.getWidth();
			initCamera(holder);
			cameraManager.setManualFramingRect(cwidth, cheight);
		}

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			throw new IllegalStateException("No SurfaceHolder provided");
		}
		if (cameraManager.isOpen()) {
			return;
		}

		try {
			cameraManager.openDriver(surfaceHolder, cwidth, cheight);
			// Creating the handler starts the preview, which can also throw a
			// RuntimeException.
			if (handler == null) {
				handler = new ScannerHandler(this, decodeFormats, decodeHints,
						characterSet, cameraManager);
			}
			decodeOrStoreSavedBitmap(null, null);
		} catch (Exception e) {
			// Barcode Scanner has seen crashes in the wild of this variety:
			// java.?lang.?RuntimeException: Fail to connect to camera service
			e.printStackTrace();
		}
	}

	private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
		// Bitmap isn't used yet -- will be used soon
		if (handler == null) {
			savedResultToShow = result;
		} else {
			if (result != null) {
				savedResultToShow = result;
			}
			if (savedResultToShow != null) {
				Message message = Message.obtain(handler, IDS.DECODE_SUCCESSED,
						savedResultToShow);
				handler.sendMessage(message);
			}
			savedResultToShow = null;
		}
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}

	public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
		inactivityTimer.onActivity();

		boolean fromLiveScan = barcode != null;
		if (fromLiveScan) {
			// Then not from history, so beep/vibrate and we have an image to
			// draw on
			beepManager.playBeepSoundAndVibrate();
			drawResultPoints(barcode, scaleFactor, rawResult);
		}

		if (fromLiveScan && CameraSettings.isBULKMODE()) {

			// Wait a moment or else it will scan the same barcode continuously
			// about 3 times
			handleDecodeInternally(rawResult, barcode);
			restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
		} else {
			handleDecodeInternally(rawResult, barcode);
		}
	}

	public void restartPreviewAfterDelay(long delayMS) {
		if (handler != null) {
			handler.sendEmptyMessageDelayed(IDS.RESART_PREVIEW, delayMS);
		}
		resetStatusView();
	}

	private void resetStatusView() {
		viewfinderView.setVisibility(View.VISIBLE);
	}

	private void drawResultPoints(Bitmap barcode, float scaleFactor,
			Result rawResult) {
		ResultPoint[] points = rawResult.getResultPoints();
		if (points != null && points.length > 0) {
			Canvas canvas = new Canvas(barcode);
			Paint paint = new Paint();
			paint.setColor(Color.parseColor("#b0000000"));
			if (points.length == 2) {
				paint.setStrokeWidth(4.0f);
				drawLine(canvas, paint, points[0], points[1], scaleFactor);
			} else if (points.length == 4
					&& (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A || rawResult
							.getBarcodeFormat() == BarcodeFormat.EAN_13)) {
				// Hacky special case -- draw two lines, for the barcode and
				// metadata
				drawLine(canvas, paint, points[0], points[1], scaleFactor);
				drawLine(canvas, paint, points[2], points[3], scaleFactor);
			} else {
				paint.setStrokeWidth(10.0f);
				for (ResultPoint point : points) {
					if (point != null) {
						canvas.drawPoint(scaleFactor * point.getX(),
								scaleFactor * point.getY(), paint);
					}
				}
			}
		}
	}

	private static void drawLine(Canvas canvas, Paint paint, ResultPoint a,
			ResultPoint b, float scaleFactor) {
		if (a != null && b != null) {
			canvas.drawLine(scaleFactor * a.getX(), scaleFactor * a.getY(),
					scaleFactor * b.getX(), scaleFactor * b.getY(), paint);
		}
	}

	// Put up our own UI for how to handle the decoded contents.
	private void handleDecodeInternally(Result rawResult, Bitmap barcode) {
		if (this.mIScanSuccessListener != null)
			this.mIScanSuccessListener.scanCompleted(rawResult);
	}

	public void setScanSuccessListener(IScanEvent paramIScanEvent) {
		this.mIScanSuccessListener = paramIScanEvent;
	}
}
