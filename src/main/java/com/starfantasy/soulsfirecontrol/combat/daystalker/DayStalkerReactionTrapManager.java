package com.starfantasy.soulsfirecontrol.combat.daystalker;

import com.starfantasy.soulsfirecontrol.StarFantasySoulsFireControl;
import com.starfantasy.soulsfirecontrol.combat.effect.ConfiguredMobEffect;
import com.starfantasy.soulsfirecontrol.config.ChaosMonarchConfig;
import com.starfantasy.soulsfirecontrol.util.DayStalkerTweaks;
import com.starfantasy.soulsfirecontrol.util.NightProwlerTweaks;
import com.starfantasy.soulsfirecontrol.vfx.telegraph.TelegraphVfx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.soulsweaponry.entity.mobs.DayStalker;
import net.soulsweaponry.entity.mobs.NightProwler;
import net.soulsweaponry.entity.mobs.WarmthEntity;
import net.soulsweaponry.entity.projectile.noclip.DamagingWarmupEntity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = StarFantasySoulsFireControl.MOD_ID)
public final class DayStalkerReactionTrapManager {
    private static final String MANAGED_TRAP_TAG = "starfantasy_day_stalker_reaction_trap";
    private static final int WARNING_TICKS = 20;
    private static final int DAMAGE_DELAY_TICKS = WARNING_TICKS + 1;
    private static final int FLAME_PILLAR_WARMUP = WARNING_TICKS - 7;
    private static final double TRAP_RADIUS = 2.5D;
    private static final float TRAP_DAMAGE = 15.0F;
    private static final int[] CROSS_DISTANCES = new int[]{3, 6, 9};
    private static final int LOW_HEALTH_RING_COUNT = 18;
    private static final double LOW_HEALTH_RING_RADIUS = 9.0D;
    private static final ResourceLocation GOETY_FIRE_BLAST_TRAP = new ResourceLocation("goety", "fire_blast_trap");
    private static final List<PendingTrigger> PENDING_TRIGGERS = new ArrayList<>();
    private static final List<PendingTrap> PENDING_TRAPS = new ArrayList<>();

    private DayStalkerReactionTrapManager() {
    }

    public static void trigger(DayStalker boss) {
        if (boss == null || boss.level().isClientSide() || !boss.isAlive() || !boss.isPhaseTwo()) {
            return;
        }
        LivingEntity target = boss.getTarget();
        if (!shouldSkipTarget(boss, target)) {
            spawnPatternAtTarget(boss, target);
        }
    }

