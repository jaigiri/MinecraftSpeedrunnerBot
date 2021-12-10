package com.daytrip.event;

public abstract class Event {
	public <T extends Event> T as(Class<T> tClass) {
		return tClass.cast(this);
	}
}
