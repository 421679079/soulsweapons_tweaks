/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.MobSpawnType
 *  net.minecraft.world.entity.projectile.AbstractArrow
 *  net.minecraft.world.entity.projectile.LargeFireball
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.entity.projectile.ShulkerBullet
 *  net.minecraft.world.entity.projectile.SmallFireball
 *  net.minecraft.world.entity.projectile.Snowball
 *  net.minecraft.world.entity.projectile.SpectralArrow
 *  net.minecraft.world.entity.projectile.ThrownExperienceBottle
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.soulsweaponry.entity.mobs.ChaosMonarch
 *  net.soulsweaponry.entity.projectile.noclip.FlamePillar
 */
package com.starfantasy.soulsfirecontrol.util;

import com.starfantasy.soulsfirecontrol.config.ChaosMonarchConfig;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.soulsweaponry.entity.mobs.ChaosMonarch;
import net.soulsweaponry.entity.projectile.noclip.FlamePillar;

public final class ChaosMonarchHelper {
    public static final String FIRE_COMBO_TOKEN = "starfantasy:fire_combo";
    private static final String MANAGED_PROJECTILE_FLAG = "starfantasyChaosMonarchProjectile";
    private static final String MANAGED_PROJECTILE_AGE = "starfantasyChaosMonarchProjectileAge";
    private static final String CHAOS_SKULL_NEXT_USE_TICK = "starfantasyChaosSkullNextUseTick";
    private static final String CHAOS_MONARCH_SUMMON_FLAG = "starfantasyChaosMonarchSummon";
    private static final String CHAOS_MONARCH_OWNER_UUID = "starfantasyChaosMonarchOwner";

    private ChaosMonarchHelper() {
    }

    public static void fireControlledProjectile(ChaosMonarch boss, LivingEntity target, int attackStatus, String projectileId) {
        Level level = boss.level();
        if (level.isClientSide()) {
            return;
        }
        Projectile projectile = ChaosMonarchHelper.createControlledProjectile(boss, target, attackStatus, projectileId);
        if (projectile != null) {
            level.addFreshEntity((Entity)projectile);
        }
    }

    public static void fireRandomProjectile(ChaosMonarch boss) {
        Level level = boss.level();
        LivingEntity target = boss.getTarget();
        if (level.isClientSide() || target == null) {
            return;
        }
        String projectileId = ChaosMonarchHelper.pickRandomId(boss.getRandom(), ChaosMonarchConfig.getRandomProjectiles(), ChaosMonarchConfig.DEFAULT_RANDOM_PROJECTILES);
        Projectile projectile = ChaosMonarchHelper.createRandomProjectile(boss, target, projectileId);
        if (projectile != null) {
            level.addFreshEntity((Entity)projectile);
        }
    }

    public static String pickControlledProjectileId(RandomSource random) {
        return ChaosMonarchHelper.pickRandomId(random, ChaosMonarchConfig.getControlledProjectiles(), ChaosMonarchConfig.DEFAULT_CONTROLLED_PROJECTILES);
    }

    public static EntityType<?> pickRandomHostileSummon(RandomSource random) {
        return ChaosMonarchHelper.pickRandomEntityType(random, ChaosMonarchConfig.getRandomHostileSummons(), ChaosMonarchConfig.DEFAULT_RANDOM_HOSTILE_SUMMONS);
    }

    public static EntityType<?> pickRandomPassiveSummon(RandomSource random) {
        return ChaosMonarchHelper.pickRandomEntityType(random, ChaosMonarchConfig.getRandomPassiveSummons(), ChaosMonarchConfig.DEFAULT_RANDOM_PASSIVE_SUMMONS);
    }

    public static boolean tickManagedProjectile(Projectile projectile) {
        CompoundTag data = projectile.getPersistentData();
        if (!data.getBoolean(MANAGED_PROJECTILE_FLAG)) {
            return false;
        }
        int maxLifetime = ChaosMonarchConfig.getProjectileLifetimeTicks();
        if (maxLifetime <= 0 || projectile.level().isClientSide()) {
            return false;
        }
        int age = data.getInt(MANAGED_PROJECTILE_AGE) + 1;
        data.putInt(MANAGED_PROJECTILE_AGE, age);
        if (age >= maxLifetime) {
            projectile.discard();
            return true;
        }
        return false;
    }

