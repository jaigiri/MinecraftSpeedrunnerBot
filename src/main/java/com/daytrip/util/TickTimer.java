package com.daytrip.util;

import com.daytrip.event.Event;
import com.daytrip.event.Events;
import com.daytrip.event.impl.EventTick;

public class TickTimer {
	private final EmptyCallbackFunction callbackFunction;
	private final boolean repeats;
	private final boolean autoReset;
	private final boolean autoStop;
	private final int targetTicks;

	private int currentTicks;
	private boolean canRun;

	public TickTimer(boolean repeats, int targetTicks, boolean runOnOwn, boolean autoReset, boolean autoStop, EmptyCallbackFunction callbackFunction) {
		this.callbackFunction = callbackFunction;
		this.repeats = repeats;
		this.autoReset = autoReset;
		this.autoStop = autoStop;
		this.targetTicks = targetTicks;

		if(runOnOwn) {
			Events.on(EventTick.class, this::tick);
		}
	}

	public void tick(Event ignored) {
		if(canRun) {
			if(currentTicks >= targetTicks) {
				callbackFunction.call();
				if(autoReset) {
					reset();
				}
				if(!repeats && autoStop) {
					stop();
				}
			} else {
				currentTicks++;
			}
		}
	}

	public void reset() {
		currentTicks = 0;
	}

	public void start() {
		canRun = true;
	}

	public void stop() {
		canRun = false;
	}
}
