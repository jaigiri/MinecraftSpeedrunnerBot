package com.daytrip.event.impl;

import com.daytrip.event.CancellableEvent;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class EventRenderBlockModel extends CancellableEvent {
	public BlockPos pos;
	public BlockState state;
	public boolean checkSides;
}
