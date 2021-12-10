package com.daytrip.event;

public abstract class CancellableEvent extends Event {
	private boolean isCancelled;

	public void cancel() {
		isCancelled = true;
	}

	public boolean isCancelled() {
		return isCancelled;
	}
}
