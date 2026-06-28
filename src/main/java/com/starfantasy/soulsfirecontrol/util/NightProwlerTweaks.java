package com.starfantasy.soulsfirecontrol.util;

import com.starfantasy.soulsfirecontrol.vfx.telegraph.TelegraphVfx;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.soulsweaponry.entity.mobs.NightProwler;

public final class NightProwlerTweaks {
    public static final String USED_SOULMASS_SUMMON_TAG = "starfantasy_night_prowler_used_soulmass_summon";
    public static final String NO_LOOT_SOULMASS_TAG = "starfantasy_night_prowler_no_loot_soulmass";
    public static final String SUMMON_ALLY_TAG = "starfantasy_night_prowler_summon_ally";
    public static final String SUMMON_ALLY_GROUP_TAG_PREFIX = "starfantasy_night_prowler_summon_group_";

    private static final int MAX_WARNING_TICKS = 40;
    private static final int WARMUP_DAMAGE_DELAY_TICKS = 7;
    private static final double WARNING_HEIGHT_FRACTION = 0.55D;
    private static final double MIN_WARNING_HEIGHT = 1.2D;
    private static final double MAX_WARNING_HEIGHT = 2.6D;
    private static final double BOSS_RING_RADIUS_SCALE = 4.0D / 3.0D;
    private static final double MELEE_RADIUS = 4.8D;
    private static final double HEAVY_MELEE_RADIUS = 6.0D;
    private static final double RANGED_RADIUS = 4.2D;
    private static final double LARGE_RANGED_RADIUS = 8.0D;
    private static final double BLACKFLAME_GROUND_RADIUS = 2.0D;
    private static final double NIGHTS_EDGE_GROUND_RADIUS = 1.6D;
    private static final double ECLIPSE_SKULL_GROUND_RADIUS = 2.0D;
    private static final double BLACKFLAME_LANDING_RADIUS = 4.0D;
    private static final double SUMMON_SEARCH_RADIUS = 128.0D;
    private static final ThreadLocal<NightProwler> SUPPRESSED_GUARD_BREAK_SOURCE = new ThreadLocal<>();

    private NightProwlerTweaks() {
    }

    public static void warnTrinity(NightProwler boss, int attackStatus) {
        warn(boss, attackStatus, boss.isPhaseTwo() ? 65 : 77, 0, false, HEAVY_MELEE_RADIUS);
    }

    public static void warnReapingSlash(NightProwler boss, int attackStatus) {
        warn(boss, attackStatus, 26, 0, false, MELEE_RADIUS);
    }

    public static void warnNightsEmbrace(NightProwler boss, int attackStatus) {
        // Summoning has no immediate damage frame, so it should not draw a boss warning.
    }

    public static void warnRippleFang(NightProwler boss, int attackStatus) {
        warn(boss, attackStatus, 39, 0, true, RANGED_RADIUS);
    }

    public static void warnBladesReach(NightProwler boss, int attackStatus) {
        int swordDamageFrame = boss.isPhaseTwo() ? 23 : 30;
        warn(boss, attackStatus, swordDamageFrame, 0, true, RANGED_RADIUS);
        if (boss.isPhaseTwo()) {
            warn(boss, attackStatus, 43, swordDamageFrame, true, RANGED_RADIUS);
        }
    }

    public static void warnSoulReaper(NightProwler boss, int attackStatus) {
        int[] frames = boss.isPhaseTwo()
                ? new int[]{17, 28, 50, 61, 74, 96, 109, 128}
                : new int[]{14, 29, 49, 72};
        warnCombo(boss, attackStatus, frames, false, MELEE_RADIUS);
    }

    public static void warnDiminishingLight(NightProwler boss, int attackStatus) {
        if (boss.isPhaseTwo()) {
            warnCombo(boss, attackStatus, new int[]{20, 30, 40}, true, RANGED_RADIUS);
            return;
        }
        warn(boss, attackStatus, 25, 0, true, RANGED_RADIUS);
    }

    public static void warnDarknessRise(NightProwler boss, int attackStatus) {
        // The blackflame ring pattern draws ground warnings for every explosion.
    }

    public static void warnEclipse(NightProwler boss, int attackStatus) {
        // ECLIPSE damage comes from spawned skulls; each skull draws its own landing warning.
    }

