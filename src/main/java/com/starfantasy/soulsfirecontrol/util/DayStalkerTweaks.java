package com.starfantasy.soulsfirecontrol.util;

import com.starfantasy.soulsfirecontrol.vfx.telegraph.TelegraphVfx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.soulsweaponry.entity.mobs.DayStalker;
import net.soulsweaponry.entity.mobs.NightProwler;
import net.soulsweaponry.entity.mobs.WarmthEntity;
import net.soulsweaponry.util.WeaponUtil;

import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

public final class DayStalkerTweaks {
    public static final String WARMTH_SUMMON_TAG = "starfantasy_day_stalker_warmth_summon";
    public static final String NO_LOOT_WARMTH_TAG = "starfantasy_day_stalker_no_loot_warmth";
    public static final String WARMTH_GROUP_TAG_PREFIX = "starfantasy_day_stalker_warmth_group_";
    public static final int CONFLAGRATION_GROWING_FIREBALL_GROWTH_TICKS = 36;
    public static final int CONFLAGRATION_PHASE_TWO_TOTAL_TICKS = 56;
    public static final int SKY_HIGH_WARNING_START = 106;
    public static final int SKY_HIGH_TRACKING_TICKS = 20;
    public static final int SKY_HIGH_LOCK_FRAME = SKY_HIGH_WARNING_START + SKY_HIGH_TRACKING_TICKS;
    public static final int SKY_HIGH_LANDING_FRAME = 142;
    public static final int SKY_HIGH_HIT_FRAME = 146;
    public static final int FLAMES_EDGE_AFTERSHOCK_COUNT = 20;
    public static final int FLAMES_EDGE_AFTERSHOCK_INTERVAL_TICKS = 1;
    public static final int FLAMES_EDGE_AFTERSHOCK_WARNING_TICKS = 20;
    public static final int OVERHEAT_FIRST_PILLAR_HIT_FRAME = 68;
    private static final int MAX_WARNING_TICKS = 40;
    private static final int WARMUP_DAMAGE_DELAY_TICKS = 7;
    private static final int INFERNO_FIRST_WARNING_START = 30;
    private static final int INFERNO_SECOND_WARNING_START = 85;
    private static final double WARNING_HEIGHT_FRACTION = 0.55D;
    private static final double MIN_WARNING_HEIGHT = 1.2D;
    private static final double MAX_WARNING_HEIGHT = 2.8D;
    private static final double BOSS_RING_RADIUS_SCALE = 4.0D / 3.0D;
    private static final double MELEE_RADIUS = 4.8D;
    private static final double HEAVY_MELEE_RADIUS = 6.4D;
    private static final double RANGED_RADIUS = 4.4D;
    private static final double LARGE_RANGED_RADIUS = 8.0D;
    private static final double FLAME_PILLAR_GROUND_RADIUS = 2.5D;
    private static final int OVERHEAT_PILLAR_SPAWN_DELAY_TICKS = 10;
    private static final double INFERNO_FIRST_DAMAGE_RADIUS = 12.0D;
    private static final double INFERNO_SECOND_DAMAGE_RADIUS = 16.0D;
    private static final double INFERNO_FIRST_VISUAL_SIZE = INFERNO_FIRST_DAMAGE_RADIUS / 2.0D;
    private static final double INFERNO_SECOND_VISUAL_SIZE = INFERNO_SECOND_DAMAGE_RADIUS / 2.0D;
    private static final float INFERNO_FIRST_DAMAGE = 85.0F;
    private static final float INFERNO_SECOND_DAMAGE = 113.0F;
    private static final float SKY_HIGH_SIMULATED_FALL_DISTANCE = 30.0F;
    private static final double SKY_HIGH_DAMAGE_RADIUS = SKY_HIGH_SIMULATED_FALL_DISTANCE / 2.5D;
    private static final float SKY_HIGH_DAMAGE = 20.0F + SKY_HIGH_SIMULATED_FALL_DISTANCE;
    private static final double FLAMES_EDGE_AFTERSHOCK_DISTANCE = 8.0D;
    private static final double FLAMES_EDGE_AFTERSHOCK_RADIUS = 2.0D;
    private static final double FLAMES_EDGE_AFTERSHOCK_VISUAL_SIZE = 4.0D;
    private static final float FLAMES_EDGE_AFTERSHOCK_DAMAGE = 40.0F;
    private static final String WARMTH_BOSS_UUID_KEY = "StarfantasyDayStalkerUuid";
    private static final String WARMTH_FIREBALL_EXPLODED_KEY = "StarfantasyWarmthFireballExploded";
    private static final String BLAZE_BARRAGE_FIREBALL_TAG = "starfantasy_day_stalker_phase_two_blaze_barrage";
    private static final String BLAZE_BARRAGE_FIREBALL_EXPLODED_KEY = "StarfantasyBlazeBarrageFireballExploded";
    private static final int WARMTH_LIFETIME_TICKS = 600;
    private static final float WARMTH_HEAL_FRACTION = 0.1F;
    private static final double WARMTH_FIREBALL_EXPLOSION_RADIUS = 2.0D;
    private static final double WARMTH_FIREBALL_EXPLOSION_VISUAL_SIZE = 4.0D;
    private static final float WARMTH_FIREBALL_EXPLOSION_DAMAGE = 10.0F;
    private static final double BLAZE_BARRAGE_EXPLOSION_RADIUS = 2.0D;
    private static final double BLAZE_BARRAGE_EXPLOSION_VISUAL_SIZE = 4.0D;
    private static final float BLAZE_BARRAGE_EXPLOSION_DAMAGE = 20.0F;
    private static final double SUMMON_SEARCH_RADIUS = 128.0D;
    private static final Set<UUID> WARMTH_SUMMON_RESETS = new HashSet<>();
    private static final ThreadLocal<DayStalker> SUPPRESSED_GUARD_BREAK_SOURCE = new ThreadLocal<>();