    public static Entity spawnConfiguredSummon(ServerLevel level, EntityType<?> type, BlockPos pos) {
        if (type == null) {
            return null;
        }
        return type.spawn(level, pos, MobSpawnType.EVENT);
    }

    public static Entity spawnConfiguredSummon(ServerLevel level, EntityType<?> type, BlockPos pos, Entity owner) {
        Entity entity = ChaosMonarchHelper.spawnConfiguredSummon(level, type, pos);
        return ChaosMonarchHelper.markChaosMonarchSummon(entity, owner);
    }

    public static void fireChaosSkullOrFallback(ChaosMonarch boss, LivingEntity target) {
        if (!ChaosMonarchHelper.tryFireChaosSkull(boss, target)) {
            ChaosMonarchHelper.fireRandomProjectile(boss);
        }
    }

    public static boolean tryFireChaosSkull(ChaosMonarch boss, LivingEntity target) {
        Level level = boss.level();
        if (level.isClientSide() || target == null || !ChaosMonarchHelper.isChaosSkullReady(boss)) {
            return false;
        }
        double dx = target.getX() - boss.getX();
        double dy = target.getY(0.5) - boss.getY(1.0);
        double dz = target.getZ() - boss.getZ();
        EntityType<?> skullType = ChaosMonarchHelper.resolveEntityType("soulsweapons:chaos_skull");
        if (skullType == null) {
            return false;
        }
        Entity entity = skullType.create(level);
        if (!(entity instanceof Projectile)) {
            return false;
        }
        Projectile skull = (Projectile)entity;
        skull.setOwner((Entity)boss);
        skull.moveTo(boss.getX(), boss.getY(), boss.getZ(), skull.getYRot(), skull.getXRot());
        skull.setDeltaMovement(new Vec3(dx, dy + 1.0, dz).scale(0.1));
        level.addFreshEntity((Entity)skull);
        ChaosMonarchHelper.markChaosSkullUsed(boss);
        return true;
    }

    public static boolean isChaosSkullReady(ChaosMonarch boss) {
        int cooldown = ChaosMonarchConfig.getChaosSkullCooldownTicks();
        if (cooldown <= 0) {
            return true;
        }
        long nextUseTick = boss.getPersistentData().getLong(CHAOS_SKULL_NEXT_USE_TICK);
        return boss.level().getGameTime() >= nextUseTick;
    }

    public static Entity markChaosMonarchSummon(Entity entity, Entity owner) {
        UUID ownerUuid = ChaosMonarchHelper.resolveChaosMonarchOwnerUuid(owner);
        if (entity == null || ownerUuid == null) {
            return entity;
        }
        CompoundTag data = entity.getPersistentData();
        data.putBoolean(CHAOS_MONARCH_SUMMON_FLAG, true);
        data.putUUID(CHAOS_MONARCH_OWNER_UUID, ownerUuid);
        return entity;
    }

    public static void spawnLightningReplacement(ChaosMonarch boss, int multiplier) {
        Level level = boss.level();
        if (level.isClientSide()) {
            return;
        }
        int radius = 5 * multiplier;
        float damage = 7.2000003f * (float)multiplier;
        for (int theta = 0; theta < 360; theta += 30) {
            double x = boss.getX() + (double)radius * Math.cos((double)theta * Math.PI / 180.0);
            double z = boss.getZ() + (double)radius * Math.sin((double)theta * Math.PI / 180.0);
            FlamePillar pillar = new FlamePillar(level, (LivingEntity)boss, 1.4f, 8, -1);
            pillar.setBaseDamage((double)damage);
            pillar.setPos(x, boss.getY(), z);
            level.addFreshEntity((Entity)pillar);
        }
    }

    public static void handleSimplifiedChaosSkullImpact(Projectile skull, HitResult hitResult) {
        Level level = skull.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        RandomSource random = serverLevel.getRandom();
        int amount = random.nextInt(5) + 1;
        boolean hostile = random.nextBoolean();
        BlockPos pos = skull.blockPosition();
        for (int i = 0; i < amount; ++i) {
            EntityType<?> type = hostile ? ChaosMonarchHelper.pickRandomHostileSummon(random) : ChaosMonarchHelper.pickRandomPassiveSummon(random);
            ChaosMonarchHelper.spawnConfiguredSummon(serverLevel, type, pos, skull.getOwner());
        }
        skull.discard();
    }

