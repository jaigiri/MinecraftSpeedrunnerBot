package com.daytrip.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Events {
	private static final Map<Class<? extends Event>, List<EventConsumer>> eventMap = new HashMap<>();

	public static void on(Class<? extends Event> eventClass, EventConsumer listener) {
		eventMap.putIfAbsent(eventClass, new ArrayList<>());
		eventMap.get(eventClass).add(listener);
	}

	public static void post(Event event) {
		if(eventMap.containsKey(event.getClass())) {
			for(EventConsumer listener : eventMap.get(event.getClass())) {
				if(event instanceof CancellableEvent && ((CancellableEvent) event).isCancelled()) {
					break;
				}
				listener.consume(event);
			}
		}
	}

	public interface EventConsumer {
		void consume(Event event);
	}
}