    public static void warnEngulf(NightProwler boss, int attackStatus) {
        int fogFrame = boss.isPhaseTwo() ? 19 : 21;
        warn(boss, attackStatus, fogFrame, 0, true, RANGED_RADIUS);
        if (boss.isPhaseTwo()) {
            warn(boss, attackStatus, 54, 34, false, MELEE_RADIUS);
        }
    }

    public static void warnBlackflameSnake(NightProwler boss, int attackStatus) {
        if (boss.isPhaseTwo()) {
            warn(boss, attackStatus, 84, 0, false, HEAVY_MELEE_RADIUS);
            return;
        }
        warn(boss, attackStatus, 31, 0, true, RANGED_RADIUS);
    }

    public static void warnBlackflameSnakeLanding(NightProwler boss, LivingEntity target, int attackStatus) {
        if (boss.isPhaseTwo() && attackStatus == 44) {
            TelegraphVfx.groundWarningCircleTracking(target, 40, BLACKFLAME_LANDING_RADIUS, false);
        }
    }

    public static void warnBlackflameSnakeLandingAt(NightProwler boss, Vec3 targetPos, int attackStatus) {
        if (boss.isPhaseTwo() && targetPos != null && attackStatus == 44) {
            TelegraphVfx.groundWarningRectangle(boss, targetPos.add(0.0D, 0.06D, 0.0D),
                    40, BLACKFLAME_LANDING_RADIUS, BLACKFLAME_LANDING_RADIUS);
        }
    }

    public static void warnLunarDisplacement(NightProwler boss, int attackStatus) {
        warn(boss, attackStatus, 42, 0, false, MELEE_RADIUS);
        if (attackStatus >= 0 && attackStatus < 20 && attackStatus % 2 == 0) {
            Vec3 origin = boss.position().add(0.0D, boss.getBbHeight() + 0.25D, 0.0D);
            TelegraphVfx.roarWave(boss, origin, 1.4D, 12, 9.5D);
        }
    }

    public static void warnDeathsGrasp(NightProwler boss, int attackStatus) {
        int firstFrame = boss.isPhaseTwo() ? 18 : 23;
        warn(boss, attackStatus, firstFrame, 0, false, MELEE_RADIUS);
        if (boss.isPhaseTwo()) {
            warn(boss, attackStatus, 43, firstFrame, true, RANGED_RADIUS);
        }
    }

    public static boolean rewardsGuardBreak(NightProwler boss) {
        if (!boss.isPhaseTwo()) {
            return false;
        }
        return switch (boss.getAttackAnimation()) {
            case TRINITY, SOUL_REAPER, ENGULF, BLACKFLAME_SNAKE, LUNAR_DISPLACEMENT, DEATHBRINGERS_GRASP -> true;
            default -> false;
        };
    }

    public static boolean suppressesGuardBreak(NightProwler boss) {
        return boss != null && SUPPRESSED_GUARD_BREAK_SOURCE.get() == boss;
    }

    public static boolean hurtWithoutGuardBreak(NightProwler boss, LivingEntity target,
                                                DamageSource source, float damage) {
        SUPPRESSED_GUARD_BREAK_SOURCE.set(boss);
        try {
            return target.hurt(source, damage);
        } finally {
            SUPPRESSED_GUARD_BREAK_SOURCE.remove();
        }
    }

    public static void warnBlackflameGround(NightProwler boss, Vec3 center, int ticks) {
        warnGround(boss, center, ticks + WARMUP_DAMAGE_DELAY_TICKS, BLACKFLAME_GROUND_RADIUS);
    }

    public static void warnNightsEdgeGround(NightProwler boss, Vec3 center, int ticks) {
        warnGround(boss, center, ticks + WARMUP_DAMAGE_DELAY_TICKS, NIGHTS_EDGE_GROUND_RADIUS);
    }

    public static void warnEclipseSkullLanding(NightProwler boss, Entity projectile) {
        Vec3 start = projectile.position();
        Vec3 velocity = projectile.getDeltaMovement();
        if (velocity.lengthSqr() < 1.0E-4D) {
            return;
        }
        Level level = projectile.level();
        Vec3 direction = velocity.normalize();
        Vec3 end = start.add(direction.scale(80.0D));
        HitResult hit = level.clip(new ClipContext(start, end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                projectile));
        Vec3 impact = hit.getType() == HitResult.Type.MISS
                ? findGroundBelow(level, end, projectile)
                : hit.getLocation();
        if (impact == null) {
            return;
        }
        int estimatedTicks = (int) Math.ceil(start.distanceTo(impact) / Math.max(0.1D, velocity.length()));
        warnGround(boss, impact, Math.max(8, Math.min(30, estimatedTicks)), ECLIPSE_SKULL_GROUND_RADIUS);
    }

