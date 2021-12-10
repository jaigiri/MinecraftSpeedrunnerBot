package com.daytrip.util;

import java.util.HashMap;
import java.util.Map;

public class OldValue {
	private static final Map<Object, Map<String, Object>> olds = new HashMap<>();

	public static void set(Object o, String fieldName, Object newVal) {
		try {
			olds.putIfAbsent(o, new HashMap<>());
			olds.get(o).putIfAbsent(fieldName, o.getClass().getField(fieldName).get(o));
			o.getClass().getField(fieldName).set(o, newVal);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void revert(Object o, String fieldName) {
		try {
			if(!olds.containsKey(o)) {
				return;
			}
			if(!olds.get(o).containsKey(fieldName)) {
				return;
			}
			o.getClass().getField(fieldName).set(o, olds.get(o).get(fieldName));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
