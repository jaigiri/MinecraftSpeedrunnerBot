package com.daytrip.hack.impl;

import com.daytrip.event.Events;
import com.daytrip.event.impl.*;
import com.daytrip.hack.Bot;
import com.daytrip.util.DeltaTime;
import com.daytrip.util.Null;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;

import static com.daytrip.util.OldValue.revert;
import static com.daytrip.util.OldValue.set;

public class PVPBot extends Bot {
	public PVPBot() {
		super(GLFW.GLFW_KEY_P, "PVP Bot");

		Events.on(EventClickMouse.class, event -> {
			EventClickMouse eventClickMouse = event.as(EventClickMouse.class);

			if(eventClickMouse.button == EventClickMouse.Button.MIDDLE) {
				if(minecraft.pointedEntity instanceof LivingEntity) {
					setTarget((LivingEntity) minecraft.pointedEntity);
				}
			}
		});

		Events.on(EventTick.class, event -> {
			if(Null.in(target, minecraft.player, minecraft.playerController)) return;

			if(target.deathTime > 0.0f) {
				loseTarget();
				return;
			}

			minecraft.player.movementInput.sneaking = false;

			Vector3d targetVector = target.getPositionVec().add(0, target.getEyeHeight(target.getPose()) * 0.85, 0);
			Vector3d attackerVector = minecraft.player.getPositionVec();
			Vector3d lookAtVector = new Vector3d(attackerVector.x + ((targetVector.x - attackerVector.x) / 2), attackerVector.y + ((targetVector.y - attackerVector.y) / 2), attackerVector.z + ((targetVector.z - attackerVector.z) / 2));
			minecraft.player.lookAt(EntityAnchorArgument.Type.EYES, lookAtVector);

			if(minecraft.player.getDistance(target) > 1) {
				minecraft.player.movementInput.moveForward = 1.0f;
			} else {
				minecraft.player.movementInput.moveForward = 0.0f;
			}

			if(target.hurtTime > 5 && !minecraft.player.isSprinting()) {
				minecraft.player.setSprinting(true);
			}

			if(minecraft.player.getCooledAttackStrength(0.5f) >= 0.99f && minecraft.pointedEntity == target) {
				minecraft.playerController.attackEntity(minecraft.player, target);
			}
		});

		Events.on(EventCloseWorld.class, event -> loseTarget());

		Events.on(EventEntityDeath.class, event -> {
			EventEntityDeath eventEntityDeath = event.as(EventEntityDeath.class);
			System.out.println(eventEntityDeath.deadEntity.isServerWorld()); // Logs false, or running on client-side
			if(eventEntityDeath.deadEntity == target) {
				loseTarget();
			}
		});

		Events.on(EventKeypress.class, event -> {
			EventKeypress keypress = event.as(EventKeypress.class);
			if(keypress.keyAction == 0) {
				if(keypress.keyCode == GLFW.GLFW_KEY_ESCAPE && target != null) {
					loseTarget();
					keypress.cancel();
				}
			}
		});
	}

	@Override
	public void setTarget(@Nonnull LivingEntity target) {
		super.setTarget(target);
		if(minecraft.player != null) {
			set(minecraft.player.movementInput, "overridden", true);
			set(minecraft.mouseHelper, "overridden", true);
			minecraft.player.setSprinting(true);
		}
	}

	@Override
	public void loseTarget() {
		super.loseTarget();
		if(minecraft.player != null) {
			revert(minecraft.player.movementInput, "overridden");
			revert(minecraft.mouseHelper, "overridden");
		}
	}
}