    public static boolean shouldCancelFriendlyFire(LivingEntity target, DamageSource source) {
        UUID summonOwner = ChaosMonarchHelper.getSummonOwnerUuid((Entity)target);
        if (summonOwner == null) {
            return false;
        }
        UUID attackerOwner = ChaosMonarchHelper.resolveDamageSourceOwnerUuid(source);
        return summonOwner.equals(attackerOwner);
    }

    public static boolean isChaosMonarchSummon(Entity entity) {
        return ChaosMonarchHelper.getSummonOwnerUuid(entity) != null;
    }

    private static Projectile createControlledProjectile(ChaosMonarch boss, LivingEntity target, int attackStatus, String projectileId) {
        Vec3 bossPos = ChaosMonarchHelper.getBossPos(boss);
        Vec3 velocity = ChaosMonarchHelper.getAimVector(boss, target, 0.15, true);
        if (FIRE_COMBO_TOKEN.equals(projectileId)) {
            if (attackStatus % 2 == 0 && attackStatus > 18) {
                LargeFireball fireball = new LargeFireball(boss.level(), (LivingEntity)boss, target.getX() - boss.getX(), target.getY(0.5) - boss.getY(1.0), target.getZ() - boss.getZ(), boss.getRandom().nextInt(3) + 1);
                fireball.setPos(bossPos.x, bossPos.y, bossPos.z);
                return ChaosMonarchHelper.markManagedProjectile((Projectile)fireball, (LivingEntity)boss, fireball.getDeltaMovement());
            }
            if (attackStatus < 16) {
                SmallFireball fireball = new SmallFireball(boss.level(), boss.getX(), boss.getEyeY(), boss.getZ(), target.getX() - boss.getX(), target.getY(0.5) - boss.getY(1.0), target.getZ() - boss.getZ());
                fireball.setPos(bossPos.x, bossPos.y, bossPos.z);
                return ChaosMonarchHelper.markManagedProjectile((Projectile)fireball, (LivingEntity)boss, fireball.getDeltaMovement());
            }
            return null;
        }
        if ("minecraft:shulker_bullet".equals(projectileId)) {
            ShulkerBullet bullet = new ShulkerBullet(boss.level(), (LivingEntity)boss, (Entity)target, boss.getDirection().getAxis());
            bullet.setPos(bossPos.x, bossPos.y, bossPos.z);
            return ChaosMonarchHelper.markManagedProjectile((Projectile)bullet, (LivingEntity)boss, bullet.getDeltaMovement());
        }
        if ("minecraft:experience_bottle".equals(projectileId)) {
            ThrownExperienceBottle bottle = new ThrownExperienceBottle(EntityType.EXPERIENCE_BOTTLE, boss.level());
            return ChaosMonarchHelper.initSimpleProjectile((Projectile)bottle, (LivingEntity)boss, bossPos, velocity, 0.0);
        }
        if ("minecraft:snowball".equals(projectileId)) {
            Snowball snowball = new Snowball(EntityType.SNOWBALL, boss.level());
            return ChaosMonarchHelper.initSimpleProjectile((Projectile)snowball, (LivingEntity)boss, bossPos, velocity, 0.0);
        }
        if ("minecraft:spectral_arrow".equals(projectileId)) {
            SpectralArrow arrow = new SpectralArrow(boss.level(), (LivingEntity)boss);
            arrow.setBaseDamage(8.0);
            return ChaosMonarchHelper.initSimpleProjectile((Projectile)arrow, (LivingEntity)boss, bossPos, velocity.add(0.0, 0.2, 0.0), 0.0);
        }
        if ("minecraft:small_fireball".equals(projectileId)) {
            SmallFireball fireball = new SmallFireball(boss.level(), boss.getX(), boss.getEyeY(), boss.getZ(), target.getX() - boss.getX(), target.getY(0.5) - boss.getY(1.0), target.getZ() - boss.getZ());
            fireball.setPos(bossPos.x, bossPos.y, bossPos.z);
            return ChaosMonarchHelper.markManagedProjectile((Projectile)fireball, (LivingEntity)boss, fireball.getDeltaMovement());
        }
        if ("minecraft:fireball".equals(projectileId)) {
            LargeFireball fireball = new LargeFireball(boss.level(), (LivingEntity)boss, target.getX() - boss.getX(), target.getY(0.5) - boss.getY(1.0), target.getZ() - boss.getZ(), boss.getRandom().nextInt(3) + 1);
            fireball.setPos(bossPos.x, bossPos.y, bossPos.z);
            return ChaosMonarchHelper.markManagedProjectile((Projectile)fireball, (LivingEntity)boss, fireball.getDeltaMovement());
        }
        Projectile projectile = ChaosMonarchHelper.createGenericProjectile(projectileId, boss.level());
        if (projectile instanceof AbstractArrow) {
            AbstractArrow arrow = (AbstractArrow)projectile;
            arrow.setBaseDamage(8.0);
        }
        return ChaosMonarchHelper.initSimpleProjectile(projectile, (LivingEntity)boss, bossPos, velocity, 0.0);
    }

