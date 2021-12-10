package com.daytrip.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StarRenderer {
	private final List<Rectangle2d> stars = new ArrayList<>();
	private final Random random = new Random();
	private int width;
	private int height;

	public void resize(int width, int height, boolean regenerate) {
		this.width = width;
		this.height = height;
		if(regenerate) {
			generate();
		}
	}

	public void generate() {
		generate(500);
	}

	public void generate(int starCount) {
		for (int i = 0; i < starCount; i++) {
			stars.add(new Rectangle2d(random.nextInt(width), random.nextInt(height), 2 + random.nextInt(3), 2 + random.nextInt(3)));
		}
	}

	public void draw(int moveBy) {
		int r = 255, g = 255, b = 255, a = 255;

		for (Rectangle2d star : stars) {
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
			bufferbuilder.pos(star.getX(), star.getY(), 0.0D).color(r, g, b, a).endVertex();
			bufferbuilder.pos(star.getX() + star.getWidth(), star.getY(), 0.0D).color(r, g, b, a).endVertex();
			bufferbuilder.pos(star.getX() + star.getWidth(), star.getY() + star.getHeight(), 0.0D).color(r, g, b, a).endVertex();
			bufferbuilder.pos(star.getX(), star.getY() + star.getHeight(), 0.0D).color(r, g, b, a).endVertex();
			tessellator.draw();
			if(star.getY() < -10) {
				star.setY(height + 10);
			} else {
				star.setY(star.getY() - moveBy);
			}
		}
	}
}
