package com.starfantasy.soulsfirecontrol.util;

import com.starfantasy.soulsfirecontrol.vfx.telegraph.TelegraphVfx;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.soulsweaponry.config.ConfigConstructor;
import net.soulsweaponry.entity.mobs.AccursedLordBoss;

public final class AccursedLordTweaks {
    public static final int REQUIRED_GUARDS = 5;
    public static final int STUN_TICKS = 160;
    public static final float NORMAL_DAMAGE_MULTIPLIER = 0.7F;

    private static final int SWORDSLAM_HIT_FRAME = 17;
    private static final int PROJECTILE_FIRST_SHOT_FRAME = 10;
    private static final int PULL_HIT_FRAME = 20;
    private static final int HEATWAVE_FIRST_HIT_FRAME = 16;
    private static final int SPIN_FIRST_HIT_FRAME = 7;
    private static final int DEFAULT_GOAL_TICK_INTERVAL = 2;
    private static final double WARNING_HEIGHT_FRACTION = 0.5D;
    private static final double MIN_WARNING_HEIGHT = 1.5D;
    private static final double MAX_WARNING_HEIGHT = 3.2D;
    private static final double BODY_RING_SCALE = 1.5D;
    private static final double SWORDSLAM_RADIUS = 3.0D;
    private static final double PROJECTILE_WARNING_RADIUS = 6.0D;
    private static final double PULL_WARNING_RADIUS = 6.0D;
    private static final double HEATWAVE_RADIUS = 5.0D;
    private static final double SPIN_RADIUS = 6.0D;
    private static final double PROJECTILE_EXPLOSION_RADIUS = 1.0D;
    private static final double PROJECTILE_EXPLOSION_VISUAL_SIZE = 2.0D;
    private static final float PROJECTILE_EXPLOSION_DAMAGE = 5.0F;
    private static final String ACCURSED_PROJECTILE_EXPLODED_KEY = "StarfantasyAccursedLordProjectileExploded";

    private AccursedLordTweaks() {
    }

    public static void warnSwordSlam(AccursedLordBoss boss, int attackStatus, BlockPos attackPos) {
        if (attackStatus != 0) {
            return;
        }
        int warningTicks = warningTicksForGoalFrame(SWORDSLAM_HIT_FRAME);
        warnBossBody(boss, warningTicks, false, SWORDSLAM_RADIUS);
        if (attackPos != null) {
            Vec3 center = new Vec3(attackPos.getX() + 0.5D, attackPos.getY() + 0.06D,
                    attackPos.getZ() + 0.5D);
            TelegraphVfx.groundWarningCircle(boss, center, warningTicks, SWORDSLAM_RADIUS);
        }
    }

    public static void warnProjectileBarrage(AccursedLordBoss boss, int attackStatus) {
        if (attackStatus == 0) {
            warnBossBody(boss, warningTicksForGoalFrame(PROJECTILE_FIRST_SHOT_FRAME),
                    true, PROJECTILE_WARNING_RADIUS);
        }
    }

    public static void warnPull(AccursedLordBoss boss, int attackStatus) {
        if (attackStatus == 0) {
            warnBossBody(boss, warningTicksForGoalFrame(PULL_HIT_FRAME), true, PULL_WARNING_RADIUS);
        }
    }

    public static void warnHeatwave(AccursedLordBoss boss, int attackStatus) {
        if (attackStatus != 0) {
            return;
        }
        int warningTicks = warningTicksForGoalFrame(HEATWAVE_FIRST_HIT_FRAME);
        warnBossBody(boss, warningTicks, false, HEATWAVE_RADIUS);
        TelegraphVfx.groundWarningCircleTrackingGround(boss, warningTicks, HEATWAVE_RADIUS, false);
    }

    public static void warnSpin(AccursedLordBoss boss, int attackStatus) {
        if (attackStatus != 0) {
            return;
        }
        int warningTicks = warningTicksForGoalFrame(SPIN_FIRST_HIT_FRAME);
        warnBossBody(boss, warningTicks, false, SPIN_RADIUS);
        TelegraphVfx.groundWarningCircleTrackingGround(boss, warningTicks, SPIN_RADIUS, false);
    }

