package net.minecraft.client.entity.player;

import com.daytrip.ManhuntBot;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.*;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.util.ClientRecipeBook;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IJumpingMount;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.play.client.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class ClientPlayerEntity extends AbstractClientPlayerEntity {
	public final ClientPlayNetHandler connection;
	protected final Minecraft mc;
	private final StatisticsManager stats;
	private final ClientRecipeBook recipeBook;
	private final List<IAmbientSoundHandler> ambientSoundHandlers = Lists.newArrayList();
	public MovementInput movementInput;
	public float renderArmYaw;
	public float renderArmPitch;
	public float prevRenderArmYaw;
	public float prevRenderArmPitch;
	public float timeInPortal;
	public float prevTimeInPortal;
	protected int sprintToggleTimer;
	private int permissionLevel = 0;
	private double lastReportedPosX;
	private double lastReportedPosY;
	private double lastReportedPosZ;
	private float lastReportedYaw;
	private float lastReportedPitch;
	private boolean prevOnGround;
	private boolean isCrouching;
	private boolean clientSneakState;
	private boolean serverSprintState;
	private int positionUpdateTicks;
	private boolean hasValidHealth;
	private String serverBrand;
	private int horseJumpPowerCounter;
	private float horseJumpPower;
	private boolean handActive;
	private Hand activeHand;
	private boolean rowingBoat;
	private boolean autoJumpEnabled = true;
	private int autoJumpTime;
	private boolean wasFallFlying;
	private int counterInWater;
	private boolean showDeathScreen = true;

	public boolean canTakeKnockback = true;

	public ClientPlayerEntity(Minecraft mc, ClientWorld world, ClientPlayNetHandler connection, StatisticsManager stats, ClientRecipeBook recipeBook, boolean clientSneakState, boolean clientSprintState) {
		super(world, connection.getGameProfile());
		this.mc = mc;
		this.connection = connection;
		this.stats = stats;
		this.recipeBook = recipeBook;
		this.clientSneakState = clientSneakState;
		this.serverSprintState = clientSprintState;
		this.ambientSoundHandlers.add(new UnderwaterAmbientSoundHandler(this, mc.getSoundHandler()));
		this.ambientSoundHandlers.add(new BubbleColumnAmbientSoundHandler(this));
		this.ambientSoundHandlers.add(new BiomeSoundHandler(this, mc.getSoundHandler(), world.getBiomeManager()));
	}

	public boolean attackEntityFrom(DamageSource source, float amount) {
		return false;
	}

	public void heal(float healAmount) {
	}

	public boolean startRiding(Entity entityIn, boolean force) {
		if (!super.startRiding(entityIn, force)) {
			return false;
		} else {
			if (entityIn instanceof AbstractMinecartEntity) {
				this.mc.getSoundHandler().play(new RidingMinecartTickableSound(this, (AbstractMinecartEntity) entityIn));
			}

			if (entityIn instanceof BoatEntity) {
				this.prevRotationYaw = entityIn.rotationYaw;
				this.rotationYaw = entityIn.rotationYaw;
				this.setRotationYawHead(entityIn.rotationYaw);
			}

			return true;
		}
	}

	public void dismount() {
		super.dismount();
		this.rowingBoat = false;
	}

	public float getPitch(float partialTicks) {
		return this.rotationPitch;
	}

	public float getYaw(float partialTicks) {
		return this.isPassenger() ? super.getYaw(partialTicks) : this.rotationYaw;
	}

	public void tick() {
		if (this.world.isBlockLoaded(new BlockPos(this.getPosX(), 0.0D, this.getPosZ()))) {
			super.tick();
			if (this.isPassenger()) {
				this.connection.sendPacket(new CPlayerPacket.RotationPacket(this.rotationYaw, this.rotationPitch, this.onGround));
				this.connection.sendPacket(new CInputPacket(this.moveStrafing, this.moveForward, this.movementInput.jump, this.movementInput.sneaking));
				Entity entity = this.getLowestRidingEntity();
				if (entity != this && entity.canPassengerSteer()) {
					this.connection.sendPacket(new CMoveVehiclePacket(entity));
				}
			} else {
				this.onUpdateWalkingPlayer();
			}

			for (IAmbientSoundHandler iambientsoundhandler : this.ambientSoundHandlers) {
				iambientsoundhandler.tick();
			}

		}
	}

	public float getDarknessAmbience() {
		for (IAmbientSoundHandler iambientsoundhandler : this.ambientSoundHandlers) {
			if (iambientsoundhandler instanceof BiomeSoundHandler) {
				return ((BiomeSoundHandler) iambientsoundhandler).getDarknessAmbienceChance();
			}
		}

		return 0.0F;
	}

	private void onUpdateWalkingPlayer() {
		boolean flag = this.isSprinting();
		if (flag != this.serverSprintState) {
			CEntityActionPacket.Action centityactionpacket$action = flag ? CEntityActionPacket.Action.START_SPRINTING : CEntityActionPacket.Action.STOP_SPRINTING;
			this.connection.sendPacket(new CEntityActionPacket(this, centityactionpacket$action));
			this.serverSprintState = flag;
		}

		boolean flag3 = this.isSneaking();
		if (flag3 != this.clientSneakState) {
			CEntityActionPacket.Action centityactionpacket$action1 = flag3 ? CEntityActionPacket.Action.PRESS_SHIFT_KEY : CEntityActionPacket.Action.RELEASE_SHIFT_KEY;
			this.connection.sendPacket(new CEntityActionPacket(this, centityactionpacket$action1));
			this.clientSneakState = flag3;
		}

		if (this.isCurrentViewEntity()) {
			double d4 = this.getPosX() - this.lastReportedPosX;
			double d0 = this.getPosY() - this.lastReportedPosY;
			double d1 = this.getPosZ() - this.lastReportedPosZ;
			double d2 = this.rotationYaw - this.lastReportedYaw;
			double d3 = this.rotationPitch - this.lastReportedPitch;
			++this.positionUpdateTicks;
			boolean flag1 = d4 * d4 + d0 * d0 + d1 * d1 > 9.0E-4D || this.positionUpdateTicks >= 20;
			boolean flag2 = d2 != 0.0D || d3 != 0.0D;
			if (this.isPassenger()) {
				Vector3d vector3d = this.getMotion();
				this.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(vector3d.x, -999.0D, vector3d.z, this.rotationYaw, this.rotationPitch, this.onGround));
				flag1 = false;
			} else if (flag1 && flag2) {
				this.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(this.getPosX(), this.getPosY(), this.getPosZ(), this.rotationYaw, this.rotationPitch, this.onGround));
			} else if (flag1) {
				this.connection.sendPacket(new CPlayerPacket.PositionPacket(this.getPosX(), this.getPosY(), this.getPosZ(), this.onGround));
			} else if (flag2) {
				this.connection.sendPacket(new CPlayerPacket.RotationPacket(this.rotationYaw, this.rotationPitch, this.onGround));
			} else if (this.prevOnGround != this.onGround) {
				this.connection.sendPacket(new CPlayerPacket(this.onGround));
			}

			if (flag1) {
				this.lastReportedPosX = this.getPosX();
				this.lastReportedPosY = this.getPosY();
				this.lastReportedPosZ = this.getPosZ();
				this.positionUpdateTicks = 0;
			}

			if (flag2) {
				this.lastReportedYaw = this.rotationYaw;
				this.lastReportedPitch = this.rotationPitch;
			}

			this.prevOnGround = this.onGround;
			this.autoJumpEnabled = this.mc.gameSettings.autoJump;
		}

	}

	public boolean drop(boolean p_225609_1_) {
		CPlayerDiggingPacket.Action cplayerdiggingpacket$action = p_225609_1_ ? CPlayerDiggingPacket.Action.DROP_ALL_ITEMS : CPlayerDiggingPacket.Action.DROP_ITEM;
		this.connection.sendPacket(new CPlayerDiggingPacket(cplayerdiggingpacket$action, BlockPos.ZERO, Direction.DOWN));
		return this.inventory.decrStackSize(this.inventory.currentItem, p_225609_1_ && !this.inventory.getCurrentItem().isEmpty() ? this.inventory.getCurrentItem().getCount() : 1) != ItemStack.EMPTY;
	}

	public void sendChatMessage(String message) {
		if (message.startsWith("/goto")) {
			String[] s = message.split("\\s+");
			System.out.println(Arrays.toString(s));
			// 0: /goto, 1: x, 2: y, 3: z
			new ManhuntBot().begin(new BlockPos(Integer.parseInt(s[1]), Integer.parseInt(s[2]), Integer.parseInt(s[3])));
		} else {
			this.connection.sendPacket(new CChatMessagePacket(message));
		}
	}

	public void swingArm(Hand hand) {
		super.swingArm(hand);
		this.connection.sendPacket(new CAnimateHandPacket(hand));
	}

	public void respawnPlayer() {
		this.connection.sendPacket(new CClientStatusPacket(CClientStatusPacket.State.PERFORM_RESPAWN));
	}

	protected void damageEntity(DamageSource damageSrc, float damageAmount) {
		if (!this.isInvulnerableTo(damageSrc)) {
			this.setHealth(this.getHealth() - damageAmount);
		}
	}

	public void closeScreen() {
		this.connection.sendPacket(new CCloseWindowPacket(this.openContainer.windowId));
		this.closeScreenAndDropStack();
	}

	public void closeScreenAndDropStack() {
		this.inventory.setItemStack(ItemStack.EMPTY);
		super.closeScreen();
		this.mc.showScreen(null);
	}

	public void setPlayerSPHealth(float health) {
		if (this.hasValidHealth) {
			float f = this.getHealth() - health;
			if (f <= 0.0F) {
				this.setHealth(health);
				if (f < 0.0F) {
					this.hurtResistantTime = 10;
				}
			} else {
				this.lastDamage = f;
				this.setHealth(this.getHealth());
				this.hurtResistantTime = 20;
				this.damageEntity(DamageSource.GENERIC, f);
				this.maxHurtTime = 10;
				this.hurtTime = this.maxHurtTime;
			}
		} else {
			this.setHealth(health);
			this.hasValidHealth = true;
		}

	}

	public void sendPlayerAbilities() {
		this.connection.sendPacket(new CPlayerAbilitiesPacket(this.abilities));
	}

	public boolean isUser() {
		return true;
	}

	public boolean hasStoppedClimbing() {
		return !this.abilities.isFlying && super.hasStoppedClimbing();
	}

	public boolean shouldSpawnRunningEffects() {
		return !this.abilities.isFlying && super.shouldSpawnRunningEffects();
	}

	public boolean canAddSprintingEffect() {
		return !this.abilities.isFlying && super.canAddSprintingEffect();
	}

	protected void sendHorseJump() {
		this.connection.sendPacket(new CEntityActionPacket(this, CEntityActionPacket.Action.START_RIDING_JUMP, MathHelper.floor(this.getHorseJumpPower() * 100.0F)));
	}

	public void sendHorseInventory() {
		this.connection.sendPacket(new CEntityActionPacket(this, CEntityActionPacket.Action.OPEN_INVENTORY));
	}

	public String getServerBrand() {
		return this.serverBrand;
	}

	public void setServerBrand(String brand) {
		this.serverBrand = brand;
	}

	public StatisticsManager getStats() {
		return this.stats;
	}

	public ClientRecipeBook getRecipeBook() {
		return this.recipeBook;
	}

	public void removeRecipeHighlight(IRecipe<?> recipe) {
		if (this.recipeBook.isNew(recipe)) {
			this.recipeBook.markSeen(recipe);
			this.connection.sendPacket(new CMarkRecipeSeenPacket(recipe));
		}

	}

	protected int getPermissionLevel() {
		return this.permissionLevel;
	}

	public void setPermissionLevel(int permissionLevel) {
		this.permissionLevel = permissionLevel;
	}

	public void sendStatusMessage(ITextComponent chatComponent, boolean actionBar) {
		if (actionBar) {
			this.mc.ingameGUI.setOverlayMessage(chatComponent, false);
		} else {
			this.mc.ingameGUI.getChatGUI().printChatMessage(chatComponent);
		}

	}

	private void setPlayerOffsetMotion(double x, double z) {
		BlockPos blockpos = new BlockPos(x, this.getPosY(), z);
		if (this.shouldBlockPushPlayer(blockpos)) {
			double d0 = x - (double) blockpos.getX();
			double d1 = z - (double) blockpos.getZ();
			Direction direction = null;
			double d2 = Double.MAX_VALUE;
			Direction[] adirection = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH};

			for (Direction direction1 : adirection) {
				double d3 = direction1.getAxis().getCoordinate(d0, 0.0D, d1);
				double d4 = direction1.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0D - d3 : d3;
				if (d4 < d2 && !this.shouldBlockPushPlayer(blockpos.offset(direction1))) {
					d2 = d4;
					direction = direction1;
				}
			}

			if (direction != null) {
				Vector3d vector3d = this.getMotion();
				if (direction.getAxis() == Direction.Axis.X) {
					this.setMotion(0.1D * (double) direction.getXOffset(), vector3d.y, vector3d.z);
				} else {
					this.setMotion(vector3d.x, vector3d.y, 0.1D * (double) direction.getZOffset());
				}
			}

		}
	}

	private boolean shouldBlockPushPlayer(BlockPos pos) {
		AxisAlignedBB axisalignedbb = this.getBoundingBox();
		AxisAlignedBB axisalignedbb1 = (new AxisAlignedBB(pos.getX(), axisalignedbb.minY, pos.getZ(), (double) pos.getX() + 1.0D, axisalignedbb.maxY, (double) pos.getZ() + 1.0D)).shrink(1.0E-7D);
		return !this.world.func_242405_a(this, axisalignedbb1, (p_243494_1_, p_243494_2_) -> p_243494_1_.isSuffocating(this.world, p_243494_2_));
	}

	public void setXPStats(float currentXP, int maxXP, int level) {
		this.experience = currentXP;
		this.experienceTotal = maxXP;
		this.experienceLevel = level;
	}

	public void sendMessage(ITextComponent component, UUID senderUUID) {
		this.mc.ingameGUI.getChatGUI().printChatMessage(component);
	}

	public void handleStatusUpdate(byte id) {
		if (id >= 24 && id <= 28) {
			this.setPermissionLevel(id - 24);
		} else {
			super.handleStatusUpdate(id);
		}

	}

	public boolean isShowDeathScreen() {
		return this.showDeathScreen;
	}

	public void setShowDeathScreen(boolean show) {
		this.showDeathScreen = show;
	}

	public void playSound(SoundEvent soundIn, float volume, float pitch) {
		this.world.playSound(this.getPosX(), this.getPosY(), this.getPosZ(), soundIn, this.getSoundCategory(), volume, pitch, false);
	}

	public void playSound(SoundEvent p_213823_1_, SoundCategory p_213823_2_, float p_213823_3_, float p_213823_4_) {
		this.world.playSound(this.getPosX(), this.getPosY(), this.getPosZ(), p_213823_1_, p_213823_2_, p_213823_3_, p_213823_4_, false);
	}

	public boolean isServerWorld() {
		return true;
	}

	public boolean isHandActive() {
		return this.handActive;
	}

	public void resetActiveHand() {
		super.resetActiveHand();
		this.handActive = false;
	}

	public Hand getActiveHand() {
		return this.activeHand;
	}

	public void setActiveHand(Hand hand) {
		ItemStack itemstack = this.getHeldItem(hand);
		if (!itemstack.isEmpty() && !this.isHandActive()) {
			super.setActiveHand(hand);
			this.handActive = true;
			this.activeHand = hand;
		}
	}

	public void notifyDataManagerChange(DataParameter<?> key) {
		super.notifyDataManagerChange(key);
		if (LIVING_FLAGS.equals(key)) {
			boolean flag = (this.dataManager.get(LIVING_FLAGS) & 1) > 0;
			Hand hand = (this.dataManager.get(LIVING_FLAGS) & 2) > 0 ? Hand.OFF_HAND : Hand.MAIN_HAND;
			if (flag && !this.handActive) {
				this.setActiveHand(hand);
			} else if (!flag && this.handActive) {
				this.resetActiveHand();
			}
		}

		if (FLAGS.equals(key) && this.isElytraFlying() && !this.wasFallFlying) {
			this.mc.getSoundHandler().play(new ElytraSound(this));
		}

	}

	public boolean isRidingHorse() {
		Entity entity = this.getRidingEntity();
		return this.isPassenger() && entity instanceof IJumpingMount && ((IJumpingMount) entity).canJump();
	}

	public float getHorseJumpPower() {
		return this.horseJumpPower;
	}

	public void openSignEditor(SignTileEntity signTile) {
		this.mc.showScreen(new EditSignScreen(signTile));
	}

	public void openMinecartCommandBlock(CommandBlockLogic commandBlock) {
		this.mc.showScreen(new EditMinecartCommandBlockScreen(commandBlock));
	}

	public void openCommandBlock(CommandBlockTileEntity commandBlock) {
		this.mc.showScreen(new CommandBlockScreen(commandBlock));
	}

	public void openStructureBlock(StructureBlockTileEntity structure) {
		this.mc.showScreen(new EditStructureScreen(structure));
	}

	public void openJigsaw(JigsawTileEntity p_213826_1_) {
		this.mc.showScreen(new JigsawScreen(p_213826_1_));
	}

	public void openBook(ItemStack stack, Hand hand) {
		Item item = stack.getItem();
		if (item == Items.WRITABLE_BOOK) {
			this.mc.showScreen(new EditBookScreen(this, stack, hand));
		}

	}

	public void onCriticalHit(Entity entityHit) {
		this.mc.particles.addParticleEmitter(entityHit, ParticleTypes.CRIT);
	}

	public void onEnchantmentCritical(Entity entityHit) {
		this.mc.particles.addParticleEmitter(entityHit, ParticleTypes.ENCHANTED_HIT);
	}

	public boolean isSneaking() {
		return this.movementInput != null && this.movementInput.sneaking;
	}

	public boolean isCrouching() {
		return this.isCrouching;
	}

	public boolean isForcedDown() {
		return this.isCrouching() || this.isVisuallySwimming();
	}

	public void updateEntityActionState() {
		super.updateEntityActionState();
		if (this.isCurrentViewEntity()) {
			this.moveStrafing = this.movementInput.moveStrafe;
			this.moveForward = this.movementInput.moveForward;
			this.isJumping = this.movementInput.jump;
			this.prevRenderArmYaw = this.renderArmYaw;
			this.prevRenderArmPitch = this.renderArmPitch;
			this.renderArmPitch = (float) ((double) this.renderArmPitch + (double) (this.rotationPitch - this.renderArmPitch) * 0.5D);
			this.renderArmYaw = (float) ((double) this.renderArmYaw + (double) (this.rotationYaw - this.renderArmYaw) * 0.5D);
		}

	}

	protected boolean isCurrentViewEntity() {
		return this.mc.getRenderViewEntity() == this;
	}

	public void livingTick() {
		if (this.sprintToggleTimer > 0) {
			--this.sprintToggleTimer;
		}

		this.handlePortalTeleportation();
		boolean flag = this.movementInput.jump;
		boolean flag1 = this.movementInput.sneaking;
		boolean flag2 = this.isUsingSwimmingAnimation();
		this.isCrouching = !this.abilities.isFlying && !this.isSwimming() && this.isPoseClear(Pose.CROUCHING) && (this.isSneaking() || !this.isSleeping() && !this.isPoseClear(Pose.STANDING));
		this.movementInput.tickMovement(this.isForcedDown());
		this.mc.getTutorial().handleMovement(this.movementInput);
		if (this.isHandActive() && !this.isPassenger()) {
			this.movementInput.moveStrafe *= 0.2F;
			this.movementInput.moveForward *= 0.2F;
			this.sprintToggleTimer = 0;
		}

		boolean flag3 = false;
		if (this.autoJumpTime > 0) {
			--this.autoJumpTime;
			flag3 = true;
			this.movementInput.jump = true;
		}

		if (!this.noClip) {
			this.setPlayerOffsetMotion(this.getPosX() - (double) this.getWidth() * 0.35D, this.getPosZ() + (double) this.getWidth() * 0.35D);
			this.setPlayerOffsetMotion(this.getPosX() - (double) this.getWidth() * 0.35D, this.getPosZ() - (double) this.getWidth() * 0.35D);
			this.setPlayerOffsetMotion(this.getPosX() + (double) this.getWidth() * 0.35D, this.getPosZ() - (double) this.getWidth() * 0.35D);
			this.setPlayerOffsetMotion(this.getPosX() + (double) this.getWidth() * 0.35D, this.getPosZ() + (double) this.getWidth() * 0.35D);
		}

		if (flag1) {
			this.sprintToggleTimer = 0;
		}

		boolean flag4 = (float) this.getFoodStats().getFoodLevel() > 6.0F || this.abilities.allowFlying;
		if ((this.onGround || this.canSwim()) && !flag1 && !flag2 && this.isUsingSwimmingAnimation() && !this.isSprinting() && flag4 && !this.isHandActive() && !this.isPotionActive(Effects.BLINDNESS)) {
			if (this.sprintToggleTimer <= 0 && !this.mc.gameSettings.keyBindSprint.isKeyDown()) {
				this.sprintToggleTimer = 7;
			} else {
				this.setSprinting(true);
			}
		}

		if (!this.isSprinting() && (!this.isInWater() || this.canSwim()) && this.isUsingSwimmingAnimation() && flag4 && !this.isHandActive() && !this.isPotionActive(Effects.BLINDNESS) && this.mc.gameSettings.keyBindSprint.isKeyDown()) {
			this.setSprinting(true);
		}

		if (this.isSprinting()) {
			boolean flag5 = !this.movementInput.isMovingForward() || !flag4;
			boolean flag6 = flag5 || this.collidedHorizontally || this.isInWater() && !this.canSwim();
			if (this.isSwimming()) {
				if (!this.onGround && !this.movementInput.sneaking && flag5 || !this.isInWater()) {
					this.setSprinting(false);
				}
			} else if (flag6) {
				this.setSprinting(false);
			}
		}

		boolean flag7 = false;
		if (this.abilities.allowFlying) {
			if (this.mc.playerController.isSpectatorMode()) {
				if (!this.abilities.isFlying) {
					this.abilities.isFlying = true;
					flag7 = true;
					this.sendPlayerAbilities();
				}
			} else if (!flag && this.movementInput.jump && !flag3) {
				if (this.flyToggleTimer == 0) {
					this.flyToggleTimer = 7;
				} else if (!this.isSwimming()) {
					this.abilities.isFlying = !this.abilities.isFlying;
					flag7 = true;
					this.sendPlayerAbilities();
					this.flyToggleTimer = 0;
				}
			}
		}

		if (this.movementInput.jump && !flag7 && !flag && !this.abilities.isFlying && !this.isPassenger() && !this.isOnLadder()) {
			ItemStack itemstack = this.getItemStackFromSlot(EquipmentSlotType.CHEST);
			if (itemstack.getItem() == Items.ELYTRA && ElytraItem.isUsable(itemstack) && this.tryToStartFallFlying()) {
				this.connection.sendPacket(new CEntityActionPacket(this, CEntityActionPacket.Action.START_FALL_FLYING));
			}
		}

		this.wasFallFlying = this.isElytraFlying();
		if (this.isInWater() && this.movementInput.sneaking && this.func_241208_cS_()) {
			this.handleFluidSneak();
		}

		if (this.areEyesInFluid(FluidTags.WATER)) {
			int i = this.isSpectator() ? 10 : 1;
			this.counterInWater = MathHelper.clamp(this.counterInWater + i, 0, 600);
		} else if (this.counterInWater > 0) {
			this.areEyesInFluid(FluidTags.WATER);
			this.counterInWater = MathHelper.clamp(this.counterInWater - 10, 0, 600);
		}

		if (this.abilities.isFlying && this.isCurrentViewEntity()) {
			int j = 0;
			if (this.movementInput.sneaking) {
				--j;
			}

			if (this.movementInput.jump) {
				++j;
			}

			if (j != 0) {
				this.setMotion(this.getMotion().add(0.0D, (float) j * this.abilities.getFlySpeed() * 3.0F, 0.0D));
			}
		}

		if (this.isRidingHorse()) {
			IJumpingMount ijumpingmount = (IJumpingMount) this.getRidingEntity();
			if (this.horseJumpPowerCounter < 0) {
				++this.horseJumpPowerCounter;
				if (this.horseJumpPowerCounter == 0) {
					this.horseJumpPower = 0.0F;
				}
			}

			if (flag && !this.movementInput.jump) {
				this.horseJumpPowerCounter = -10;
				ijumpingmount.setJumpPower(MathHelper.floor(this.getHorseJumpPower() * 100.0F));
				this.sendHorseJump();
			} else if (!flag && this.movementInput.jump) {
				this.horseJumpPowerCounter = 0;
				this.horseJumpPower = 0.0F;
			} else if (flag) {
				++this.horseJumpPowerCounter;
				if (this.horseJumpPowerCounter < 10) {
					this.horseJumpPower = (float) this.horseJumpPowerCounter * 0.1F;
				} else {
					this.horseJumpPower = 0.8F + 2.0F / (float) (this.horseJumpPowerCounter - 9) * 0.1F;
				}
			}
		} else {
			this.horseJumpPower = 0.0F;
		}

		super.livingTick();
		if (this.onGround && this.abilities.isFlying && !this.mc.playerController.isSpectatorMode()) {
			this.abilities.isFlying = false;
			this.sendPlayerAbilities();
		}

	}

	private void handlePortalTeleportation() {
		this.prevTimeInPortal = this.timeInPortal;
		if (this.inPortal) {
			if (this.mc.currentScreen != null && !this.mc.currentScreen.isPauseScreen()) {
				if (this.mc.currentScreen instanceof ContainerScreen) {
					this.closeScreen();
				}

				this.mc.showScreen(null);
			}

			if (this.timeInPortal == 0.0F) {
				this.mc.getSoundHandler().play(SimpleSound.ambientWithoutAttenuation(SoundEvents.BLOCK_PORTAL_TRIGGER, this.rand.nextFloat() * 0.4F + 0.8F, 0.25F));
			}

			this.timeInPortal += 0.0125F;
			if (this.timeInPortal >= 1.0F) {
				this.timeInPortal = 1.0F;
			}

			this.inPortal = false;
		} else if (this.isPotionActive(Effects.NAUSEA) && this.getActivePotionEffect(Effects.NAUSEA).getDuration() > 60) {
			this.timeInPortal += 0.006666667F;
			if (this.timeInPortal > 1.0F) {
				this.timeInPortal = 1.0F;
			}
		} else {
			if (this.timeInPortal > 0.0F) {
				this.timeInPortal -= 0.05F;
			}

			if (this.timeInPortal < 0.0F) {
				this.timeInPortal = 0.0F;
			}
		}

		this.decrementTimeUntilPortal();
	}

	public void updateRidden() {
		super.updateRidden();
		this.rowingBoat = false;
		if (this.getRidingEntity() instanceof BoatEntity) {
			BoatEntity boatentity = (BoatEntity) this.getRidingEntity();
			boatentity.updateInputs(this.movementInput.leftKeyDown, this.movementInput.rightKeyDown, this.movementInput.forwardKeyDown, this.movementInput.backKeyDown);
			this.rowingBoat |= this.movementInput.leftKeyDown || this.movementInput.rightKeyDown || this.movementInput.forwardKeyDown || this.movementInput.backKeyDown;
		}

	}

	public boolean isRowingBoat() {
		return this.rowingBoat;
	}

	@Nullable
	public EffectInstance removeActivePotionEffect(@Nullable Effect potioneffectin) {
		if (potioneffectin == Effects.NAUSEA) {
			this.prevTimeInPortal = 0.0F;
			this.timeInPortal = 0.0F;
		}

		return super.removeActivePotionEffect(potioneffectin);
	}

	public void move(MoverType typeIn, Vector3d pos) {
		double d0 = this.getPosX();
		double d1 = this.getPosZ();
		super.move(typeIn, pos);
		this.updateAutoJump((float) (this.getPosX() - d0), (float) (this.getPosZ() - d1));
	}

	public boolean isAutoJumpEnabled() {
		return this.autoJumpEnabled;
	}

	protected void updateAutoJump(float movementX, float movementZ) {
		if (this.canAutoJump()) {
			Vector3d vector3d = this.getPositionVec();
			Vector3d vector3d1 = vector3d.add(movementX, 0.0D, movementZ);
			Vector3d vector3d2 = new Vector3d(movementX, 0.0D, movementZ);
			float f = this.getAIMoveSpeed();
			float f1 = (float) vector3d2.lengthSquared();
			if (f1 <= 0.001F) {
				Vector2f vector2f = this.movementInput.getMoveVector();
				float f2 = f * vector2f.x;
				float f3 = f * vector2f.y;
				float f4 = MathHelper.sin(this.rotationYaw * ((float) Math.PI / 180F));
				float f5 = MathHelper.cos(this.rotationYaw * ((float) Math.PI / 180F));
				vector3d2 = new Vector3d(f2 * f5 - f3 * f4, vector3d2.y, f3 * f5 + f2 * f4);
				f1 = (float) vector3d2.lengthSquared();
				if (f1 <= 0.001F) {
					return;
				}
			}

			float f12 = MathHelper.fastInvSqrt(f1);
			Vector3d vector3d12 = vector3d2.scale(f12);
			Vector3d vector3d13 = this.getForward();
			float f13 = (float) (vector3d13.x * vector3d12.x + vector3d13.z * vector3d12.z);
			if (!(f13 < -0.15F)) {
				ISelectionContext iselectioncontext = ISelectionContext.forEntity(this);
				BlockPos blockpos = new BlockPos(this.getPosX(), this.getBoundingBox().maxY, this.getPosZ());
				BlockState blockstate = this.world.getBlockState(blockpos);
				if (blockstate.getCollisionShape(this.world, blockpos, iselectioncontext).isEmpty()) {
					blockpos = blockpos.up();
					BlockState blockstate1 = this.world.getBlockState(blockpos);
					if (blockstate1.getCollisionShape(this.world, blockpos, iselectioncontext).isEmpty()) {
						float f7 = 1.2F;
						if (this.isPotionActive(Effects.JUMP_BOOST)) {
							f7 += (float) (this.getActivePotionEffect(Effects.JUMP_BOOST).getAmplifier() + 1) * 0.75F;
						}

						float f8 = Math.max(f * 7.0F, 1.0F / f12);
						Vector3d vector3d4 = vector3d1.add(vector3d12.scale(f8));
						float f9 = this.getWidth();
						float f10 = this.getHeight();
						AxisAlignedBB axisalignedbb = (new AxisAlignedBB(vector3d, vector3d4.add(0.0D, f10, 0.0D))).grow(f9, 0.0D, f9);
						Vector3d lvt_19_1_ = vector3d.add(0.0D, 0.51F, 0.0D);
						vector3d4 = vector3d4.add(0.0D, 0.51F, 0.0D);
						Vector3d vector3d5 = vector3d12.crossProduct(new Vector3d(0.0D, 1.0D, 0.0D));
						Vector3d vector3d6 = vector3d5.scale(f9 * 0.5F);
						Vector3d vector3d7 = lvt_19_1_.subtract(vector3d6);
						Vector3d vector3d8 = vector3d4.subtract(vector3d6);
						Vector3d vector3d9 = lvt_19_1_.add(vector3d6);
						Vector3d vector3d10 = vector3d4.add(vector3d6);
						Iterator<AxisAlignedBB> iterator = this.world.func_234867_d_(this, axisalignedbb, (p_239205_0_) -> true).flatMap((p_212329_0_) -> p_212329_0_.toBoundingBoxList().stream()).iterator();
						float f11 = Float.MIN_VALUE;

						while (iterator.hasNext()) {
							AxisAlignedBB axisalignedbb1 = iterator.next();
							if (axisalignedbb1.intersects(vector3d7, vector3d8) || axisalignedbb1.intersects(vector3d9, vector3d10)) {
								f11 = (float) axisalignedbb1.maxY;
								Vector3d vector3d11 = axisalignedbb1.getCenter();
								BlockPos blockpos1 = new BlockPos(vector3d11);

								for (int i = 1; (float) i < f7; ++i) {
									BlockPos blockpos2 = blockpos1.up(i);
									BlockState blockstate2 = this.world.getBlockState(blockpos2);
									VoxelShape voxelshape;
									if (!(voxelshape = blockstate2.getCollisionShape(this.world, blockpos2, iselectioncontext)).isEmpty()) {
										f11 = (float) voxelshape.getEnd(Direction.Axis.Y) + (float) blockpos2.getY();
										if ((double) f11 - this.getPosY() > (double) f7) {
											return;
										}
									}

									if (i > 1) {
										blockpos = blockpos.up();
										BlockState blockstate3 = this.world.getBlockState(blockpos);
										if (!blockstate3.getCollisionShape(this.world, blockpos, iselectioncontext).isEmpty()) {
											return;
										}
									}
								}
								break;
							}
						}

						if (f11 != Float.MIN_VALUE) {
							float f14 = (float) ((double) f11 - this.getPosY());
							if (!(f14 <= 0.5F) && !(f14 > f7)) {
								this.autoJumpTime = 1;
							}
						}
					}
				}
			}
		}
	}

	private boolean canAutoJump() {
		return this.isAutoJumpEnabled() && this.autoJumpTime <= 0 && this.onGround && !this.isStayingOnGroundSurface() && !this.isPassenger() && this.isMoving() && (double) this.getJumpFactor() >= 1.0D;
	}

	private boolean isMoving() {
		Vector2f vector2f = this.movementInput.getMoveVector();
		return vector2f.x != 0.0F || vector2f.y != 0.0F;
	}

	private boolean isUsingSwimmingAnimation() {
		return this.canSwim() ? this.movementInput.isMovingForward() : (double) this.movementInput.moveForward >= 0.8D;
	}

	public float getWaterBrightness() {
		if (!this.areEyesInFluid(FluidTags.WATER)) {
			return 0.0F;
		} else {
			if ((float) this.counterInWater >= 600.0F) {
				return 1.0F;
			} else {
				float f2 = MathHelper.clamp((float) this.counterInWater / 100.0F, 0.0F, 1.0F);
				float f3 = (float) this.counterInWater < 100.0F ? 0.0F : MathHelper.clamp(((float) this.counterInWater - 100.0F) / 500.0F, 0.0F, 1.0F);
				return f2 * 0.6F + f3 * 0.39999998F;
			}
		}
	}

	public boolean canSwim() {
		return this.eyesInWaterPlayer;
	}

	protected boolean updateEyesInWaterPlayer() {
		boolean flag = this.eyesInWaterPlayer;
		boolean flag1 = super.updateEyesInWaterPlayer();
		if (!this.isSpectator()) {
			if (!flag && flag1) {
				this.world.playSound(this.getPosX(), this.getPosY(), this.getPosZ(), SoundEvents.AMBIENT_UNDERWATER_ENTER, SoundCategory.AMBIENT, 1.0F, 1.0F, false);
				this.mc.getSoundHandler().play(new UnderwaterAmbientSounds.UnderWaterSound(this));
			}

			if (flag && !flag1) {
				this.world.playSound(this.getPosX(), this.getPosY(), this.getPosZ(), SoundEvents.AMBIENT_UNDERWATER_EXIT, SoundCategory.AMBIENT, 1.0F, 1.0F, false);
			}

		}
		return this.eyesInWaterPlayer;
	}

	public Vector3d getLeashPosition(float partialTicks) {
		if (this.mc.gameSettings.getPointOfView().isFirstPerson()) {
			float f = MathHelper.lerp(partialTicks * 0.5F, this.rotationYaw, this.prevRotationYaw) * ((float) Math.PI / 180F);
			float f1 = MathHelper.lerp(partialTicks * 0.5F, this.rotationPitch, this.prevRotationPitch) * ((float) Math.PI / 180F);
			double d0 = this.getPrimaryHand() == HandSide.RIGHT ? -1.0D : 1.0D;
			Vector3d vector3d = new Vector3d(0.39D * d0, -0.6D, 0.3D);
			return vector3d.rotatePitch(-f1).rotateYaw(-f).add(this.getEyePosition(partialTicks));
		} else {
			return super.getLeashPosition(partialTicks);
		}
	}

	@Override
	public void applyKnockback(float strength, double ratioX, double ratioZ) {
		if (canTakeKnockback) {
			super.applyKnockback(strength, ratioX, ratioZ);
		}
	}

	@Override
	public void addVelocity(double x, double y, double z) {
		if(canTakeKnockback) {
			super.addVelocity(x, y, z);
		}
	}
}
