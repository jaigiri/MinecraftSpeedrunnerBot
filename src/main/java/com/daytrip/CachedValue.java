package com.daytrip;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class CachedValue {
	private final Map<String, Object> cachedValues = new HashMap<>();

	public <T> T getOrElse(String key, Class<T> type, Callable<T> orElse) {
		try {
			cachedValues.putIfAbsent(key, orElse.call());
			return type.cast(cachedValues.get(key));
		} catch (Exception e) {
			return null;
		}
	}
}
