package com.starfantasy.soulsfirecontrol.combat.nightprowler;

import com.starfantasy.soulsfirecontrol.StarFantasySoulsFireControl;
import com.starfantasy.soulsfirecontrol.combat.effect.ConfiguredMobEffect;
import com.starfantasy.soulsfirecontrol.config.ChaosMonarchConfig;
import com.starfantasy.soulsfirecontrol.entity.NightProwlerLightningAoeEntity;
import com.starfantasy.soulsfirecontrol.util.DayStalkerTweaks;
import com.starfantasy.soulsfirecontrol.util.NightProwlerTweaks;
import com.starfantasy.soulsfirecontrol.vfx.telegraph.TelegraphVfx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.soulsweaponry.entity.mobs.DayStalker;
import net.soulsweaponry.entity.mobs.NightProwler;
import net.soulsweaponry.entity.mobs.WarmthEntity;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = StarFantasySoulsFireControl.MOD_ID)
public final class NightProwlerReactionLightningManager {
    private static final int WARNING_TICKS = 30;
    private static final int DAMAGE_DELAY_TICKS = WARNING_TICKS + 1;
    private static final int VISUAL_DURATION_TICKS = 14;
    private static final double PHASE_TWO_AOE_RADIUS = 2.0D;
    private static final double LOW_HEALTH_AOE_RADIUS = 5.0D;
    private static final float AOE_DAMAGE = 20.0F;
    private static final List<PendingTrigger> PENDING_TRIGGERS = new java.util.ArrayList<>();
    private static final List<PendingAoe> PENDING_AOES = new java.util.ArrayList<>();

    private NightProwlerReactionLightningManager() {
    }

    public static void trigger(NightProwler boss) {
        if (!canTrigger(boss)) {
            return;
        }
        LivingEntity target = boss.getTarget();
        if (!shouldSkipTarget(boss, target)) {
            spawnWarningAtTarget(boss, target);
        }
    }

    public static void triggerDelayed(NightProwler boss, int delayTicks) {
        if (!canTrigger(boss)) {
            return;
        }
        if (delayTicks <= 0) {
            trigger(boss);
            return;
        }
        PENDING_TRIGGERS.add(new PendingTrigger(boss.level().dimension(), boss.getUUID(), delayTicks));
    }

    @SubscribeEvent
    public static void tickPending(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) {
            return;
        }
        Iterator<PendingTrigger> triggerIterator = PENDING_TRIGGERS.iterator();
        while (triggerIterator.hasNext()) {
            PendingTrigger trigger = triggerIterator.next();
            if (!trigger.dimension.equals(level.dimension())) {
                continue;
            }
            --trigger.delayTicks;
            if (trigger.delayTicks > 0) {
                continue;
            }
            Entity entity = level.getEntity(trigger.bossUuid);
            if (entity instanceof NightProwler boss) {
                trigger(boss);
            }
            triggerIterator.remove();
        }

