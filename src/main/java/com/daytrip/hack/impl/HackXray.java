package com.daytrip.hack.impl;

import com.daytrip.event.impl.EventRenderBlockModel;
import com.daytrip.event.Events;
import com.daytrip.hack.Hack;
import net.minecraft.block.Block;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.OreBlock;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.Registry;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HackXray extends Hack {
	private final List<Block> shows = new ArrayList<>();

	public HackXray() {
		super(GLFW.GLFW_KEY_X, "X-Ray");

		for(Map.Entry<RegistryKey<Block>, Block> block : Registry.BLOCK.getEntries()) {
			if(block.getValue() instanceof OreBlock) {
				shows.add(block.getValue());
			}
			if(block.getValue() instanceof FlowingFluidBlock) {
				shows.add(block.getValue());
			}
		}

		Events.on(EventRenderBlockModel.class, event -> {
			if(enabled) {
				EventRenderBlockModel eventRenderBlockModel = (EventRenderBlockModel) event;
				eventRenderBlockModel.checkSides = false;
				if(!shows.contains(eventRenderBlockModel.state.getBlock())) eventRenderBlockModel.cancel();
			}
		});
	}

	@Override
	public void enable() {
		super.enable();
		minecraft.worldRenderer.loadRenderers();
	}

	@Override
	public void disable() {
		super.disable();
		minecraft.worldRenderer.loadRenderers();
	}
}