    private DayStalkerTweaks() {
    }

    public static void warnAirCombustion(DayStalker boss, int attackStatus) {
        warn(boss, attackStatus, 25, 0, true, RANGED_RADIUS);
    }

    public static void warnDecimate(DayStalker boss, int attackStatus) {
        warn(boss, attackStatus, 29, 0, false, MELEE_RADIUS);
        warn(boss, attackStatus, 42, 29, true, RANGED_RADIUS);
    }

    public static void warnDawnbreaker(DayStalker boss, int attackStatus) {
        warnCombo(boss, attackStatus, new int[]{11, 21, 32}, false, MELEE_RADIUS);
    }

    public static void warnChaosStorm(DayStalker boss, int attackStatus) {
        // Chaos Storm damage comes from spawned FlamePillars, so warnings are drawn at each pillar position.
    }

    public static void warnFlamethrower(DayStalker boss, int attackStatus) {
        if (boss.isPhaseTwo()) {
            warnCombo(boss, attackStatus, new int[]{18, 27, 41, 50, 58, 63}, false, MELEE_RADIUS + 1.0D);
            return;
        }
        warn(boss, attackStatus, 20, 0, true, RANGED_RADIUS);
    }

    public static void warnSunfireRush(DayStalker boss, int attackStatus) {
        warnCombo(boss, attackStatus, new int[]{45, 60, 71, 83, 96}, true, RANGED_RADIUS);
    }

    public static void warnConflagration(DayStalker boss, int attackStatus) {
        if (boss.isPhaseTwo()) {
            // The hook runs before DayStalkerGoal increments attackStatus; 14 is the frame that creates the fireball.
            if (attackStatus == 14) {
                warnFixedDuration(boss, CONFLAGRATION_GROWING_FIREBALL_GROWTH_TICKS, true, LARGE_RANGED_RADIUS);
            }
            return;
        }
        warnCombo(boss, attackStatus, new int[]{20, 24, 28, 32, 36, 40}, true, RANGED_RADIUS);
    }

    public static void warnBlazeBarrage(DayStalker boss, int attackStatus) {
        // The original barrage fires too quickly for a useful boss-body warning.
    }

