package com.starfantasy.soulsfirecontrol.util;

import com.starfantasy.soulsfirecontrol.config.ChaosMonarchConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.soulsweaponry.config.ConfigConstructor;
import net.soulsweaponry.entity.mobs.ChaosMonarch;

import javax.annotation.Nullable;
import java.util.List;

public final class ChaosMonarchTweaks {
    public static final String NO_LOOT_SUMMON_TAG = "starfantasy_chaos_monarch_no_loot_summon";

    private static final double TELEPORT_MELEE_START_DISTANCE = 3.0D;
    private static final double MELEE_CLOSE_RANGE = 4.0D;
    private static final int TELEPORT_ATTACK_ATTEMPTS = 10;
    private static final int TELEPORT_ATTACK_MAX_Y_OFFSET = 47;
    private static final double TELEPORT_FAR_DISTANCE = 24.0D;
    private static final double TELEPORT_NEAR_DISTANCE = 6.0D;
    private static final double TELEPORT_CLOSE_TO_TARGET_DISTANCE = 5.0D;
    private static final double TELEPORT_RANDOM_MIN_DISTANCE = 12.0D;
    private static final double TELEPORT_RANDOM_MAX_DISTANCE = 24.0D;
    private static final ThreadLocal<ChaosMonarch> SUPPRESSED_GUARD_BREAK_SOURCE = new ThreadLocal<>();

    private ChaosMonarchTweaks() {
    }

    public static boolean rewardsGuardBreak(ChaosMonarch boss) {
        return boss != null
                && boss.getAttack() == ChaosMonarch.Attack.MELEE
                && SUPPRESSED_GUARD_BREAK_SOURCE.get() != boss;
    }

    public static boolean suppressesGuardBreak(ChaosMonarch boss) {
        return boss != null && SUPPRESSED_GUARD_BREAK_SOURCE.get() == boss;
    }

    public static boolean hurtWithoutGuardBreak(ChaosMonarch boss, LivingEntity target,
                                                DamageSource source, float damage) {
        SUPPRESSED_GUARD_BREAK_SOURCE.set(boss);
        try {
            return target.hurt(source, damage);
        } finally {
            SUPPRESSED_GUARD_BREAK_SOURCE.remove();
        }
    }

    public static float modifiedDamage(float damage) {
        return damage * ConfigConstructor.chaos_monarch_damage_modifier;
    }

    public static void igniteIfNotBurning(LivingEntity target, int seconds) {
        if (target.getRemainingFireTicks() <= 0) {
            target.setSecondsOnFire(seconds);
        }
    }

    public static void teleportToMeleeStart(ChaosMonarch boss, LivingEntity target) {
        teleportNearTarget(boss, target, TELEPORT_MELEE_START_DISTANCE);
    }

    public static void teleportNearTarget(ChaosMonarch boss, LivingEntity target, double distanceFromTarget) {
        if (boss.level().isClientSide() || target == null || !target.isAlive()) {
            return;
        }
        Vec3 awayFromTarget = boss.position().subtract(target.position());
        awayFromTarget = new Vec3(awayFromTarget.x, 0.0D, awayFromTarget.z);
        if (awayFromTarget.lengthSqr() < 1.0E-4D) {
            awayFromTarget = target.getLookAngle().multiply(-1.0D, 0.0D, -1.0D);
        }
        if (awayFromTarget.lengthSqr() < 1.0E-4D) {
            awayFromTarget = new Vec3(1.0D, 0.0D, 0.0D);
        }
        Vec3 desired = target.position().add(awayFromTarget.normalize().scale(distanceFromTarget));
        Vec3 ground = groundCenterAt(boss.level(), desired.x, Math.max(target.getY(), boss.getY()) + 4.0D, desired.z,
                Mth.floor(desired.x), Mth.floor(desired.z));
        double yaw = Math.toDegrees(Math.atan2(target.getZ() - ground.z, target.getX() - ground.x)) - 90.0D;
        boss.moveTo(ground.x, ground.y - 0.06D, ground.z, (float) yaw, boss.getXRot());
        boss.setDeltaMovement(Vec3.ZERO);
        boss.fallDistance = 0.0F;
        boss.hurtMarked = true;
        syncPosition(boss);
    }

    public static boolean teleportForTeleportAttack(ChaosMonarch boss, LivingEntity target) {
        if (boss.level().isClientSide() || target == null || !target.isAlive() || !boss.isAlive()) {
            return false;
        }
        for (int attempt = 0; attempt < TELEPORT_ATTACK_ATTEMPTS; ++attempt) {
            Vec3 desired = teleportAttackCandidate(boss, target);
            double searchY = boss.getY() + boss.getRandom().nextInt(TELEPORT_ATTACK_MAX_Y_OFFSET + 1);
            Vec3 landing = validTeleportLanding(boss, desired.x, searchY, desired.z);
            if (landing != null) {
                teleportBossTo(boss, target, landing);
                return true;
            }
        }
        return false;
    }

