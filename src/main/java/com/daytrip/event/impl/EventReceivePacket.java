package com.daytrip.event.impl;

import com.daytrip.event.CancellableEvent;
import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;

public class EventReceivePacket extends CancellableEvent {
	public IPacket<?> packet;
	public INetHandler handler;
}