    public static void triggerDelayed(DayStalker boss, int delayTicks) {
        if (boss == null || boss.level().isClientSide() || !boss.isAlive() || !boss.isPhaseTwo()) {
            return;
        }
        if (delayTicks <= 0) {
            trigger(boss);
            return;
        }
        PENDING_TRIGGERS.add(new PendingTrigger(boss.level().dimension(), boss.getUUID(), delayTicks));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void cancelNativeManagedTrapDamage(LivingAttackEvent event) {
        if (hasManagedTrapTag(event.getSource())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void tickPendingTraps(TickEvent.LevelTickEvent event) {
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
            if (entity instanceof DayStalker boss) {
                trigger(boss);
            }
            triggerIterator.remove();
        }
        Iterator<PendingTrap> iterator = PENDING_TRAPS.iterator();
        while (iterator.hasNext()) {
            PendingTrap trap = iterator.next();
            if (!trap.dimension.equals(level.dimension())) {
                continue;
            }
            --trap.delayTicks;
            if (trap.delayTicks > 0) {
                continue;
            }
            detonate(level, trap);
            iterator.remove();
        }
    }

    private static void spawnPatternAtTarget(DayStalker boss, LivingEntity target) {
        Vec3 base = groundCenterAt(boss.level(), target.getX(),
                Math.max(target.getY(), boss.getY()) + 8.0D,
                target.getZ(), Mth.floor(target.getX()), Mth.floor(target.getZ()));
        Set<BlockPos> spawned = new LinkedHashSet<>();
        spawnTrapOnce(boss, base, spawned);
        for (int distance : CROSS_DISTANCES) {
            spawnTrapOnce(boss, base.add(distance, 0.0D, 0.0D), spawned);
            spawnTrapOnce(boss, base.add(-distance, 0.0D, 0.0D), spawned);
            spawnTrapOnce(boss, base.add(0.0D, 0.0D, distance), spawned);
            spawnTrapOnce(boss, base.add(0.0D, 0.0D, -distance), spawned);
        }
        if (boss.getHealth() > boss.getMaxHealth() * 0.5F) {
            return;
        }
        for (int i = 0; i < LOW_HEALTH_RING_COUNT; ++i) {
            double angle = (Math.PI * 2.0D) * i / LOW_HEALTH_RING_COUNT;
            spawnTrapOnce(boss, base.add(
                    Math.cos(angle) * LOW_HEALTH_RING_RADIUS,
                    0.0D,
                    Math.sin(angle) * LOW_HEALTH_RING_RADIUS), spawned);
        }
    }

    private static void spawnTrapOnce(DayStalker boss, Vec3 desired, Set<BlockPos> spawned) {
        Vec3 center = groundCenterAt(boss.level(), desired.x, desired.y + 8.0D, desired.z,
                Mth.floor(desired.x), Mth.floor(desired.z));
        BlockPos key = BlockPos.containing(center.x, center.y, center.z);
        if (!spawned.add(key)) {
            return;
        }
        TelegraphVfx.redGroundWarningCircle(boss, center.add(0.0D, 0.06D, 0.0D),
                WARNING_TICKS, TRAP_RADIUS);
        if (spawnVisualTrap(boss, center)) {
            PENDING_TRAPS.add(new PendingTrap(boss.level().dimension(), boss.getUUID(),
                    center, DAMAGE_DELAY_TICKS));
        }
    }

    private static boolean spawnVisualTrap(DayStalker boss, Vec3 center) {
        if (!(boss.level() instanceof ServerLevel level)) {
            return false;
        }
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(GOETY_FIRE_BLAST_TRAP);
        if (type == null) {
            return false;
        }
        Entity visual = type.create(level);
        if (visual == null) {
            return false;
        }
        visual.addTag(MANAGED_TRAP_TAG);
        visual.moveTo(center.x, center.y, center.z, boss.getYRot(), boss.getXRot());
        visual.setDeltaMovement(Vec3.ZERO);
        if (visual instanceof Projectile projectile) {
            projectile.setOwner(boss);
        }
        if (visual instanceof DamagingWarmupEntity damaging) {
            damaging.setOwner(boss);
            damaging.setRadius((float) TRAP_RADIUS);
            damaging.setWarmup(FLAME_PILLAR_WARMUP);
            damaging.setBaseDamage(0.0D);
            damaging.setEventId(-1);
            damaging.setParticleAmountMod(1.5F);
        }
        invokeIfPresent(visual, "setOwner", LivingEntity.class, boss);
        invokeIfPresent(visual, "setAreaOfEffect", float.class, 0.0F);
        invokeIfPresent(visual, "setExtraDamage", float.class, 0.0F);
        invokeIfPresent(visual, "setBurning", int.class, 0);
        invokeIfPresent(visual, "setImmediate", boolean.class, false);
        return level.addFreshEntity(visual);
    }

    private static void detonate(ServerLevel level, PendingTrap trap) {
        Entity entity = level.getEntity(trap.bossUuid);
        if (!(entity instanceof DayStalker boss) || !boss.isAlive()) {
            return;
        }
        DamageSource source = boss.damageSources().indirectMagic(boss, boss);
        List<ConfiguredMobEffect> effects = ConfiguredMobEffect.parseList(
                ChaosMonarchConfig.getDayStalkerReactionTrapHitEffects());
        AABB searchBox = new AABB(trap.center, trap.center).inflate(TRAP_RADIUS);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, searchBox)) {
            if (shouldSkipTarget(boss, target)
                    || !intersectsSphere(target.getBoundingBox(), trap.center, TRAP_RADIUS)) {
                continue;
            }
            if (DayStalkerTweaks.hurtWithoutGuardBreak(boss, target, source, TRAP_DAMAGE)) {
                applyEffects(target, effects);
            }
        }
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

    private static boolean shouldSkipTarget(DayStalker boss, LivingEntity target) {
        if (target == null || target == boss || !target.isAlive() || target.isInvulnerable()
                || target instanceof DayStalker || target instanceof NightProwler
                || target instanceof WarmthEntity || boss.isPartner(target)
                || DayStalkerTweaks.areWarmthAllies(boss, target)
                || NightProwlerTweaks.areSummonAllies(boss, target)) {
            return true;
        }
        return target instanceof Player player && (player.isCreative() || player.isSpectator());
    }

    private static boolean hasManagedTrapTag(DamageSource source) {
        return hasManagedTrapTag(source.getDirectEntity()) || hasManagedTrapTag(source.getEntity());
    }

    private static boolean hasManagedTrapTag(Entity entity) {
        return entity != null && entity.getTags().contains(MANAGED_TRAP_TAG);
    }

    private static void invokeIfPresent(Entity entity, String methodName, Class<?> parameterType, Object value) {
        try {
            Method method = entity.getClass().getMethod(methodName, parameterType);
            method.invoke(entity, value);
        } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
            // Optional support for soft-dependency trap entities.
        }
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

    private static final class PendingTrap {
        private final ResourceKey<Level> dimension;
        private final UUID bossUuid;
        private final Vec3 center;
        private int delayTicks;

        private PendingTrap(ResourceKey<Level> dimension, UUID bossUuid, Vec3 center, int delayTicks) {
            this.dimension = dimension;
            this.bossUuid = bossUuid;
            this.center = center;
            this.delayTicks = delayTicks;
        }
    }

    private static final class PendingTrigger {
        private final ResourceKey<Level> dimension;
        private final UUID bossUuid;
        private int delayTicks;

        private PendingTrigger(ResourceKey<Level> dimension, UUID bossUuid, int delayTicks) {
            this.dimension = dimension;
            this.bossUuid = bossUuid;
            this.delayTicks = delayTicks;
        }
    }
}