    public static void attachBlazeBarrageOwner(DayStalker boss, Entity entity) {
        if (boss == null || entity == null) {
            return;
        }
        if (entity instanceof Projectile projectile && projectile.getOwner() == null) {
            projectile.setOwner(boss);
        }
        if (boss.isPhaseTwo() && entity instanceof SmallFireball) {
            entity.addTag(BLAZE_BARRAGE_FIREBALL_TAG);
        }
    }

    public static boolean hurtWithBossMagic(DayStalker boss, LivingEntity target, float damage) {
        return boss != null && target != null && target.hurt(boss.damageSources().indirectMagic(boss, boss), damage);
    }

    public static String warmthSummonGroupTag(Entity boss) {
        return WARMTH_GROUP_TAG_PREFIX + boss.getUUID();
    }

    public static void addWarmthSummonTags(DayStalker boss, WarmthEntity warmth) {
        if (boss == null || warmth == null) {
            return;
        }
        String groupTag = warmthSummonGroupTag(boss);
        boss.addTag(groupTag);
        warmth.addTag(WARMTH_SUMMON_TAG);
        warmth.addTag(NO_LOOT_WARMTH_TAG);
        warmth.addTag(groupTag);
        warmth.getPersistentData().putUUID(WARMTH_BOSS_UUID_KEY, boss.getUUID());
    }

    public static boolean hasActiveWarmthSummons(DayStalker boss) {
        if (boss == null || boss.level().isClientSide) {
            return false;
        }
        String groupTag = warmthSummonGroupTag(boss);
        AABB searchBox = boss.getBoundingBox().inflate(SUMMON_SEARCH_RADIUS);
        return !boss.level().getEntities(boss, searchBox,
                entity -> entity.isAlive()
                        && entity instanceof WarmthEntity
                        && entity.getTags().contains(groupTag)).isEmpty();
    }

    public static void discardWarmthSummons(DayStalker boss) {
        if (boss == null || boss.level().isClientSide) {
            return;
        }
        String groupTag = warmthSummonGroupTag(boss);
        AABB searchBox = boss.getBoundingBox().inflate(SUMMON_SEARCH_RADIUS);
        for (Entity entity : boss.level().getEntities(boss, searchBox,
                entity -> entity.getTags().contains(groupTag) && entity instanceof WarmthEntity)) {
            entity.discard();
        }
    }

    public static boolean areWarmthAllies(Entity first, Entity second) {
        if (first == null || second == null || first == second) {
            return false;
        }
        if (first instanceof DayStalker boss && second instanceof WarmthEntity warmth) {
            return isWarmthOwnedBy(boss, warmth);
        }
        if (second instanceof DayStalker boss && first instanceof WarmthEntity warmth) {
            return isWarmthOwnedBy(boss, warmth);
        }
        if (first instanceof WarmthEntity && second instanceof WarmthEntity) {
            return shareWarmthGroupTag(first, second);
        }
        return false;
    }

    public static void tickSummonedWarmth(WarmthEntity warmth) {
        if (warmth == null || !(warmth.level() instanceof Level level) || level.isClientSide
                || !warmth.getTags().contains(WARMTH_SUMMON_TAG)) {
            return;
        }
        DayStalker boss = resolveWarmthBoss(warmth);
        if (boss != null && !boss.isAlive()) {
            warmth.discard();
            return;
        }
        if (warmth.tickCount < WARMTH_LIFETIME_TICKS) {
            return;
        }
        if (boss != null && boss.isAlive()) {
            boss.heal(boss.getMaxHealth() * WARMTH_HEAL_FRACTION);
            playWarmthHealParticles(warmth, boss);
            if (boss.getHealth() > boss.getMaxHealth() * 0.5F) {
                WARMTH_SUMMON_RESETS.add(boss.getUUID());
            }
        }
        warmth.discard();
    }

    public static boolean consumeWarmthSummonReset(DayStalker boss) {
        return boss != null && WARMTH_SUMMON_RESETS.remove(boss.getUUID());
    }

