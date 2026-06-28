package com.starfantasy.soulsfirecontrol.util;

import com.starfantasy.soulsfirecontrol.vfx.telegraph.TelegraphVfx;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.soulsweaponry.entity.mobs.DraugrBoss;

public final class DraugrBossTweaks {
    private static final int MAX_WARNING_TICKS = 20;
    private static final double WARNING_HEIGHT = 1.5D;
    private static final double WARNING_RADIUS_SCALE = 1.5D;
    private static final double DEFAULT_MELEE_WARNING_RADIUS = 2.6D;
    private static final double HEAVY_WARNING_RADIUS = 3.0D;
    private static final double LEAP_WARNING_RADIUS = 3.2D;
    private static final double THRUST_WARNING_RADIUS = 2.8D;
    private static final double ROAR_KNOCKBACK_HORIZONTAL = 2.35D;
    private static final double ROAR_KNOCKBACK_VERTICAL = 0.45D;
    private static final int PARRY_SHIELD_END_FRAME = 14;
    private static final int PARRY_SWORD_FRAME = 26;

    private DraugrBossTweaks() {
    }

    public static void warnSingleTargetFrames(DraugrBoss boss, int currentAttackStatus, int[] frames,
                                              float knockback) {
        double radius = knockback > 0.0F ? 3.3D : DEFAULT_MELEE_WARNING_RADIUS;
        if (frames.length > 0) {
            int warningTicks = Math.min(MAX_WARNING_TICKS, Math.max(1, frames[0]));
            spawnWarningAtStatus(boss, currentAttackStatus, frames[0], warningTicks, radius);
        }
    }

    public static void warnNextSingleTargetFrame(DraugrBoss boss, int currentAttackStatus, int[] frames,
                                                 float knockback) {
        double radius = knockback > 0.0F ? 3.3D : DEFAULT_MELEE_WARNING_RADIUS;
        for (int i = 0; i < frames.length - 1; ++i) {
            if (currentAttackStatus == frames[i]) {
                int warningTicks = Math.min(MAX_WARNING_TICKS, Math.max(1, frames[i + 1] - frames[i]));
                spawnWarningNow(boss, warningTicks, radius);
                return;
            }
        }
    }

    public static void warnParrySwordAfterShield(DraugrBoss boss, int currentAttackStatus) {
        if (currentAttackStatus == PARRY_SHIELD_END_FRAME) {
            spawnWarningNow(boss, PARRY_SWORD_FRAME - PARRY_SHIELD_END_FRAME, DEFAULT_MELEE_WARNING_RADIUS);
        }
    }

    public static void warnFrame(DraugrBoss boss, int currentAttackStatus, int frame, double radius) {
        int warningTicks = Math.min(MAX_WARNING_TICKS, Math.max(1, frame));
        spawnWarningAtStatus(boss, currentAttackStatus, frame, warningTicks, radius);
    }

    public static void warnHeavy(DraugrBoss boss, int currentAttackStatus) {
        warnFrame(boss, currentAttackStatus, 24, HEAVY_WARNING_RADIUS);
    }

    public static void warnLeap(DraugrBoss boss, int currentAttackStatus) {
        warnFrame(boss, currentAttackStatus, 26, LEAP_WARNING_RADIUS);
    }

    public static void warnRunThrust(DraugrBoss boss, int currentAttackStatus) {
        warnFrame(boss, currentAttackStatus, 13, THRUST_WARNING_RADIUS);
    }

    public static void warnAoe(DraugrBoss boss, int currentAttackStatus, int frame, double radius) {
        int warningTicks = Math.min(MAX_WARNING_TICKS, Math.max(1, frame));
        spawnWarningAtStatus(boss, currentAttackStatus, frame, warningTicks, radius);
    }

    public static boolean isBattleCry(float damage, MobEffect[] effects, double boxSize, boolean shieldUpWhenDone) {
        return damage == 0.0F && effects.length > 0 && boxSize >= 9.5D && shieldUpWhenDone;
    }

    public static void tickBattleCry(DraugrBoss boss, int attackStatus, int hitFrame, double radius) {
        if (attackStatus >= hitFrame && attackStatus <= hitFrame + 8 && (attackStatus - hitFrame) % 2 == 0) {
            spawnRoarWave(boss, radius);
        }
        if (attackStatus == hitFrame) {
            knockBackNearbyPlayers(boss, radius);
        }
    }

    private static void spawnWarningAtStatus(DraugrBoss boss, int currentAttackStatus, int hitFrame,
                                             int warningTicks, double radius) {
        if (currentAttackStatus == hitFrame - warningTicks) {
            spawnWarningNow(boss, warningTicks, radius);
        }
    }

    private static void spawnWarningNow(DraugrBoss boss, int warningTicks, double radius) {
        TelegraphVfx.attackWarningRing(boss, warningTicks, radius * WARNING_RADIUS_SCALE, WARNING_HEIGHT);
    }

    private static void spawnRoarWave(DraugrBoss boss, double radius) {
        Vec3 forward = horizontalLook(boss);
        Vec3 origin = boss.position().add(0.0D, boss.getBbHeight() * 0.55D, 0.0D).add(forward.scale(1.2D));
        TelegraphVfx.roarWave(boss, origin, 1.0D, 10, Math.max(4.0D, radius * 0.75D));
    }

    private static void knockBackNearbyPlayers(DraugrBoss boss, double radius) {
        if (!(boss.level() instanceof ServerLevel level) || radius <= 0.0D) {
            return;
        }
        Vec3 origin = boss.position().add(0.0D, boss.getBbHeight() * 0.5D, 0.0D);
        AABB box = new AABB(origin, origin).inflate(radius);
        double radiusSqr = radius * radius;
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, box)) {
            Vec3 playerCenter = player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D);
            if (player.isCreative() || player.isSpectator() || playerCenter.distanceToSqr(origin) > radiusSqr) {
                continue;
            }
            Vec3 direction = playerCenter.subtract(origin);
            direction = new Vec3(direction.x, 0.0D, direction.z);
            if (direction.lengthSqr() < 1.0E-4D) {
                direction = horizontalLook(boss);
            }
            Vec3 motion = direction.normalize()
                    .scale(ROAR_KNOCKBACK_HORIZONTAL)
                    .add(0.0D, ROAR_KNOCKBACK_VERTICAL, 0.0D);
            player.setDeltaMovement(motion);
            player.hurtMarked = true;
            player.connection.send(new ClientboundSetEntityMotionPacket(player));
        }
    }

    private static Vec3 horizontalLook(LivingEntity entity) {
        Vec3 direction = entity.getLookAngle();
        direction = new Vec3(direction.x, 0.0D, direction.z);
        if (direction.lengthSqr() < 1.0E-4D) {
            direction = new Vec3(-Mth.sin(entity.getYRot() * ((float) Math.PI / 180.0F)), 0.0D,
                    Mth.cos(entity.getYRot() * ((float) Math.PI / 180.0F)));
        }
        if (direction.lengthSqr() < 1.0E-4D) {
            return new Vec3(1.0D, 0.0D, 0.0D);
        }
        return direction.normalize();
    }
}
