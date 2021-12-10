package com.daytrip.hack.impl;

import com.daytrip.hack.Hack;
import org.lwjgl.glfw.GLFW;

import static com.daytrip.util.OldValue.revert;
import static com.daytrip.util.OldValue.set;

public class HackBetterPVP extends Hack {
	public HackBetterPVP() {
		super(GLFW.GLFW_KEY_H, "Better PVP");
	}

	@Override
	public void enable() {
		super.enable();
		set(minecraft.player, "stopSprintingAfterHit", false);
		set(minecraft.player, "slowDownAfterHit", false);
		set(minecraft.player, "canTakeKnockback", false);
	}

	@Override
	public void disable() {
		super.disable();
		revert(minecraft.player, "stopSprintingAfterHit");
		revert(minecraft.player, "slowDownAfterHit");
		revert(minecraft.player, "canTakeKnockback");
	}
}
