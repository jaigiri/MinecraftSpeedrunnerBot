package com.daytrip.hack.impl;

import com.daytrip.hack.Hack;
import org.lwjgl.glfw.GLFW;
import static com.daytrip.util.OldValue.*;

public class HackNoKnockback extends Hack {
	public HackNoKnockback() {
		super(GLFW.GLFW_KEY_K, "No knockback");
	}

	@Override
	public void enable() {
		super.enable();
		set(minecraft.player, "canTakeKnockback", false);
	}

	@Override
	public void disable() {
		revert(minecraft.player, "canTakeKnockback");
	}
}
