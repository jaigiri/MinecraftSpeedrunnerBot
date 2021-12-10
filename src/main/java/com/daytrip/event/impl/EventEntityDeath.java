package com.daytrip.event.impl;

import com.daytrip.event.Event;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;

import javax.annotation.Nullable;

public class EventEntityDeath extends Event {
	public LivingEntity deadEntity;
	public DamageSource deathSource;
	@Nullable
	public LivingEntity attacker;
}