    public static void detonateWarmthFireball(SmallFireball fireball, Vec3 center) {
        if (fireball == null || center == null || fireball.level().isClientSide) {
            return;
        }
        Entity owner = fireball.getOwner();
        if (!(owner instanceof WarmthEntity warmth)
                || !warmth.getTags().contains(WARMTH_SUMMON_TAG)) {
            return;
        }
        CompoundTag data = fireball.getPersistentData();
        if (data.getBoolean(WARMTH_FIREBALL_EXPLODED_KEY)) {
            return;
        }
        data.putBoolean(WARMTH_FIREBALL_EXPLODED_KEY, true);
        DayStalker boss = resolveWarmthBoss(warmth);
        if (boss == null || !boss.isAlive()) {
            return;
        }
        if (!(fireball.level() instanceof ServerLevel level)) {
            return;
        }
        level.playSound(null, center.x, center.y, center.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0F, 1.0F);
        TelegraphVfx.swordExplosion(boss, center, WARMTH_FIREBALL_EXPLOSION_VISUAL_SIZE);
        DamageSource source = boss.damageSources().indirectMagic(fireball, boss);
        AABB searchBox = new AABB(center, center).inflate(WARMTH_FIREBALL_EXPLOSION_RADIUS);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, searchBox)) {
            if (shouldSkipWarmthExplosionTarget(boss, target)
                    || !intersectsSphere(target.getBoundingBox(), center, WARMTH_FIREBALL_EXPLOSION_RADIUS)) {
                continue;
            }
            target.hurt(source, WARMTH_FIREBALL_EXPLOSION_DAMAGE);
        }
        fireball.discard();
    }

    public static void detonateBlazeBarrageFireball(SmallFireball fireball, Vec3 center) {
        if (fireball == null || center == null || fireball.level().isClientSide
                || !fireball.getTags().contains(BLAZE_BARRAGE_FIREBALL_TAG)) {
            return;
        }
        CompoundTag data = fireball.getPersistentData();
        if (data.getBoolean(BLAZE_BARRAGE_FIREBALL_EXPLODED_KEY)) {
            return;
        }
        data.putBoolean(BLAZE_BARRAGE_FIREBALL_EXPLODED_KEY, true);
        Entity owner = fireball.getOwner();
        if (!(owner instanceof DayStalker boss) || !boss.isAlive()
                || !(fireball.level() instanceof ServerLevel level)) {
            return;
        }
        level.playSound(null, center.x, center.y, center.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0F, 1.0F);
        TelegraphVfx.swordExplosion(boss, center, BLAZE_BARRAGE_EXPLOSION_VISUAL_SIZE);
        DamageSource source = boss.damageSources().indirectMagic(fireball, boss);
        AABB searchBox = new AABB(center, center).inflate(BLAZE_BARRAGE_EXPLOSION_RADIUS);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, searchBox)) {
            if (shouldSkipWarmthExplosionTarget(boss, target)
                    || !intersectsSphere(target.getBoundingBox(), center, BLAZE_BARRAGE_EXPLOSION_RADIUS)) {
                continue;
            }
            target.hurt(source, BLAZE_BARRAGE_EXPLOSION_DAMAGE);
        }
        fireball.discard();
    }

    public static void warnFlamesEdge(DayStalker boss, int attackStatus) {
        warn(boss, attackStatus, boss.isPhaseTwo() ? 41 : 22, 0, false, HEAVY_MELEE_RADIUS);
    }

    public static void warnRadiance(DayStalker boss, int attackStatus) {
        if (attackStatus >= 0 && attackStatus < 20 && attackStatus % 2 == 0) {
            Vec3 origin = boss.position().add(0.0D, boss.getBbHeight() + 0.25D, 0.0D);
            TelegraphVfx.roarWave(boss, origin, 1.4D, 12, 9.5D);
        }
        warn(boss, attackStatus, 80, 0, false, HEAVY_MELEE_RADIUS);
    }

    public static void warnWarmth(DayStalker boss, int attackStatus) {
        // Summoning has no immediate damage frame, so it should not draw a warning.
    }

    public static void warnOverheat(DayStalker boss, int attackStatus) {
        warn(boss, attackStatus, OVERHEAT_FIRST_PILLAR_HIT_FRAME,
                OVERHEAT_FIRST_PILLAR_HIT_FRAME - 30, true, LARGE_RANGED_RADIUS);
    }

    public static void warnInferno(DayStalker boss, int attackStatus) {
        if (attackStatus == INFERNO_FIRST_WARNING_START) {
            warnFixedDuration(boss, 40, false, INFERNO_FIRST_DAMAGE_RADIUS / BOSS_RING_RADIUS_SCALE);
            TelegraphVfx.groundWarningCircleTrackingGround(boss, 40, INFERNO_FIRST_DAMAGE_RADIUS, false);
        }
        if (attackStatus == INFERNO_SECOND_WARNING_START) {
            warnFixedDuration(boss, 40, false, INFERNO_SECOND_DAMAGE_RADIUS / BOSS_RING_RADIUS_SCALE);
            TelegraphVfx.groundWarningCircleTrackingGround(boss, 40, INFERNO_SECOND_DAMAGE_RADIUS, false);
        }
    }

    public static void detonateInferno(DayStalker boss, boolean secondExplosion) {
        double radius = secondExplosion ? INFERNO_SECOND_DAMAGE_RADIUS : INFERNO_FIRST_DAMAGE_RADIUS;
        double visualSize = secondExplosion ? INFERNO_SECOND_VISUAL_SIZE : INFERNO_FIRST_VISUAL_SIZE;
        float damage = secondExplosion ? INFERNO_SECOND_DAMAGE : INFERNO_FIRST_DAMAGE;
        Vec3 center = groundCenterBelow(boss);
        playInfernoVisuals(boss, center, radius, visualSize);
        dealFixedInfernoDamage(boss, center, radius, damage);
    }

    public static void warnSkyHighStart(DayStalker boss, LivingEntity target) {
        warnFixedDuration(boss, SKY_HIGH_HIT_FRAME - SKY_HIGH_WARNING_START, true, HEAVY_MELEE_RADIUS);
        if (target != null) {
            TelegraphVfx.groundWarningCircleTrackingGroundThenFreeze(target,
                    SKY_HIGH_HIT_FRAME - SKY_HIGH_WARNING_START, SKY_HIGH_TRACKING_TICKS,
                    SKY_HIGH_DAMAGE_RADIUS, true);
        }
    }

    public static double skyHighDamageRadius() {
        return SKY_HIGH_DAMAGE_RADIUS;
    }

    public static float skyHighDamage() {
        return SKY_HIGH_DAMAGE;
    }

    public static float skyHighParticleScale() {
        return SKY_HIGH_SIMULATED_FALL_DISTANCE / 10.0F;
    }

    public static void warnFlamesReach(DayStalker boss, int attackStatus) {
        warn(boss, attackStatus, 29, 0, false, MELEE_RADIUS);
        warn(boss, attackStatus, 43, 29, false, HEAVY_MELEE_RADIUS);
    }

    public static Vec3 flamesEdgeAftershockCenter(DayStalker boss, int index) {
        double angle = -(Math.PI * 2.0D) * index / FLAMES_EDGE_AFTERSHOCK_COUNT;
        Vec3 desired = boss.position().add(
                Math.cos(angle) * FLAMES_EDGE_AFTERSHOCK_DISTANCE,
                0.0D,
                Math.sin(angle) * FLAMES_EDGE_AFTERSHOCK_DISTANCE);
        return groundCenterAt(boss.level(), desired.x, boss.getY() + 3.0D, desired.z,
                Mth.floor(desired.x), Mth.floor(desired.z));
    }

    public static void warnFlamesEdgeAftershock(DayStalker boss, Vec3 center) {
        if (center == null) {
            return;
        }
        TelegraphVfx.redGroundWarningCircle(boss, center.add(0.0D, 0.06D, 0.0D),
                FLAMES_EDGE_AFTERSHOCK_WARNING_TICKS, FLAMES_EDGE_AFTERSHOCK_RADIUS);
    }

    public static void detonateFlamesEdgeAftershock(DayStalker boss, Vec3 center) {
        if (!(boss.level() instanceof ServerLevel level) || center == null) {
            return;
        }
        level.playSound(null, center.x, center.y, center.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0F, 1.0F);
        TelegraphVfx.swordExplosion(boss, center, FLAMES_EDGE_AFTERSHOCK_VISUAL_SIZE);
        TelegraphVfx.horizontalRoarWave(boss, center, 1.0D, 14, FLAMES_EDGE_AFTERSHOCK_VISUAL_SIZE);
        DamageSource source = boss.damageSources().indirectMagic(boss, boss);
        AABB searchBox = new AABB(center, center).inflate(FLAMES_EDGE_AFTERSHOCK_RADIUS);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, searchBox)) {
            if (target == boss || !target.isAlive() || target.isInvulnerable()
                    || target instanceof WarmthEntity || boss.isPartner(target)
                    || !intersectsSphere(target.getBoundingBox(), center, FLAMES_EDGE_AFTERSHOCK_RADIUS)) {
                continue;
            }
            hurtWithoutGuardBreak(boss, target, source, FLAMES_EDGE_AFTERSHOCK_DAMAGE);
        }
    }

    public static void warnFlamePillarGround(DayStalker boss, Vec3 center, int warmup) {
        warnFlamePillarGround(boss, center, warmup, 0);
    }

    public static void warnOverheatPillars(DayStalker boss, float yaw) {
        Level level = boss.level();
        Vec3 origin = boss.position();
        float rad = (float) Math.toRadians(yaw);
        Vec3 forward = new Vec3(Math.cos(rad), 0.0D, Math.sin(rad));
        Vec3 rightDir = forward.cross(new Vec3(0.0D, 1.0D, 0.0D)).normalize();
        for (int i = -4; i <= 4; ++i) {
            Vec3 start = origin.add(rightDir.scale(i * 2.0F));
            int lineIndex = i;
            WeaponUtil.doConsumerOnLine(level, yaw, start, 10.0D, 20, 2.0F,
                    (vec, warmup, lineYaw) -> warnFlamePillarGround(boss, vec,
                            -6 + Math.abs(lineIndex * 6), OVERHEAT_PILLAR_SPAWN_DELAY_TICKS));
        }
    }

    private static void warnFlamePillarGround(DayStalker boss, Vec3 center, int warmup, int extraDelayTicks) {
        if (center == null) {
            return;
        }
        int ticks = Math.max(1, extraDelayTicks + warmup + WARMUP_DAMAGE_DELAY_TICKS);
        TelegraphVfx.redGroundWarningCircle(boss, center.add(0.0D, 0.06D, 0.0D), ticks, FLAME_PILLAR_GROUND_RADIUS);
    }

    public static boolean rewardsGuardBreak(DayStalker boss) {
        if (!boss.isPhaseTwo()) {
            return false;
        }
        return switch (boss.getAttackAnimation()) {
            case DECIMATE, DAWNBREAKER, FLAMETHROWER, FLAMES_EDGE, RADIANCE, INFERNO, SKY_HIGH, FLAMES_REACH -> true;
            default -> false;
        };
    }

    public static boolean suppressesGuardBreak(DayStalker boss) {
        return boss != null && SUPPRESSED_GUARD_BREAK_SOURCE.get() == boss;
    }

    public static boolean hurtWithoutGuardBreak(DayStalker boss, LivingEntity target,
                                                 DamageSource source, float damage) {
        SUPPRESSED_GUARD_BREAK_SOURCE.set(boss);
        try {
            return target.hurt(source, damage);
        } finally {
            SUPPRESSED_GUARD_BREAK_SOURCE.remove();
        }
    }

    private static boolean shouldSkipWarmthExplosionTarget(DayStalker boss, LivingEntity target) {
        if (target == null || target == boss || !target.isAlive() || target.isInvulnerable()
                || target instanceof DayStalker || target instanceof NightProwler
                || target instanceof WarmthEntity || boss.isPartner(target)) {
            return true;
        }
        return target instanceof Player player && (player.isCreative() || player.isSpectator());
    }

    private static boolean isWarmthOwnedBy(DayStalker boss, WarmthEntity warmth) {
        if (boss == null || warmth == null) {
            return false;
        }
        CompoundTag data = warmth.getPersistentData();
        if (data.hasUUID(WARMTH_BOSS_UUID_KEY)
                && boss.getUUID().equals(data.getUUID(WARMTH_BOSS_UUID_KEY))) {
            return true;
        }
        return boss.getTags().contains(warmthSummonGroupTag(boss))
                && warmth.getTags().contains(warmthSummonGroupTag(boss));
    }

    private static boolean shareWarmthGroupTag(Entity first, Entity second) {
        for (String tag : first.getTags()) {
            if (tag.startsWith(WARMTH_GROUP_TAG_PREFIX)
                    && second.getTags().contains(tag)) {
                return true;
            }
        }
        return false;
    }

    private static DayStalker resolveWarmthBoss(WarmthEntity warmth) {
        if (!(warmth.level() instanceof ServerLevel level)) {
            return null;
        }
        CompoundTag data = warmth.getPersistentData();
        if (!data.hasUUID(WARMTH_BOSS_UUID_KEY)) {
            return null;
        }
        UUID bossUuid = data.getUUID(WARMTH_BOSS_UUID_KEY);
        Entity entity = level.getEntity(bossUuid);
        return entity instanceof DayStalker boss ? boss : null;
    }

    private static void playWarmthHealParticles(WarmthEntity warmth, DayStalker boss) {
        if (!(warmth.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 start = warmth.position().add(0.0D, warmth.getBbHeight() * 0.5D, 0.0D);
        Vec3 end = boss.position().add(0.0D, boss.getBbHeight() * 0.65D, 0.0D);
        Vec3 delta = end.subtract(start);
        int steps = 16;
        for (int i = 0; i < steps; ++i) {
            double progress = (double) i / (double) Math.max(1, steps - 1);
            Vec3 pos = start.add(delta.scale(progress));
            level.sendParticles(ParticleTypes.FLAME,
                    pos.x, pos.y, pos.z,
                    1,
                    0.04D, 0.04D, 0.04D,
                    0.02D);
        }
    }

    private static void playInfernoVisuals(DayStalker boss, Vec3 center, double radius, double visualSize) {
        if (!(boss.level() instanceof ServerLevel level)) {
            return;
        }
        int explosionCount = Math.max(1, Mth.floor(radius / 2.0D));
        double ringRadius = radius / 2.0D;
        for (int i = 0; i < explosionCount; ++i) {
            double angle = (Math.PI * 2.0D) * i / explosionCount;
            Vec3 explosionPos = center.add(Math.cos(angle) * ringRadius, 0.0D, Math.sin(angle) * ringRadius);
            level.playSound(null, explosionPos.x, explosionPos.y, explosionPos.z,
                    SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.25F, 0.88F);
            TelegraphVfx.swordExplosion(boss, explosionPos, visualSize);
        }
        TelegraphVfx.horizontalRoarWave(boss, center, 1.0D, 14, radius * 2.0D);
    }

    private static void dealFixedInfernoDamage(DayStalker boss, Vec3 center, double radius, float damage) {
        if (!(boss.level() instanceof ServerLevel level) || damage <= 0.0F || radius <= 0.0D) {
            return;
        }
        DamageSource source = boss.damageSources().indirectMagic(boss, boss);
        AABB searchBox = new AABB(center, center).inflate(radius);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, searchBox)) {
            if (target == boss || !target.isAlive() || target.isInvulnerable()
                    || target instanceof WarmthEntity || boss.isPartner(target)
                    || !intersectsSphere(target.getBoundingBox(), center, radius)) {
                continue;
            }
            target.hurt(source, damage);
        }
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

    public static Vec3 groundCenterBelow(LivingEntity entity) {
        Level level = entity.level();
        int x = Mth.floor(entity.getX());
        int z = Mth.floor(entity.getZ());
        return groundCenterAt(level, entity.getX(), entity.getY(), entity.getZ(), x, z);
    }

    public static void snapToGroundAndSync(DayStalker boss, Vec3 ground) {
        if (ground == null) {
            return;
        }
        double y = ground.y() - 0.06D;
        boss.moveTo(ground.x(), y, ground.z(), boss.getYRot(), boss.getXRot());
        boss.setFlying(false);
        boss.setDeltaMovement(Vec3.ZERO);
        boss.fallDistance = 0.0F;
        boss.hurtMarked = true;
        if (boss.level() instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().broadcast(boss, new ClientboundTeleportEntityPacket(boss));
        }
    }

    public static void forceVerticalKnockup(LivingEntity target, double yVelocity) {
        Vec3 movement = target.getDeltaMovement();
        target.setDeltaMovement(movement.x, Math.max(movement.y, yVelocity), movement.z);
        target.hasImpulse = true;
        target.hurtMarked = true;
    }

    public static void teleportNearTarget(DayStalker boss, LivingEntity target, double distanceFromTarget) {
        if (target == null || !target.isAlive()) {
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
        Vec3 ground = groundCenterAt(boss.level(), desired.x, Math.max(target.getY(), boss.getY()) + 3.0D, desired.z,
                Mth.floor(desired.x), Mth.floor(desired.z));
        double yaw = Math.toDegrees(Math.atan2(target.getZ() - ground.z, target.getX() - ground.x)) - 90.0D;
        boss.moveTo(ground.x, ground.y - 0.06D, ground.z, (float) yaw, boss.getXRot());
        boss.setDeltaMovement(Vec3.ZERO);
        boss.fallDistance = 0.0F;
        boss.hurtMarked = true;
        if (boss.level() instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().broadcast(boss, new ClientboundTeleportEntityPacket(boss));
        }
    }

    public static void teleportBehindTarget(DayStalker boss, LivingEntity target, double distanceBehindTarget) {
        if (target == null || !target.isAlive()) {
            return;
        }
        Vec3 horizontalLook = target.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        if (horizontalLook.lengthSqr() < 1.0E-4D) {
            double yawRadians = Math.toRadians(target.getYRot());
            horizontalLook = new Vec3(-Math.sin(yawRadians), 0.0D, Math.cos(yawRadians));
        }
        Vec3 desired = target.position().subtract(horizontalLook.normalize().scale(distanceBehindTarget));
        Vec3 ground = groundCenterAt(boss.level(), desired.x, Math.max(target.getY(), boss.getY()) + 3.0D, desired.z,
                Mth.floor(desired.x), Mth.floor(desired.z));
        double yaw = Math.toDegrees(Math.atan2(target.getZ() - ground.z, target.getX() - ground.x)) - 90.0D;
        boss.moveTo(ground.x, ground.y - 0.06D, ground.z, (float) yaw, boss.getXRot());
        boss.setDeltaMovement(Vec3.ZERO);
        boss.fallDistance = 0.0F;
        boss.hurtMarked = true;
        if (boss.level() instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().broadcast(boss, new ClientboundTeleportEntityPacket(boss));
        }
    }

    private static Vec3 groundCenterAt(Level level, double preciseX, double searchY, double preciseZ, int blockX, int blockZ) {
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

    private static void warnCombo(DayStalker boss, int attackStatus, int[] hitFrames,
                                  boolean red, double radius) {
        int previousFrame = 0;
        for (int hitFrame : hitFrames) {
            warn(boss, attackStatus, hitFrame, previousFrame, red, radius);
            previousFrame = hitFrame;
        }
    }

    private static void warn(DayStalker boss, int attackStatus, int hitFrame, int previousFrame,
                             boolean red, double radius) {
        int startFrame = Math.max(previousFrame, hitFrame - MAX_WARNING_TICKS);
        if (attackStatus != startFrame) {
            return;
        }
        int warningTicks = Math.max(1, hitFrame - startFrame);
        warnFixedDuration(boss, warningTicks, red, radius);
    }

    private static void warnFixedDuration(DayStalker boss, int warningTicks, boolean red, double radius) {
        double height = Math.max(MIN_WARNING_HEIGHT,
                Math.min(MAX_WARNING_HEIGHT, boss.getBbHeight() * WARNING_HEIGHT_FRACTION));
        double scaledRadius = radius * BOSS_RING_RADIUS_SCALE;
        if (red || !boss.isPhaseTwo()) {
            TelegraphVfx.redAttackWarningRing(boss, warningTicks, scaledRadius, height);
        } else {
            TelegraphVfx.attackWarningRing(boss, warningTicks, scaledRadius, height);
        }
    }
}