        Iterator<PendingAoe> aoeIterator = PENDING_AOES.iterator();
        while (aoeIterator.hasNext()) {
            PendingAoe aoe = aoeIterator.next();
            if (!aoe.dimension.equals(level.dimension())) {
                continue;
            }
            --aoe.delayTicks;
            if (aoe.delayTicks > 0) {
                continue;
            }
            detonate(level, aoe);
            aoeIterator.remove();
        }
    }

    private static boolean canTrigger(NightProwler boss) {
        return boss != null
                && !boss.level().isClientSide()
                && boss.isAlive()
                && boss.isPhaseTwo();
    }

    private static void spawnWarningAtTarget(NightProwler boss, LivingEntity target) {
        double radius = radiusFor(boss);
        Vec3 center = groundCenterAt(boss.level(), target.getX(),
                Math.max(target.getY(), boss.getY()) + 8.0D,
                target.getZ(), Mth.floor(target.getX()), Mth.floor(target.getZ()));
        TelegraphVfx.redGroundWarningCircle(boss, center.add(0.0D, 0.06D, 0.0D),
                WARNING_TICKS, radius);
        PENDING_AOES.add(new PendingAoe(boss.level().dimension(), boss.getUUID(), center,
                radius, DAMAGE_DELAY_TICKS));
    }

    private static void detonate(ServerLevel level, PendingAoe aoe) {
        Entity entity = level.getEntity(aoe.bossUuid);
        if (!(entity instanceof NightProwler boss) || !boss.isAlive()) {
            return;
        }
        spawnVisuals(level, aoe.center, aoe.radius);
        DamageSource source = boss.damageSources().indirectMagic(boss, boss);
        List<ConfiguredMobEffect> effects = ConfiguredMobEffect.parseList(
                ChaosMonarchConfig.getNightProwlerReactionAoeHitEffects());
        AABB searchBox = new AABB(aoe.center, aoe.center).inflate(aoe.radius);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, searchBox)) {
            if (shouldSkipTarget(boss, target)
                    || !intersectsSphere(target.getBoundingBox(), aoe.center, aoe.radius)) {
                continue;
            }
            if (NightProwlerTweaks.hurtWithoutGuardBreak(boss, target, source, AOE_DAMAGE)) {
                applyEffects(target, effects);
            }
        }
    }

    private static void spawnVisuals(ServerLevel level, Vec3 center, double radius) {
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
        if (lightning != null) {
            lightning.moveTo(center.x, center.y, center.z);
            lightning.setVisualOnly(true);
            level.addFreshEntity(lightning);
        }
        level.playSound(null, center.x, center.y, center.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0F, 1.0F);
        level.addFreshEntity(new NightProwlerLightningAoeEntity(level, center,
                VISUAL_DURATION_TICKS, (float) radius));
    }

    private static double radiusFor(NightProwler boss) {
        return boss.getHealth() <= boss.getMaxHealth() * 0.5F
                ? LOW_HEALTH_AOE_RADIUS
                : PHASE_TWO_AOE_RADIUS;
    }

    private static void applyEffects(LivingEntity target, List<ConfiguredMobEffect> effects) {
        for (ConfiguredMobEffect effect : effects) {
            MobEffectInstance current = target.getEffect(effect.effect());
            if (current == null || current.getAmplifier() < effect.amplifier()
                    || current.getDuration() < effect.durationTicks()) {
                target.addEffect(effect.createInstance());
            }
        }
    }

    private static boolean shouldSkipTarget(NightProwler boss, LivingEntity target) {
        if (target == null || target == boss || !target.isAlive() || target.isInvulnerable()
                || target instanceof NightProwler || target instanceof DayStalker
                || target instanceof WarmthEntity || boss.isPartner(target)
                || DayStalkerTweaks.areWarmthAllies(boss, target)
                || NightProwlerTweaks.areSummonAllies(boss, target)) {
            return true;
        }
        return target instanceof Player player && (player.isCreative() || player.isSpectator());
    }

    private static Vec3 groundCenterAt(Level level, double preciseX, double searchY, double preciseZ,
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

    private static final class PendingTrigger {
        private final net.minecraft.resources.ResourceKey<Level> dimension;
        private final UUID bossUuid;
        private int delayTicks;

        private PendingTrigger(net.minecraft.resources.ResourceKey<Level> dimension, UUID bossUuid, int delayTicks) {
            this.dimension = dimension;
            this.bossUuid = bossUuid;
            this.delayTicks = delayTicks;
        }
    }

    private static final class PendingAoe {
        private final net.minecraft.resources.ResourceKey<Level> dimension;
        private final UUID bossUuid;
        private final Vec3 center;
        private final double radius;
        private int delayTicks;

        private PendingAoe(net.minecraft.resources.ResourceKey<Level> dimension, UUID bossUuid,
                           Vec3 center, double radius, int delayTicks) {
            this.dimension = dimension;
            this.bossUuid = bossUuid;
            this.center = center;
            this.radius = radius;
            this.delayTicks = delayTicks;
        }
    }
}
