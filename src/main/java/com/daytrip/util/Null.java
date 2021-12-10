package com.daytrip.util;

public class Null {
	public static boolean in(Object... objects) {
		for(Object o : objects) {
			if (o == null) return true;
		}
		return false;
	}
}
