package com.daytrip.hack;

import net.minecraft.entity.LivingEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class Bot extends Hack {
	@Nullable
	protected LivingEntity target;

	protected Bot(int keyBind, String name) {
		super(keyBind, name);
	}

	public void setTarget(@Nonnull LivingEntity target) {
		this.target = target;
	}

	public void loseTarget() {
		target = null;
	}
}