    private static Projectile createRandomProjectile(ChaosMonarch boss, LivingEntity target, String projectileId) {
        Vec3 bossPos = ChaosMonarchHelper.getBossPos(boss);
        Vec3 velocity = ChaosMonarchHelper.getAimVector(boss, target, 0.15, true);
        if ("minecraft:fireball".equals(projectileId)) {
            LargeFireball fireball = new LargeFireball(boss.level(), (LivingEntity)boss, target.getX() - boss.getX(), target.getY(0.5) - boss.getY(1.0), target.getZ() - boss.getZ(), boss.getRandom().nextInt(3) + 1);
            fireball.setPos(bossPos.x, bossPos.y, bossPos.z);
            return ChaosMonarchHelper.markManagedProjectile((Projectile)fireball, (LivingEntity)boss, fireball.getDeltaMovement());
        }
        if ("minecraft:small_fireball".equals(projectileId)) {
            SmallFireball fireball = new SmallFireball(boss.level(), boss.getX(), boss.getEyeY(), boss.getZ(), target.getX() - boss.getX(), target.getY(0.5) - boss.getY(1.0), target.getZ() - boss.getZ());
            fireball.setPos(bossPos.x, bossPos.y, bossPos.z);
            return ChaosMonarchHelper.markManagedProjectile((Projectile)fireball, (LivingEntity)boss, fireball.getDeltaMovement());
        }
        if ("minecraft:spectral_arrow".equals(projectileId)) {
            SpectralArrow arrow = new SpectralArrow(boss.level(), (LivingEntity)boss);
            arrow.setBaseDamage(6.0);
            return ChaosMonarchHelper.initSimpleProjectile((Projectile)arrow, (LivingEntity)boss, bossPos, velocity, 0.0);
        }
        if ("minecraft:shulker_bullet".equals(projectileId)) {
            ShulkerBullet bullet = new ShulkerBullet(boss.level(), (LivingEntity)boss, (Entity)target, boss.getDirection().getAxis());
            bullet.setPos(bossPos.x, bossPos.y, bossPos.z);
            return ChaosMonarchHelper.markManagedProjectile((Projectile)bullet, (LivingEntity)boss, bullet.getDeltaMovement());
        }
        Projectile projectile = ChaosMonarchHelper.createGenericProjectile(projectileId, boss.level());
        if (projectile instanceof AbstractArrow) {
            AbstractArrow arrow = (AbstractArrow)projectile;
            arrow.setBaseDamage(6.0);
        }
        return ChaosMonarchHelper.initSimpleProjectile(projectile, (LivingEntity)boss, bossPos, velocity, 0.0);
    }

    private static Projectile initSimpleProjectile(Projectile projectile, LivingEntity owner, Vec3 startPos, Vec3 velocity, double extraYVelocity) {
        if (projectile == null) {
            return null;
        }
        projectile.setPos(startPos.x, startPos.y, startPos.z);
        Vec3 finalVelocity = velocity.add(0.0, extraYVelocity, 0.0);
        return ChaosMonarchHelper.markManagedProjectile(projectile, owner, finalVelocity);
    }

