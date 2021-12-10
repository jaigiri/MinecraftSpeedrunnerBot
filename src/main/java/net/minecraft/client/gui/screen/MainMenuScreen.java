package net.minecraft.client.gui.screen;

import com.google.common.util.concurrent.Runnables;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.AccessibilityScreen;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.toasts.SystemToast;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.renderer.RenderSkybox;
import net.minecraft.client.renderer.RenderSkyboxCube;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.realms.RealmsBridgeScreen;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraft.world.storage.SaveFormat;
import net.minecraft.world.storage.WorldSummary;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@OnlyIn(Dist.CLIENT)
public class MainMenuScreen extends Screen {
	public static final RenderSkyboxCube PANORAMA_RESOURCES = new RenderSkyboxCube(new ResourceLocation("textures/gui/title/background/panorama"));
	private static final Logger field_238656_b_ = LogManager.getLogger();
	private static final ResourceLocation PANORAMA_OVERLAY_TEXTURES = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
	private static final ResourceLocation ACCESSIBILITY_TEXTURES = new ResourceLocation("textures/gui/accessibility.png");
	private static final ResourceLocation MINECRAFT_TITLE_TEXTURES = new ResourceLocation("textures/gui/title/minecraft.png");
	private static final ResourceLocation MINECRAFT_TITLE_EDITION = new ResourceLocation("textures/gui/title/edition.png");
	private final boolean showTitleWronglySpelled;
	private final RenderSkybox panorama = new RenderSkybox(PANORAMA_RESOURCES);
	private final boolean showFadeInAnimation;
	@Nullable
	private String splashText;
	private Button buttonResetDemo;
	private boolean hasCheckedForRealmsNotification;
	private Screen realmsNotification;
	private int widthCopyright;
	private int widthCopyrightRest;
	private long firstRenderTime;

	public MainMenuScreen() {
		this(false);
	}

	public MainMenuScreen(boolean fadeIn) {
		super(new TranslationTextComponent("narrator.screen.title"));
		this.showFadeInAnimation = fadeIn;
		this.showTitleWronglySpelled = (double) (new Random()).nextFloat() < 1.0E-4D;
	}

	public static CompletableFuture<Void> loadAsync(TextureManager texMngr, Executor backgroundExecutor) {
		return CompletableFuture.allOf(texMngr.loadAsync(MINECRAFT_TITLE_TEXTURES, backgroundExecutor), texMngr.loadAsync(MINECRAFT_TITLE_EDITION, backgroundExecutor), texMngr.loadAsync(PANORAMA_OVERLAY_TEXTURES, backgroundExecutor), PANORAMA_RESOURCES.loadAsync(texMngr, backgroundExecutor));
	}

	private boolean areRealmsNotificationsEnabled() {
		return this.minecraft.gameSettings.realmsNotifications && this.realmsNotification != null;
	}

	public void tick() {
		if (this.areRealmsNotificationsEnabled()) {
			this.realmsNotification.tick();
		}

	}

	public boolean isPauseScreen() {
		return false;
	}

	public boolean shouldCloseOnEsc() {
		return false;
	}

	protected void init() {
		if (this.splashText == null) {
			this.splashText = this.minecraft.getSplashes().getSplashText();
		}

		this.widthCopyright = this.font.getStringWidth("Copyright Mojang AB. Do not distribute!");
		this.widthCopyrightRest = this.width - this.widthCopyright - 2;
		int j = this.height / 4 + 48;
		if (this.minecraft.isDemo()) {
			this.addDemoButtons(j, 24);
		} else {
			this.addSingleplayerMultiplayerButtons(j, 24);
		}

		this.addButton(new ImageButton(this.width / 2 - 124, j + 72 + 12, 20, 20, 0, 106, 20, Button.WIDGETS_LOCATION, 256, 256, (p_213090_1_) -> this.minecraft.showScreen(new LanguageScreen(this, this.minecraft.gameSettings, this.minecraft.getLanguageManager())), new TranslationTextComponent("narrator.button.language")));
		this.addButton(new Button(this.width / 2 - 100, j + 72 + 12, 98, 20, new TranslationTextComponent("menu.options"), (p_213096_1_) -> {
			this.minecraft.showScreen(new OptionsScreen(this, this.minecraft.gameSettings));
		}));
		this.addButton(new Button(this.width / 2 + 2, j + 72 + 12, 98, 20, new TranslationTextComponent("menu.quit"), (p_213094_1_) -> {
			this.minecraft.shutdown();
		}));
		this.addButton(new ImageButton(this.width / 2 + 104, j + 72 + 12, 20, 20, 0, 0, 20, ACCESSIBILITY_TEXTURES, 32, 64, (p_213088_1_) -> this.minecraft.showScreen(new AccessibilityScreen(this, this.minecraft.gameSettings)), new TranslationTextComponent("narrator.button.accessibility")));
		this.minecraft.setConnectedToRealms(false);
		if (this.minecraft.gameSettings.realmsNotifications && !this.hasCheckedForRealmsNotification) {
			RealmsBridgeScreen realmsbridgescreen = new RealmsBridgeScreen();
			this.realmsNotification = realmsbridgescreen.func_239555_b_(this);
			this.hasCheckedForRealmsNotification = true;
		}

		if (this.areRealmsNotificationsEnabled()) {
			this.realmsNotification.init(this.minecraft, this.width, this.height);
		}

	}

