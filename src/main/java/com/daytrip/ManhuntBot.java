package com.daytrip;

import com.daytrip.event.Events;
import com.daytrip.event.impl.EventTick;
import com.daytrip.pathfinding.Pathfinder;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class ManhuntBot {
	private final Minecraft minecraft;
	private int ticks;

	public ManhuntBot() {
		minecraft = Minecraft.getInstance();
	}

	public void  begin(@Nonnull BlockPos end) {
		if (minecraft.player != null && minecraft.world != null) {
			System.out.println("Got here.");
			List<Pathfinder.PathPoint> path = Pathfinder.findPath(minecraft.world, minecraft.player.getPosition(), end);
			System.out.println("Got here!");
			System.out.println(Arrays.toString(path.toArray()));
			Events.on(EventTick.class, event -> {
				if (!path.isEmpty()) {
					if (ticks > 7) {
						ticks = 0;
						BlockPos pos = path.get(0);
						path.remove(0);
						minecraft.player.sendChatMessage("/teleport @s " + pos.getX() + " ~ " + pos.getZ());
					} else {
						ticks++;
					}
				}
			});
		}
	}
}
