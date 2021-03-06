package net.minecraft.entity.ai.brain;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShootableItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPosWrapper;
import net.minecraft.util.math.EntityPosWrapper;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

public class BrainUtil {
   public static void lookApproachEachOther(LivingEntity firstEntity, LivingEntity secondEntity, float speed) {
      lookAtEachOther(firstEntity, secondEntity);
      approachEachOther(firstEntity, secondEntity, speed);
   }

   public static boolean canSee(Brain<?> brainIn, LivingEntity target) {
      return brainIn.getMemory(MemoryModuleType.VISIBLE_MOBS).filter((p_220614_1_) -> p_220614_1_.contains(target)).isPresent();
   }

   public static boolean isCorrectVisibleType(Brain<?> brains, MemoryModuleType<? extends LivingEntity> memorymodule, EntityType<?> entityTypeIn) {
      return canSeeEntity(brains, memorymodule, (p_220622_1_) -> p_220622_1_.getType() == entityTypeIn);
   }

   private static boolean canSeeEntity(Brain<?> brain, MemoryModuleType<? extends LivingEntity> memoryType, Predicate<LivingEntity> livingPredicate) {
      return brain.getMemory(memoryType).filter(livingPredicate).filter(LivingEntity::isAlive).filter((p_220615_1_) -> canSee(brain, p_220615_1_)).isPresent();
   }

   private static void lookAtEachOther(LivingEntity firstEntity, LivingEntity secondEntity) {
      lookAt(firstEntity, secondEntity);
      lookAt(secondEntity, firstEntity);
   }

   public static void lookAt(LivingEntity entityIn, LivingEntity targetIn) {
      entityIn.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityPosWrapper(targetIn, true));
   }

   private static void approachEachOther(LivingEntity firstEntity, LivingEntity secondEntity, float speed) {
      setTargetEntity(firstEntity, secondEntity, speed, 2);
      setTargetEntity(secondEntity, firstEntity, speed, 2);
   }

   public static void setTargetEntity(LivingEntity livingEntity, Entity target, float speed, int distance) {
      WalkTarget walktarget = new WalkTarget(new EntityPosWrapper(target, false), speed, distance);
      livingEntity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityPosWrapper(target, true));
      livingEntity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, walktarget);
   }

   public static void setTargetPosition(LivingEntity livingEntity, BlockPos pos, float speed, int distance) {
      WalkTarget walktarget = new WalkTarget(new BlockPosWrapper(pos), speed, distance);
      livingEntity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosWrapper(pos));
      livingEntity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, walktarget);
   }

   public static void spawnItemNearEntity(LivingEntity livingEntity, ItemStack stack, Vector3d offset) {
      double d0 = livingEntity.getPosYEye() - (double)0.3F;
      ItemEntity itementity = new ItemEntity(livingEntity.world, livingEntity.getPosX(), d0, livingEntity.getPosZ(), stack);
      Vector3d vector3d = offset.subtract(livingEntity.getPositionVec());
      vector3d = vector3d.normalize().scale(0.3F);
      itementity.setMotion(vector3d);
      itementity.setDefaultPickupDelay();
      livingEntity.world.addEntity(itementity);
   }

   public static SectionPos getClosestVillageSection(ServerWorld serverWorldIn, SectionPos sectionPosIn, int radius) {
      int i = serverWorldIn.sectionsToVillage(sectionPosIn);
      return SectionPos.getAllInBox(sectionPosIn, radius).filter((p_220620_2_) -> serverWorldIn.sectionsToVillage(p_220620_2_) < i).min(Comparator.comparingInt(serverWorldIn::sectionsToVillage)).orElse(sectionPosIn);
   }

   public static boolean canFireAtTarget(MobEntity mob, LivingEntity target, int cooldown) {
      Item item = mob.getHeldItemMainhand().getItem();
      if (item instanceof ShootableItem && mob.func_230280_a_((ShootableItem)item)) {
         int i = ((ShootableItem)item).func_230305_d_() - cooldown;
         return mob.isEntityInRange(target, i);
      } else {
         return canAttackTarget(mob, target);
      }
   }

   public static boolean canAttackTarget(LivingEntity livingEntity, LivingEntity target) {
      double d0 = livingEntity.getDistanceSq(target);
      double d1 = livingEntity.getWidth() * 2.0F * livingEntity.getWidth() * 2.0F + target.getWidth();
      return d0 <= d1;
   }

   public static boolean isTargetOutsideDistance(LivingEntity livingEntity, LivingEntity target, double distance) {
      Optional<LivingEntity> optional = livingEntity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
      if (!optional.isPresent()) {
         return true;
      } else {
         double d0 = livingEntity.getDistanceSq(optional.get().getPositionVec());
         double d1 = livingEntity.getDistanceSq(target.getPositionVec());
         return !(d1 > d0 + distance * distance);
      }
   }

   public static boolean isMobVisible(LivingEntity livingEntity, LivingEntity target) {
      Brain<?> brain = livingEntity.getBrain();
      return brain.hasMemory(MemoryModuleType.VISIBLE_MOBS) && brain.getMemory(MemoryModuleType.VISIBLE_MOBS).get().contains(target);
   }

   public static LivingEntity getNearestEntity(LivingEntity centerEntity, Optional<LivingEntity> optionalEntity, LivingEntity livingEntity) {
      return optionalEntity.map(entity -> getNearestEntity(centerEntity, entity, livingEntity)).orElse(livingEntity);
   }

   public static LivingEntity getNearestEntity(LivingEntity centerEntity, LivingEntity livingEntity1, LivingEntity livingEntity2) {
      Vector3d vector3d = livingEntity1.getPositionVec();
      Vector3d vector3d1 = livingEntity2.getPositionVec();
      return centerEntity.getDistanceSq(vector3d) < centerEntity.getDistanceSq(vector3d1) ? livingEntity1 : livingEntity2;
   }

   public static Optional<LivingEntity> getTargetFromMemory(LivingEntity livingEntity, MemoryModuleType<UUID> targetMemory) {
      Optional<UUID> optional = livingEntity.getBrain().getMemory(targetMemory);
      return optional.map((p_233868_1_) -> (LivingEntity)((ServerWorld)livingEntity.world).getEntityByUuid(p_233868_1_));
   }

   public static Stream<VillagerEntity> getNearbyVillagers(VillagerEntity villager, Predicate<VillagerEntity> villagerPredicate) {
      return villager.getBrain().getMemory(MemoryModuleType.MOBS).map((p_233873_2_) -> p_233873_2_.stream().filter((p_233871_1_) -> p_233871_1_ instanceof VillagerEntity && p_233871_1_ != villager).map((p_233859_0_) -> (VillagerEntity)p_233859_0_).filter(LivingEntity::isAlive).filter(villagerPredicate)).orElseGet(Stream::empty);
   }
}
