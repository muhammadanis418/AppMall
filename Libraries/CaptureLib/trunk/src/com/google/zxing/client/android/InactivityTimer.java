package com.google.zxing.client.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.BatteryManager;

/**
 * Finishes an activity after a period of inactivity if the device is on battery
 * power.
 */
final class InactivityTimer {
	private static final long INACTIVITY_DELAY_MS = 5 * 60 * 1000L;
	private final Context context;
	private final BroadcastReceiver powerStatusReceiver;
	private boolean registered;
	private AsyncTask<?, ?, ?> inactivityTask;

	InactivityTimer(Context context) {
		this.context = context;
		powerStatusReceiver = new PowerStatusReceiver();
		registered = false;
		onActivity();
	}

	@SuppressWarnings("unchecked")
	synchronized void onActivity() {
		cancel();
		inactivityTask = new InactivityAsyncTask();
		inactivityTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	public synchronized void onPause() {
		cancel();
		if (registered) {
			context.unregisterReceiver(powerStatusReceiver);
			registered = false;
		}
	}

	public synchronized void onResume() {
		if (!registered) {
			context.registerReceiver(powerStatusReceiver, new IntentFilter(
					Intent.ACTION_BATTERY_CHANGED));
			registered = true;
		}
		onActivity();
	}

	private synchronized void cancel() {
		AsyncTask<?, ?, ?> task = inactivityTask;
		if (task != null) {
			task.cancel(true);
			inactivityTask = null;
		}
	}

	void shutdown() {
		cancel();
	}

	private final class PowerStatusReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
				// 0 indicates that we're on battery
				boolean onBatteryNow = intent.getIntExtra(
						BatteryManager.EXTRA_PLUGGED, -1) <= 0;
				if (onBatteryNow) {
					InactivityTimer.this.onActivity();
				} else {
					InactivityTimer.this.cancel();
				}
			}
		}
	}

	private final class InactivityAsyncTask extends
			AsyncTask<Object, Object, Object> {
		@Override
		protected Object doInBackground(Object... objects) {
			try {
				Thread.sleep(INACTIVITY_DELAY_MS);
//				Thread.yield();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}
