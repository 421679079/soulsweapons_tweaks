package com.starfantasy.soulsfirecontrol.vfx.telegraph;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class TelegraphVfx {
    private static final double MAX_ENCODED_HEIGHT = 9.0D;
    private static final double TRACKING_SENTINEL = 1000.0D;
    private static final double TRACKING_FREEZE_SCALE = 1000.0D;

    private TelegraphVfx() {
    }

    public static void attackWarningRing(LivingEntity entity, int ticks, double radius, double height) {
        if (!(entity.level() instanceof ServerLevel level) || ticks <= 0 || radius <= 0.0D) {
            return;
        }
        sendAttackWarningRing(level, entity.getX(), entity.getY() + height, entity.getZ(),
                ticks, radius, entity.getId() + encodeHeight(height));
    }

    public static void redAttackWarningRing(LivingEntity entity, int ticks, double radius, double height) {
        if (!(entity.level() instanceof ServerLevel level) || ticks <= 0 || radius <= 0.0D) {
            return;
        }
        sendAttackWarningRing(level, entity.getX(), entity.getY() + height, entity.getZ(),
                ticks, -radius, entity.getId() + encodeHeight(height));
    }

    public static void attackWarningRingAt(Entity source, Vec3 origin, int ticks, double radius, boolean red) {
        if (!(source.level() instanceof ServerLevel level) || ticks <= 0 || radius <= 0.0D) {
            return;
        }
        sendAttackWarningRing(level, origin.x, origin.y, origin.z, ticks, red ? -radius : radius, 0.0D);
    }

    private static void sendAttackWarningRing(ServerLevel level, double x, double y, double z,
                                              int ticks, double encodedRadius, double trackingData) {
        level.sendParticles(
                TelegraphParticleRegistry.ATTACK_WARNING_RING.get(),
                x,
                y,
                z,
                0,
                encodedRadius,
                ticks,
                trackingData,
                1.0D
        );
    }

    public static void roarWave(Entity source, Vec3 origin, double startSize, int ticks, double endSize) {
        if (!(source.level() instanceof ServerLevel level) || ticks <= 0 || endSize <= 0.0D) {
            return;
        }
        level.sendParticles(
                TelegraphParticleRegistry.ROAR_WAVE.get(),
                origin.x,
                origin.y,
                origin.z,
                0,
                startSize,
                ticks,
                endSize,
                1.0D
        );
    }

    public static void horizontalRoarWave(Entity source, Vec3 origin, double startSize, int ticks, double endSize) {
        if (!(source.level() instanceof ServerLevel level) || ticks <= 0 || endSize <= 0.0D) {
            return;
        }
        level.sendParticles(
                TelegraphParticleRegistry.ROAR_WAVE.get(),
                origin.x,
                origin.y,
                origin.z,
                0,
                -Math.abs(startSize),
                ticks,
                endSize,
                1.0D
        );
    }

    public static void swordExplosion(Entity source, Vec3 origin, double size) {
        if (!(source.level() instanceof ServerLevel level) || size <= 0.0D) {
            return;
        }
        level.sendParticles(
                TelegraphParticleRegistry.SWORD_EXPLOSION.get(),
                origin.x,
                origin.y,
                origin.z,
                0,
                size,
                0.0D,
                0.0D,
                1.0D
        );
    }

    public static void groundWarningRectangle(Entity source, Vec3 center, int ticks,
                                              double halfWidth, double halfLength) {
        if (!(source.level() instanceof ServerLevel level) || ticks <= 0
                || halfWidth <= 0.0D || halfLength <= 0.0D) {
            return;
        }
        sendGroundWarningRectangle(level, center, ticks, halfWidth, halfLength);
    }

    public static void groundWarningCircle(Entity source, Vec3 center, int ticks, double radius) {
        if (!(source.level() instanceof ServerLevel level) || ticks <= 0 || radius <= 0.0D) {
            return;
        }
        sendGroundWarningRectangle(level, center, ticks, radius, radius);
    }

    public static void redGroundWarningCircle(Entity source, Vec3 center, int ticks, double radius) {
        if (!(source.level() instanceof ServerLevel level) || ticks <= 0 || radius <= 0.0D) {
            return;
        }
        sendGroundWarningRectangle(level, center, ticks, -radius, radius);
    }

    public static void groundWarningCircleTracking(LivingEntity target, int ticks, double radius, boolean red) {
        if (!(target.level() instanceof ServerLevel level) || ticks <= 0 || radius <= 0.0D) {
            return;
        }
        double encodedRadius = red ? -radius : radius;
        sendGroundWarningRectangle(level,
                target.position().add(0.0D, 0.06D, 0.0D),
                ticks,
                encodedRadius,
                TRACKING_SENTINEL + target.getId());
    }

    public static void groundWarningCircleTrackingGround(LivingEntity target, int ticks, double radius, boolean red) {
        if (!(target.level() instanceof ServerLevel level) || ticks <= 0 || radius <= 0.0D) {
            return;
        }
        double encodedRadius = red ? -radius : radius;
        sendGroundWarningRectangle(level,
                target.position().add(0.0D, 0.06D, 0.0D),
                ticks,
                encodedRadius,
                -(TRACKING_SENTINEL + target.getId()));
    }

    public static void groundWarningCircleTrackingGroundThenFreeze(LivingEntity target, int ticks,
                                                                   int trackingTicks, double radius, boolean red) {
        if (!(target.level() instanceof ServerLevel level) || ticks <= 0 || trackingTicks <= 0 || radius <= 0.0D) {
            return;
        }
        int freezeAfter = Math.max(1, Math.min(trackingTicks, ticks));
        double encodedRadius = red ? -radius : radius;
        double trackingData = TRACKING_SENTINEL + target.getId() + (double) freezeAfter / TRACKING_FREEZE_SCALE;
        sendGroundWarningRectangle(level,
                target.position().add(0.0D, 0.06D, 0.0D),
                ticks,
                encodedRadius,
                -trackingData);
    }

    private static void sendGroundWarningRectangle(ServerLevel level, Vec3 center, int ticks,
                                                   double encodedHalfWidth, double halfLength) {
        level.sendParticles(
                TelegraphParticleRegistry.GROUND_WARNING_RECTANGLE.get(),
                center.x,
                center.y,
                center.z,
                0,
                encodedHalfWidth,
                ticks,
                halfLength,
                1.0D
        );
    }

    private static double encodeHeight(double height) {
        return Math.max(0.0D, Math.min(MAX_ENCODED_HEIGHT, height)) / 10.0D;
    }
}