	private void addSingleplayerMultiplayerButtons(int yIn, int rowHeightIn) {
		this.addButton(new Button(this.width / 2 - 100, yIn, 200, 20, new TranslationTextComponent("menu.singleplayer"), (p_213089_1_) -> {
			this.minecraft.showScreen(new WorldSelectionScreen(this));
		}));
		boolean flag = this.minecraft.isMultiplayerEnabled();
		Button.ITooltip button$itooltip = flag ? Button.field_238486_s_ : (p_238659_1_, p_238659_2_, p_238659_3_, p_238659_4_) -> {
			if (!p_238659_1_.active) {
				this.renderTooltip(p_238659_2_, this.minecraft.fontRenderer.trimStringToWidth(new TranslationTextComponent("title.multiplayer.disabled"), Math.max(this.width / 2 - 43, 170)), p_238659_3_, p_238659_4_);
			}

		};
		(this.addButton(new Button(this.width / 2 - 100, yIn + rowHeightIn * 1, 200, 20, new TranslationTextComponent("menu.multiplayer"), (p_213095_1_) -> {
			Screen screen = this.minecraft.gameSettings.skipMultiplayerWarning ? new MultiplayerScreen(this) : new MultiplayerWarningScreen(this);
			this.minecraft.showScreen(screen);
		}, button$itooltip))).active = flag;
		(this.addButton(new Button(this.width / 2 - 100, yIn + rowHeightIn * 2, 200, 20, new TranslationTextComponent("menu.online"), (p_238661_1_) -> {
			this.switchToRealms();
		}, button$itooltip))).active = flag;
	}

	private void addDemoButtons(int yIn, int rowHeightIn) {
		boolean flag = this.func_243319_k();
		this.addButton(new Button(this.width / 2 - 100, yIn, 200, 20, new TranslationTextComponent("menu.playdemo"), (p_213091_2_) -> {
			if (flag) {
				this.minecraft.loadWorld("Demo_World");
			} else {
				DynamicRegistries.Impl dynamicregistries$impl = DynamicRegistries.func_239770_b_();
				this.minecraft.createWorld("Demo_World", MinecraftServer.DEMO_WORLD_SETTINGS, dynamicregistries$impl, DimensionGeneratorSettings.func_242752_a(dynamicregistries$impl));
			}

		}));
		this.buttonResetDemo = this.addButton(new Button(this.width / 2 - 100, yIn + rowHeightIn * 1, 200, 20, new TranslationTextComponent("menu.resetdemo"), (p_238658_1_) -> {
			SaveFormat saveformat = this.minecraft.getSaveLoader();

			try (SaveFormat.LevelSave saveformat$levelsave = saveformat.getLevelSave("Demo_World")) {
				WorldSummary worldsummary = saveformat$levelsave.readWorldSummary();
				if (worldsummary != null) {
					this.minecraft.showScreen(new ConfirmScreen(this::deleteDemoWorld, new TranslationTextComponent("selectWorld.deleteQuestion"), new TranslationTextComponent("selectWorld.deleteWarning", worldsummary.getDisplayName()), new TranslationTextComponent("selectWorld.deleteButton"), DialogTexts.GUI_CANCEL));
				}
			} catch (IOException ioexception) {
				SystemToast.func_238535_a_(this.minecraft, "Demo_World");
				field_238656_b_.warn("Failed to access demo world", ioexception);
			}

		}));
		this.buttonResetDemo.active = flag;
	}

	private boolean func_243319_k() {
		try (SaveFormat.LevelSave saveformat$levelsave = this.minecraft.getSaveLoader().getLevelSave("Demo_World")) {
			return saveformat$levelsave.readWorldSummary() != null;
		} catch (IOException ioexception) {
			SystemToast.func_238535_a_(this.minecraft, "Demo_World");
			field_238656_b_.warn("Failed to read demo world data", ioexception);
			return false;
		}
	}

	private void switchToRealms() {
		RealmsBridgeScreen realmsbridgescreen = new RealmsBridgeScreen();
		realmsbridgescreen.func_231394_a_(this);
	}

	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (this.firstRenderTime == 0L && this.showFadeInAnimation) {
			this.firstRenderTime = Util.milliTime();
		}

