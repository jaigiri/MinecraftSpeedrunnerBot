package com.daytrip.util;

import net.minecraft.client.Minecraft;

public class DeltaTime {
	public static float get() {
		return Minecraft.getInstance().getRenderPartialTicks();
	}
}
