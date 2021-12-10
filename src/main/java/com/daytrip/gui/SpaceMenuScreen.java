package com.daytrip.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;

import java.awt.*;

public class SpaceMenuScreen extends Screen {
	private final StarRenderer starRenderer = new StarRenderer();

	public SpaceMenuScreen() {
		super(new StringTextComponent("Space Client Menu"));
	}

	@Override
	public void init(Minecraft minecraft, int width, int height) {
		starRenderer.resize(width, height, true);
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		fillGradient(matrixStack, 0, 0, this.width, this.height, new Color(0, 0, 0, 50).getRGB(), new Color(24, 24, 24, 50).getRGB());
		starRenderer.draw(3);
		fillGradient(matrixStack, 0, 0, this.width, this.height, new Color(64, 64, 64, 100).getRGB(), new Color(64, 64, 64, 100).getRGB());
		drawCenteredString(matrixStack, font, getTitle(), width / 2, width / 2, 10);
	}

	@Override
	public void resize(Minecraft minecraft, int width, int height) {
		super.resize(minecraft, width, height);
		starRenderer.resize(width, height, true);
	}
}