		float f = this.showFadeInAnimation ? (float) (Util.milliTime() - this.firstRenderTime) / 1000.0F : 1.0F;
		fill(matrixStack, 0, 0, this.width, this.height, -1);
		this.panorama.render(partialTicks, MathHelper.clamp(f, 0.0F, 1.0F));
		int i = 274;
		int j = this.width / 2 - 137;
		int k = 30;
		this.minecraft.getTextureManager().bindTexture(PANORAMA_OVERLAY_TEXTURES);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.showFadeInAnimation ? (float) MathHelper.ceil(MathHelper.clamp(f, 0.0F, 1.0F)) : 1.0F);
		blit(matrixStack, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
		float f1 = this.showFadeInAnimation ? MathHelper.clamp(f - 1.0F, 0.0F, 1.0F) : 1.0F;
		int l = MathHelper.ceil(f1 * 255.0F) << 24;
		if ((l & -67108864) != 0) {
			this.minecraft.getTextureManager().bindTexture(MINECRAFT_TITLE_TEXTURES);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, f1);
			if (this.showTitleWronglySpelled) {
				this.blitBlackOutline(j, 30, (p_238660_2_, p_238660_3_) -> {
					this.blit(matrixStack, p_238660_2_ + 0, p_238660_3_, 0, 0, 99, 44);
					this.blit(matrixStack, p_238660_2_ + 99, p_238660_3_, 129, 0, 27, 44);
					this.blit(matrixStack, p_238660_2_ + 99 + 26, p_238660_3_, 126, 0, 3, 44);
					this.blit(matrixStack, p_238660_2_ + 99 + 26 + 3, p_238660_3_, 99, 0, 26, 44);
					this.blit(matrixStack, p_238660_2_ + 155, p_238660_3_, 0, 45, 155, 44);
				});
			} else {
				this.blitBlackOutline(j, 30, (p_238657_2_, p_238657_3_) -> {
					this.blit(matrixStack, p_238657_2_ + 0, p_238657_3_, 0, 0, 155, 44);
					this.blit(matrixStack, p_238657_2_ + 155, p_238657_3_, 0, 45, 155, 44);
				});
			}

			this.minecraft.getTextureManager().bindTexture(MINECRAFT_TITLE_EDITION);
			blit(matrixStack, j + 88, 67, 0.0F, 0.0F, 98, 14, 128, 16);
			if (this.splashText != null) {
				RenderSystem.pushMatrix();
				RenderSystem.translatef((float) (this.width / 2 + 90), 70.0F, 0.0F);
				RenderSystem.rotatef(-20.0F, 0.0F, 0.0F, 1.0F);
				float f2 = 1.8F - MathHelper.abs(MathHelper.sin((float) (Util.milliTime() % 1000L) / 1000.0F * ((float) Math.PI * 2F)) * 0.1F);
				f2 = f2 * 100.0F / (float) (this.font.getStringWidth(this.splashText) + 32);
				RenderSystem.scalef(f2, f2, f2);
				drawCenteredString(matrixStack, this.font, this.splashText, 0, -8, 16776960 | l);
				RenderSystem.popMatrix();
			}

			String s = "Minecraft " + SharedConstants.getVersion().getName();
			if (this.minecraft.isDemo()) {
				s = s + " Demo";
			} else {
				s = s + ("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType());
			}

			if (this.minecraft.isModdedClient()) {
				s = s + I18n.format("menu.modded");
			}

			drawString(matrixStack, this.font, s, 2, this.height - 10, 16777215 | l);
			drawString(matrixStack, this.font, "Copyright Mojang AB. Do not distribute!", this.widthCopyrightRest, this.height - 10, 16777215 | l);
			if (mouseX > this.widthCopyrightRest && mouseX < this.widthCopyrightRest + this.widthCopyright && mouseY > this.height - 10 && mouseY < this.height) {
				fill(matrixStack, this.widthCopyrightRest, this.height - 1, this.widthCopyrightRest + this.widthCopyright, this.height, 16777215 | l);
			}

			for (Widget widget : this.buttons) {
				widget.setAlpha(f1);
			}

			super.render(matrixStack, mouseX, mouseY, partialTicks);
			if (this.areRealmsNotificationsEnabled() && f1 >= 1.0F) {
				this.realmsNotification.render(matrixStack, mouseX, mouseY, partialTicks);
			}

		}
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (super.mouseClicked(mouseX, mouseY, button)) {
			return true;
		} else if (this.areRealmsNotificationsEnabled() && this.realmsNotification.mouseClicked(mouseX, mouseY, button)) {
			return true;
		} else {
			if (mouseX > (double) this.widthCopyrightRest && mouseX < (double) (this.widthCopyrightRest + this.widthCopyright) && mouseY > (double) (this.height - 10) && mouseY < (double) this.height) {
				this.minecraft.showScreen(new WinGameScreen(false, Runnables.doNothing()));
			}

			return false;
		}
	}

	public void onClose() {
		if (this.realmsNotification != null) {
			this.realmsNotification.onClose();
		}

	}

	private void deleteDemoWorld(boolean p_213087_1_) {
		if (p_213087_1_) {
			try (SaveFormat.LevelSave saveformat$levelsave = this.minecraft.getSaveLoader().getLevelSave("Demo_World")) {
				saveformat$levelsave.deleteSave();
			} catch (IOException ioexception) {
				SystemToast.func_238538_b_(this.minecraft, "Demo_World");
				field_238656_b_.warn("Failed to delete demo world", ioexception);
			}
		}

		this.minecraft.showScreen(this);
	}
}