    public static void faceTarget(Mob boss, LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return;
        }
        boss.getLookControl().setLookAt(target.getX(), target.getEyeY(), target.getZ());
        boss.lookAt(target, 180.0F, 180.0F);
    }

    public static void chaseBeforeMeleeHit(Mob boss, LivingEntity target, double maxDistance) {
        if (boss.level().isClientSide() || target == null || !target.isAlive()) {
            return;
        }
        Vec3 delta = target.position().subtract(boss.position());
        Vec3 horizontal = new Vec3(delta.x, 0.0D, delta.z);
        double distance = horizontal.length();
        if (distance < 1.0E-4D) {
            return;
        }
        double step = distance <= MELEE_CLOSE_RANGE
                ? Math.min(maxDistance, distance * 0.1D)
                : Math.min(maxDistance, Math.max(0.0D, distance - MELEE_CLOSE_RANGE));
        if (step <= 0.02D) {
            return;
        }
        Vec3 movement = horizontal.normalize().scale(step);
        boss.move(MoverType.SELF, movement);
        boss.setDeltaMovement(Vec3.ZERO);
        boss.hurtMarked = true;
        syncPosition(boss);
    }

    public static void summonTeleportMobs(ChaosMonarch boss, Vec3 origin) {
        if (!(boss.level() instanceof ServerLevel level) || origin == null) {
            return;
        }
        List<String> entries = ChaosMonarchConfig.getChaosMonarchTeleportSummons(
                com.starfantasy.soulsfirecontrol.combat.chaosmonarch.ChaosMonarchPhaseManager.getCurrentPhase(boss));
        for (SummonEntry entry : parseSummons(entries)) {
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(entry.id);
            if (type == null) {
                continue;
            }
            for (int i = 0; i < entry.count; ++i) {
                Entity entity = type.create(level);
                if (entity == null) {
                    continue;
                }
                Vec3 pos = summonOffset(origin, i, entry.count);
                Vec3 ground = groundCenterAt(level, pos.x, origin.y + 4.0D, pos.z,
                        Mth.floor(pos.x), Mth.floor(pos.z));
                entity.moveTo(ground.x, ground.y, ground.z, boss.getYRot(), 0.0F);
                entity.addTag(NO_LOOT_SUMMON_TAG);
                if (entity instanceof Mob mob) {
                    mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()),
                            MobSpawnType.MOB_SUMMONED, (SpawnGroupData) null, null);
                    mob.setTarget(boss.getTarget());
                }
                level.addFreshEntity(entity);
            }
        }
    }

    public static boolean shouldSkipTarget(ChaosMonarch boss, @Nullable LivingEntity target) {
        if (target == null || target == boss || !target.isAlive() || target.isInvulnerable()) {
            return true;
        }
        return target instanceof Player player && (player.isCreative() || player.isSpectator());
    }

    public static Vec3 groundCenterBelow(Entity entity) {
        return groundCenterAt(entity.level(), entity.getX(), entity.getY(), entity.getZ(),
                Mth.floor(entity.getX()), Mth.floor(entity.getZ()));
    }

    public static Vec3 groundCenterAt(Level level, double preciseX, double searchY, double preciseZ,
                                      int blockX, int blockZ) {
        int startY = Math.min(level.getMaxBuildHeight() - 1, Mth.floor(searchY));
        int minY = level.getMinBuildHeight() + 1;
        for (int y = startY; y >= minY; --y) {
            BlockPos feet = new BlockPos(blockX, y, blockZ);
            if (level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP)
                    && !level.getBlockState(feet).blocksMotion()) {
                return new Vec3(preciseX, y + 0.06D, preciseZ);
            }
        }
        return new Vec3(preciseX, searchY + 0.06D, preciseZ);
    }

    private static Vec3 teleportAttackCandidate(ChaosMonarch boss, LivingEntity target) {
        Vec3 targetPos = target.position();
        Vec3 bossPos = boss.position();
        Vec3 targetToBoss = horizontal(bossPos.subtract(targetPos));
        double distanceToTarget = targetToBoss.length();
        if (distanceToTarget >= TELEPORT_FAR_DISTANCE) {
            Vec3 bossToTarget = targetToBoss.scale(-1.0D).normalize();
            return targetPos.subtract(bossToTarget.scale(TELEPORT_CLOSE_TO_TARGET_DISTANCE));
        }
        double radius = Mth.lerp(boss.getRandom().nextDouble(),
                TELEPORT_RANDOM_MIN_DISTANCE, TELEPORT_RANDOM_MAX_DISTANCE);
        double angle;
        if (distanceToTarget <= TELEPORT_NEAR_DISTANCE) {
            Vec3 away = targetToBoss.lengthSqr() < 1.0E-4D
                    ? horizontal(target.getLookAngle()).scale(-1.0D)
                    : targetToBoss.normalize();
            if (away.lengthSqr() < 1.0E-4D) {
                away = new Vec3(1.0D, 0.0D, 0.0D);
            }
            angle = Math.atan2(away.z, away.x)
                    + Math.toRadians(boss.getRandom().nextDouble() * 180.0D - 90.0D);
        } else {
            angle = boss.getRandom().nextDouble() * Math.PI * 2.0D;
        }
        return targetPos.add(Math.cos(angle) * radius, 0.0D, Math.sin(angle) * radius);
    }

    @Nullable
    private static Vec3 validTeleportLanding(ChaosMonarch boss, double preciseX, double searchY, double preciseZ) {
        Level level = boss.level();
        int blockX = Mth.floor(preciseX);
        int blockZ = Mth.floor(preciseZ);
        int startY = Math.min(level.getMaxBuildHeight() - 1, Mth.floor(searchY));
        int minY = Math.max(level.getMinBuildHeight() + 1, Mth.floor(boss.getY()));
        for (int y = startY; y >= minY; --y) {
            BlockPos feet = new BlockPos(blockX, y, blockZ);
            BlockPos below = feet.below();
            BlockState belowState = level.getBlockState(below);
            if (!belowState.isFaceSturdy(level, below, Direction.UP)
                    || belowState.getFluidState().is(FluidTags.WATER)
                    || !level.getFluidState(feet).isEmpty()
                    || !level.getFluidState(feet.above()).isEmpty()
                    || level.getBlockState(feet).blocksMotion()
                    || level.getBlockState(feet.above()).blocksMotion()) {
                continue;
            }
            Vec3 landing = new Vec3(preciseX, y + 0.06D, preciseZ);
            AABB movedBox = boss.getBoundingBox().move(landing.subtract(boss.position()));
            if (level.noCollision(boss, movedBox)) {
                return landing;
            }
        }
        return null;
    }

    private static void teleportBossTo(ChaosMonarch boss, LivingEntity target, Vec3 landing) {
        Vec3 oldPos = boss.position();
        double yaw = Math.toDegrees(Math.atan2(target.getZ() - landing.z, target.getX() - landing.x)) - 90.0D;
        boss.moveTo(landing.x, landing.y, landing.z, (float) yaw, boss.getXRot());
        boss.setDeltaMovement(Vec3.ZERO);
        boss.fallDistance = 0.0F;
        boss.hurtMarked = true;
        boss.level().gameEvent(GameEvent.TELEPORT, oldPos, GameEvent.Context.of(boss));
        syncPosition(boss);
    }

    private static Vec3 horizontal(Vec3 vec) {
        return new Vec3(vec.x, 0.0D, vec.z);
    }

    private static List<SummonEntry> parseSummons(List<String> entries) {
        return entries.stream()
                .map(ChaosMonarchTweaks::parseSummon)
                .filter(entry -> entry != null)
                .toList();
    }

    @Nullable
    private static SummonEntry parseSummon(String entry) {
        if (entry == null) {
            return null;
        }
        String trimmed = entry.trim();
        int split = trimmed.lastIndexOf('=');
        if (split <= 0 || split >= trimmed.length() - 1) {
            return null;
        }
        ResourceLocation id = ResourceLocation.tryParse(trimmed.substring(0, split).trim());
        if (id == null) {
            return null;
        }
        try {
            int count = Integer.parseInt(trimmed.substring(split + 1).trim());
            if (count < 1) {
                return null;
            }
            return new SummonEntry(id, Math.min(16, count));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static Vec3 summonOffset(Vec3 origin, int index, int count) {
        if (count <= 1) {
            return origin;
        }
        double angle = Math.PI * 2.0D * index / count;
        double radius = 1.5D;
        return origin.add(Math.cos(angle) * radius, 0.0D, Math.sin(angle) * radius);
    }

    private static void syncPosition(Mob boss) {
        if (boss.level() instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().broadcast(boss, new ClientboundTeleportEntityPacket(boss));
        }
    }

    private record SummonEntry(ResourceLocation id, int count) {
    }
}
