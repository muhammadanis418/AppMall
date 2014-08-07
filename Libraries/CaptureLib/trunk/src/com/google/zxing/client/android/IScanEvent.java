package com.google.zxing.client.android;

import com.google.zxing.Result;

public abstract class IScanEvent {
	public abstract void scanCompleted(Result paramScannerResult);
}
