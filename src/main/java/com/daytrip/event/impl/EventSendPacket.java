package com.daytrip.event.impl;

import com.daytrip.event.CancellableEvent;
import net.minecraft.network.IPacket;

public class EventSendPacket extends CancellableEvent {
	public IPacket<?> packet;
}