    public static boolean rewardsGuardBreak(AccursedLordBoss boss) {
        if (boss == null) {
            return false;
        }
        return switch (boss.getAttackAnimation()) {
            case SWORDSLAM, HEATWAVE, SPIN -> true;
            default -> false;
        };
    }

    public static void forcePullIgnoringKnockbackResistance(LivingEntity target, double x, double z, double strength) {
        Vec3 direction = new Vec3(x, 0.0D, z);
        if (direction.lengthSqr() < 1.0E-7D) {
            return;
        }
        direction = direction.normalize().scale(strength);
        Vec3 movement = target.getDeltaMovement();
        target.setDeltaMovement(
                movement.x / 2.0D - direction.x,
                target.onGround() ? Math.min(0.4D, movement.y / 2.0D + strength) : movement.y,
                movement.z / 2.0D - direction.z);
        target.hasImpulse = true;
        target.hurtMarked = true;
    }

    public static void detonateAccursedProjectile(Entity projectile, Vec3 center) {
        if (projectile == null || center == null || projectile.level().isClientSide) {
            return;
        }
        Entity owner = projectile instanceof net.minecraft.world.entity.projectile.Projectile vanillaProjectile
                ? vanillaProjectile.getOwner()
                : null;
        if (!(owner instanceof AccursedLordBoss boss) || !boss.isAlive()) {
            return;
        }
        CompoundTag data = projectile.getPersistentData();
        if (data.getBoolean(ACCURSED_PROJECTILE_EXPLODED_KEY)) {
            return;
        }
        data.putBoolean(ACCURSED_PROJECTILE_EXPLODED_KEY, true);
        if (!(projectile.level() instanceof ServerLevel level)) {
            return;
        }
        level.playSound(null, center.x, center.y, center.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0F, 1.0F);
        TelegraphVfx.swordExplosion(boss, center, PROJECTILE_EXPLOSION_VISUAL_SIZE);
        DamageSource source = boss.damageSources().indirectMagic(projectile, boss);
        float damage = PROJECTILE_EXPLOSION_DAMAGE * ConfigConstructor.decaying_king_damage_modifier;
        AABB searchBox = new AABB(center, center).inflate(PROJECTILE_EXPLOSION_RADIUS);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, searchBox)) {
            if (shouldSkipExplosionTarget(boss, target)
                    || !intersectsSphere(target.getBoundingBox(), center, PROJECTILE_EXPLOSION_RADIUS)) {
                continue;
            }
            target.hurt(source, damage);
        }
        projectile.discard();
    }

    private static void warnBossBody(AccursedLordBoss boss, int warningTicks, boolean red, double radius) {
        if (boss == null || warningTicks <= 0) {
            return;
        }
        double height = Math.max(MIN_WARNING_HEIGHT,
                Math.min(MAX_WARNING_HEIGHT, boss.getBbHeight() * WARNING_HEIGHT_FRACTION));
        double scaledRadius = radius * BODY_RING_SCALE;
        if (red) {
            TelegraphVfx.redAttackWarningRing(boss, warningTicks, scaledRadius, height);
        } else {
            TelegraphVfx.attackWarningRing(boss, warningTicks, scaledRadius, height);
        }
    }

    private static int warningTicksForGoalFrame(int hitFrame) {
        return Math.max(1, hitFrame * DEFAULT_GOAL_TICK_INTERVAL);
    }

    private static boolean shouldSkipExplosionTarget(AccursedLordBoss boss, LivingEntity target) {
        if (target == null || target == boss || !target.isAlive() || target.isInvulnerable()) {
            return true;
        }
        return target instanceof Player player && (player.isCreative() || player.isSpectator());
    }

    private static boolean intersectsSphere(AABB box, Vec3 center, double radius) {
        double dx = distanceToInterval(center.x, box.minX, box.maxX);
        double dy = distanceToInterval(center.y, box.minY, box.maxY);
        double dz = distanceToInterval(center.z, box.minZ, box.maxZ);
        return dx * dx + dy * dy + dz * dz <= radius * radius;
    }

    private static double distanceToInterval(double value, double min, double max) {
        if (value < min) {
            return min - value;
        }
        if (value > max) {
            return value - max;
        }
        return 0.0D;
    }
}