    private static Projectile markManagedProjectile(Projectile projectile, LivingEntity owner, Vec3 velocity) {
        if (projectile.getOwner() == null) {
            projectile.setOwner((Entity)owner);
        }
        projectile.setDeltaMovement(velocity);
        CompoundTag data = projectile.getPersistentData();
        data.putBoolean(MANAGED_PROJECTILE_FLAG, true);
        data.putInt(MANAGED_PROJECTILE_AGE, 0);
        return projectile;
    }

    private static Projectile createGenericProjectile(String projectileId, Level level) {
        EntityType<?> type = ChaosMonarchHelper.resolveEntityType(projectileId);
        if (type == null) {
            return null;
        }
        Entity entity = type.create(level);
        if (entity instanceof Projectile) {
            Projectile projectile = (Projectile)entity;
            return projectile;
        }
        return null;
    }

    private static EntityType<?> pickRandomEntityType(RandomSource random, List<String> configuredIds, List<String> fallbackIds) {
        List<String> ids;
        List<String> list = ids = configuredIds.isEmpty() ? fallbackIds : configuredIds;
        if (ids.isEmpty()) {
            return null;
        }
        for (int attempt = 0; attempt < ids.size(); ++attempt) {
            String id = ids.get(random.nextInt(ids.size()));
            EntityType<?> type = ChaosMonarchHelper.resolveEntityType(id);
            if (type == null) continue;
            return type;
        }
        return null;
    }

    private static String pickRandomId(RandomSource random, List<String> configuredIds, List<String> fallbackIds) {
        List<String> ids;
        List<String> list = ids = configuredIds.isEmpty() ? fallbackIds : configuredIds;
        if (ids.isEmpty()) {
            return "";
        }
        return ids.get(random.nextInt(ids.size()));
    }

    private static EntityType<?> resolveEntityType(String id) {
        ResourceLocation resourceLocation = ResourceLocation.tryParse((String)id);
        if (resourceLocation == null) {
            return null;
        }
        return (EntityType)ForgeRegistries.ENTITY_TYPES.getValue(resourceLocation);
    }

    private static Vec3 getBossPos(ChaosMonarch boss) {
        return new Vec3(boss.getX(), boss.getEyeY(), boss.getZ());
    }

    private static Vec3 getAimVector(ChaosMonarch boss, LivingEntity target, double scale, boolean addExtraY) {
        double dx = target.getX() - boss.getX();
        double dy = target.getY(0.5) - boss.getY(1.0);
        double dz = target.getZ() - boss.getZ();
        double extraY = addExtraY ? 1.0 : 0.0;
        return new Vec3(dx, dy + extraY, dz).scale(scale);
    }

    private static void markChaosSkullUsed(ChaosMonarch boss) {
        int cooldown = ChaosMonarchConfig.getChaosSkullCooldownTicks();
        if (cooldown > 0) {
            boss.getPersistentData().putLong(CHAOS_SKULL_NEXT_USE_TICK, boss.level().getGameTime() + (long)cooldown);
        }
    }

    private static UUID getSummonOwnerUuid(Entity entity) {
        if (entity == null) {
            return null;
        }
        CompoundTag data = entity.getPersistentData();
        if (!data.getBoolean(CHAOS_MONARCH_SUMMON_FLAG) || !data.hasUUID(CHAOS_MONARCH_OWNER_UUID)) {
            return null;
        }
        return data.getUUID(CHAOS_MONARCH_OWNER_UUID);
    }

    private static UUID resolveDamageSourceOwnerUuid(DamageSource source) {
        if (source == null) {
            return null;
        }
        UUID directOwner = ChaosMonarchHelper.resolveChaosMonarchOwnerUuid(source.getDirectEntity());
        if (directOwner != null) {
            return directOwner;
        }
        return ChaosMonarchHelper.resolveChaosMonarchOwnerUuid(source.getEntity());
    }

    private static UUID resolveChaosMonarchOwnerUuid(Entity entity) {
        Projectile projectile;
        Entity entity2;
        if (entity == null) {
            return null;
        }
        if (entity instanceof ChaosMonarch) {
            return entity.getUUID();
        }
        if (entity instanceof Projectile && (entity2 = (projectile = (Projectile)entity).getOwner()) instanceof ChaosMonarch) {
            ChaosMonarch owner = (ChaosMonarch)entity2;
            return owner.getUUID();
        }
        return null;
    }
}

