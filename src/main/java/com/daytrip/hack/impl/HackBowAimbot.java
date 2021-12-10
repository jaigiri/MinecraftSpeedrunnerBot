package com.daytrip.hack.impl;

import com.daytrip.event.Events;
import com.daytrip.event.impl.EventClickMouse;
import com.daytrip.event.impl.EventTick;
import com.daytrip.hack.Hack;
import com.daytrip.util.Null;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.LivingEntity;
import org.lwjgl.glfw.GLFW;

import static com.daytrip.util.OldValue.*;

public class HackBowAimbot extends Hack {
	private LivingEntity target;

	public HackBowAimbot() {
		super(GLFW.GLFW_KEY_B, "Bow Aimbot");

		Events.on(EventClickMouse.class, event -> {
			EventClickMouse eventClickMouse = event.as(EventClickMouse.class);

			if(eventClickMouse.button == EventClickMouse.Button.MIDDLE) {
				if(minecraft.pointedEntity instanceof LivingEntity) {
					target = (LivingEntity) minecraft.pointedEntity;
					set(minecraft.mouseHelper, "overridden", true);
					eventClickMouse.cancel();
				}
			}
		});

		Events.on(EventTick.class, event -> {
			if (Null.in(target, minecraft.player) || !enabled) {
				revert(minecraft.mouseHelper, "overridden");
				return;
			}

			minecraft.player.lookAt(EntityAnchorArgument.Type.FEET, target.getPositionVec());
			minecraft.player.rotationPitch += (-0.19183673d * minecraft.player.getDistance(target)) + 0.930612244897957d;
		});
	}
}