    public static String summonAllyGroupTag(Entity boss) {
        return SUMMON_ALLY_GROUP_TAG_PREFIX + boss.getUUID();
    }

    public static void addSummonAllyTags(Entity entity, String groupTag) {
        entity.addTag(SUMMON_ALLY_TAG);
        entity.addTag(groupTag);
    }

    public static void copySummonAllyTags(Entity source, Entity target) {
        if (!source.getTags().contains(SUMMON_ALLY_TAG)) {
            return;
        }
        target.addTag(SUMMON_ALLY_TAG);
        for (String tag : source.getTags()) {
            if (tag.startsWith(SUMMON_ALLY_GROUP_TAG_PREFIX)) {
                target.addTag(tag);
            }
        }
    }

    public static boolean areSummonAllies(Entity first, Entity second) {
        if (first == null || second == null || first == second) {
            return false;
        }
        if (!first.getTags().contains(SUMMON_ALLY_TAG)
                || !second.getTags().contains(SUMMON_ALLY_TAG)) {
            return false;
        }
        for (String tag : first.getTags()) {
            if (tag.startsWith(SUMMON_ALLY_GROUP_TAG_PREFIX)
                    && second.getTags().contains(tag)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasActiveSummonAllies(Entity boss) {
        if (boss == null || boss.level().isClientSide) {
            return false;
        }
        String groupTag = summonAllyGroupTag(boss);
        return !boss.level().getEntities(boss, boss.getBoundingBox().inflate(SUMMON_SEARCH_RADIUS),
                entity -> entity.isAlive() && entity.getTags().contains(groupTag)).isEmpty();
    }

    public static void discardSummonAllies(Entity boss) {
        if (boss == null || boss.level().isClientSide) {
            return;
        }
        String groupTag = summonAllyGroupTag(boss);
        for (Entity entity : boss.level().getEntities(boss, boss.getBoundingBox().inflate(SUMMON_SEARCH_RADIUS),
                entity -> entity.getTags().contains(groupTag))) {
            entity.discard();
        }
    }

    private static void warnCombo(NightProwler boss, int attackStatus, int[] hitFrames,
                                  boolean red, double radius) {
        int previousFrame = 0;
        for (int hitFrame : hitFrames) {
            warn(boss, attackStatus, hitFrame, previousFrame, red, radius);
            previousFrame = hitFrame;
        }
    }

    private static void warn(NightProwler boss, int attackStatus, int hitFrame, int previousFrame,
                             boolean red, double radius) {
        int startFrame = Math.max(previousFrame, hitFrame - MAX_WARNING_TICKS);
        if (attackStatus != startFrame) {
            return;
        }
        int warningTicks = Math.max(1, hitFrame - startFrame);
        double height = Math.max(MIN_WARNING_HEIGHT,
                Math.min(MAX_WARNING_HEIGHT, boss.getBbHeight() * WARNING_HEIGHT_FRACTION));
        double scaledRadius = radius * BOSS_RING_RADIUS_SCALE;
        if (red || !boss.isPhaseTwo()) {
            TelegraphVfx.redAttackWarningRing(boss, warningTicks, scaledRadius, height);
        } else {
            TelegraphVfx.attackWarningRing(boss, warningTicks, scaledRadius, height);
        }
    }

    private static void warnGround(NightProwler boss, Vec3 center, int ticks, double radius) {
        if (center == null || ticks <= 0) {
            return;
        }
        TelegraphVfx.redGroundWarningCircle(boss, center.add(0.0D, 0.06D, 0.0D), ticks, radius);
    }

    private static Vec3 findGroundBelow(Level level, Vec3 start, Entity source) {
        Vec3 end = new Vec3(start.x, level.getMinBuildHeight(), start.z);
        HitResult hit = level.clip(new ClipContext(start, end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                source));
        return hit.getType() == HitResult.Type.MISS ? null : hit.getLocation();
    }
}
