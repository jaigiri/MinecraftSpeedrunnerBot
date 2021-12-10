package com.daytrip;

import com.daytrip.event.Events;
import com.daytrip.event.impl.EventKeypress;
import com.daytrip.event.impl.EventRegister;
import com.daytrip.gui.SpaceMenuScreen;
import com.daytrip.hack.HackManager;
import com.daytrip.hack.impl.HackBetterPVP;
import com.daytrip.hack.impl.HackBowAimbot;
import com.daytrip.hack.impl.HackNoKnockback;
import com.daytrip.hack.impl.HackXray;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class SpaceClient {
	private final Logger logger = LogManager.getLogger();

	public SpaceClient() {
		Events.on(EventRegister.class, event -> {
			logger.info("Space client registration beginning...");
			HackManager.init();
			HackManager.register(new HackBetterPVP());
			HackManager.register(new HackXray());
			HackManager.register(new HackNoKnockback());
			// HackManager.register(3, new PVPBot());
			HackManager.register(new HackBowAimbot());
			logger.info("Space client registration success!");
		});

		Events.on(EventKeypress.class, event -> {
			EventKeypress eventKeypress = event.as(EventKeypress.class);
			if (eventKeypress.keyAction == 0) {
				if (eventKeypress.keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
					Minecraft.getInstance().showScreen(new SpaceMenuScreen());
				}
			}
		});

		// 1.16.5 SERVER DOWNLOAD:
		// https://launcher.mojang.com/v1/objects/1b557e7b033b583cd9f66746b7a9ab1ec1673ced/server.jar
	}
}
