package com.starfantasy.soulsfirecontrol.combat.chaosmonarch;

import com.starfantasy.soulsfirecontrol.StarFantasySoulsFireControl;
import com.starfantasy.soulsfirecontrol.combat.effect.ConfiguredMobEffect;
import com.starfantasy.soulsfirecontrol.config.ChaosMonarchConfig;
import com.starfantasy.soulsfirecontrol.entity.NightProwlerLightningAoeEntity;
import com.starfantasy.soulsfirecontrol.entity.TwinMeteorEntity;
import com.starfantasy.soulsfirecontrol.entity.TwinMeteorEntityRegistry;
import com.starfantasy.soulsfirecontrol.mixin.BossEntityAccessor;
import com.starfantasy.soulsfirecontrol.util.ChaosMonarchTweaks;
import com.starfantasy.soulsfirecontrol.vfx.telegraph.TelegraphVfx;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.soulsweaponry.entity.mobs.ChaosMonarch;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = StarFantasySoulsFireControl.MOD_ID)
public final class ChaosMonarchAmbienceManager {
    private static final String COOLDOWN_KEY = "starfantasy_chaos_monarch_ambience_cooldown";
    private static final String PHASE_SIX_SEQUENCE_KEY = "starfantasy_chaos_monarch_ambience_phase_six_sequence";
    private static final String VISUAL_DAMAGE_CANCEL_TAG = "starfantasy_chaos_monarch_ambience_visual";

    private static final ResourceLocation GOETY_FANG = new ResourceLocation("goety", "fang");

    private static final int NORMAL_INTERVAL_TICKS = 20;
    private static final int PHASE_SIX_INTERVAL_TICKS = 4;
    private static final int AUDIENCE_RECHECK_TICKS = 20;
    private static final double IMPACT_DISTANCE_MIN = 12.0D;
    private static final double IMPACT_DISTANCE_MAX = 18.0D;
    private static final double IMPACT_RADIUS = 2.0D;
    private static final float METEOR_DAMAGE = 20.0F;
    private static final float SLAM_DAMAGE = 20.0F;
    private static final float FANG_DAMAGE = 10.0F;
    private static final int SLAM_VISUAL_DURATION_TICKS = 14;

    private static final List<PendingAction> PENDING_ACTIONS = new ArrayList<>();

    private ChaosMonarchAmbienceManager() {
    }

    public static void tick(ChaosMonarch boss) {
        if (boss.level().isClientSide() || !boss.isAlive()
                || ChaosMonarchPhaseManager.isTransitioning(boss)) {
            return;
        }
        CompoundTag data = boss.getPersistentData();
        int cooldown = data.getInt(COOLDOWN_KEY);
        if (cooldown > 0) {
            data.putInt(COOLDOWN_KEY, cooldown - 1);
            return;
        }
        if (!hasAudience(boss)) {
            data.putInt(COOLDOWN_KEY, AUDIENCE_RECHECK_TICKS);
            return;
        }

        int phase = ChaosMonarchPhaseManager.getCurrentPhase(boss);
        int effectPhase = phase >= 6 ? nextPhaseSixEffect(data) : Math.max(1, Math.min(5, phase));
        spawnPhaseEffect(boss, effectPhase);
        data.putInt(COOLDOWN_KEY, phase >= 6 ? PHASE_SIX_INTERVAL_TICKS : NORMAL_INTERVAL_TICKS);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void cancelVisualNativeDamage(LivingAttackEvent event) {
        Entity direct = event.getSource().getDirectEntity();
        Entity source = event.getSource().getEntity();
        if ((direct != null && direct.getTags().contains(VISUAL_DAMAGE_CANCEL_TAG))
                || (source != null && source.getTags().contains(VISUAL_DAMAGE_CANCEL_TAG))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void tickPending(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) {
            return;
        }
        List<PendingAction> dueActions = new ArrayList<>();
        Iterator<PendingAction> iterator = PENDING_ACTIONS.iterator();
        while (iterator.hasNext()) {
            PendingAction action = iterator.next();
            if (!action.dimension.equals(level.dimension())) {
                continue;
            }
            --action.delayTicks;
            if (action.delayTicks > 0) {
                continue;
            }
            dueActions.add(action);
            iterator.remove();
        }
        for (PendingAction action : dueActions) {
            runAction(level, action);
        }
    }

    private static int nextPhaseSixEffect(CompoundTag data) {
        int sequence = Math.floorMod(data.getInt(PHASE_SIX_SEQUENCE_KEY), 5);
        data.putInt(PHASE_SIX_SEQUENCE_KEY, sequence + 1);
        return sequence + 1;
    }

    private static void spawnPhaseEffect(ChaosMonarch boss, int effectPhase) {
        Vec3 impact = randomImpactAroundBoss(boss);
        int warningTicks = Math.max(1, ChaosMonarchConfig.getTwinBossMeteorWarningTicks());
        switch (effectPhase) {
            case 1 -> spawnMeteor(boss, impact, warningTicks,
                    TwinMeteorEntityRegistry.DAY_STALKER_METEOR.get(), 1);
            case 2 -> spawnMeteor(boss, impact, warningTicks,
                    TwinMeteorEntityRegistry.CHAOS_FROST_METEOR.get(), 2);
            case 3 -> spawnSlamWarning(boss, impact, warningTicks);
            case 4 -> spawnMeteor(boss, impact, warningTicks,
                    TwinMeteorEntityRegistry.NIGHT_PROWLER_METEOR.get(), 4);
            case 5 -> spawnFangWarning(boss, impact, warningTicks);
            default -> {
            }
        }
    }

    private static void spawnMeteor(ChaosMonarch boss, Vec3 impact, int warningTicks,
                                    EntityType<TwinMeteorEntity> meteorType, int effectPhase) {
        if (!(boss.level() instanceof ServerLevel level)) {
            return;
        }
        TwinMeteorEntity meteor = meteorType.create(level);
        if (meteor == null) {
            return;
        }
        Vec3 start = impact.add(0.0D, ChaosMonarchConfig.getTwinBossMeteorSpawnYOffset(), 0.0D);
        meteor.moveTo(start.x, start.y, start.z, boss.getYRot(), boss.getXRot());
        meteor.configureChaos(boss, impact, warningTicks, effectPhase, METEOR_DAMAGE);
        level.addFreshEntity(meteor);
        TelegraphVfx.redGroundWarningCircle(boss, impact.add(0.0D, 0.06D, 0.0D),
                warningTicks, IMPACT_RADIUS);
    }

    private static void spawnSlamWarning(ChaosMonarch boss, Vec3 impact, int warningTicks) {
        TelegraphVfx.redGroundWarningCircle(boss, impact.add(0.0D, 0.06D, 0.0D),
                warningTicks, IMPACT_RADIUS);
        PENDING_ACTIONS.add(PendingAction.slam(boss, impact, 3, warningTicks));
    }

    private static void spawnFangWarning(ChaosMonarch boss, Vec3 impact, int warningTicks) {
        TelegraphVfx.purpleGroundWarningCircle(boss, impact.add(0.0D, 0.06D, 0.0D),
                warningTicks, IMPACT_RADIUS);
        PENDING_ACTIONS.add(PendingAction.fang(boss, impact, 5, warningTicks));
    }

    private static void runAction(ServerLevel level, PendingAction action) {
        Entity entity = level.getEntity(action.bossUuid);
        if (!(entity instanceof ChaosMonarch boss) || !boss.isAlive()) {
            return;
        }
        switch (action.kind) {
            case SLAM -> detonateSlam(level, boss, action.center, action.effectPhase);
            case FANG -> detonateFang(level, boss, action.center, action.effectPhase);
        }
    }

    private static void detonateSlam(ServerLevel level, ChaosMonarch boss, Vec3 center, int effectPhase) {
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
        if (lightning != null) {
            lightning.moveTo(center.x, center.y, center.z);
            lightning.setVisualOnly(true);
            level.addFreshEntity(lightning);
        }
        level.playSound(null, center.x, center.y, center.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0F, 1.0F);
        level.addFreshEntity(new NightProwlerLightningAoeEntity(level, center,
                SLAM_VISUAL_DURATION_TICKS, (float) IMPACT_RADIUS));
        DamageSource source = boss.damageSources().indirectMagic(boss, boss);
        damageTargets(level, boss, center, source, SLAM_DAMAGE, effectPhase);
    }

    private static void detonateFang(ServerLevel level, ChaosMonarch boss, Vec3 center, int effectPhase) {
        spawnFangVisual(level, boss, center);
        DamageSource source = boss.damageSources().fellOutOfWorld();
        damageTargets(level, boss, center, source, FANG_DAMAGE, effectPhase);
    }

    private static void damageTargets(ServerLevel level, ChaosMonarch boss, Vec3 center,
                                      DamageSource source, float damage, int effectPhase) {
        List<ConfiguredMobEffect> effects = ConfiguredMobEffect.parseList(
                ChaosMonarchConfig.getChaosMonarchLightningHitEffects(effectPhase));
        AABB searchBox = new AABB(center, center).inflate(IMPACT_RADIUS);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, searchBox)) {
            if (target instanceof ChaosMonarch || ChaosMonarchTweaks.shouldSkipTarget(boss, target)
                    || !intersectsSphere(target.getBoundingBox(), center, IMPACT_RADIUS)) {
                continue;
            }
            if (ChaosMonarchTweaks.hurtWithoutGuardBreak(boss, target, source, damage)) {
                applyEffects(target, effects);
            }
        }
    }

    private static void spawnFangVisual(ServerLevel level, ChaosMonarch boss, Vec3 center) {
        Entity visual = createEntity(level, GOETY_FANG);
        if (visual == null) {
            return;
        }
        visual.addTag(VISUAL_DAMAGE_CANCEL_TAG);
        visual.moveTo(center.x, center.y, center.z, boss.getYRot(), 0.0F);
        invokeIfPresent(visual, "setOwner", LivingEntity.class, boss);
        invokeIfPresent(visual, "setDamage", int.class, 0);
        invokeIfPresent(visual, "setBurning", int.class, 0);
        invokeIfPresent(visual, "setSoulEater", int.class, 0);
        setPrivateInt(visual, "warmupDelayTicks", 0);
        level.addFreshEntity(visual);
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

    private static boolean hasAudience(ChaosMonarch boss) {
        LivingEntity target = boss.getTarget();
        if (target instanceof Player player && !isIgnoredPlayer(player)) {
            return true;
        }
        return !collectPlayers(boss, ChaosMonarchConfig.getChaosMonarchNearbyPlayerDebuffRadius()).isEmpty();
    }

    private static Set<ServerPlayer> collectPlayers(ChaosMonarch boss, double radius) {
        Set<ServerPlayer> players = new LinkedHashSet<>();
        if (!(boss.level() instanceof ServerLevel level)) {
            return players;
        }
        try {
            ServerBossEvent bossBar = ((BossEntityAccessor) boss).starfantasy$getBossBar();
            players.addAll(bossBar.getPlayers());
        } catch (ClassCastException ignored) {
        }
        AABB searchBox = boss.getBoundingBox().inflate(radius);
        players.addAll(level.getEntitiesOfClass(ServerPlayer.class, searchBox));
        players.removeIf(ChaosMonarchAmbienceManager::isIgnoredPlayer);
        return players;
    }

    private static boolean isIgnoredPlayer(Player player) {
        return player.isSpectator() || player.isCreative() || !player.isAlive();
    }

    private static Vec3 randomImpactAroundBoss(ChaosMonarch boss) {
        RandomSource random = boss.getRandom();
        double distance = IMPACT_DISTANCE_MIN
                + random.nextDouble() * Math.max(0.0D, IMPACT_DISTANCE_MAX - IMPACT_DISTANCE_MIN);
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double x = boss.getX() + Math.cos(angle) * distance;
        double z = boss.getZ() + Math.sin(angle) * distance;
        return ChaosMonarchTweaks.groundCenterAt(boss.level(), x,
                boss.getY() + ChaosMonarchConfig.getTwinBossMeteorSpawnYOffset(),
                z, Mth.floor(x), Mth.floor(z));
    }

    @Nullable
    private static Entity createEntity(ServerLevel level, ResourceLocation id) {
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(id);
        return type == null ? null : type.create(level);
    }

    private static void invokeIfPresent(Entity entity, String methodName, Class<?> parameterType, Object value) {
        try {
            Method method = entity.getClass().getMethod(methodName, parameterType);
            method.invoke(entity, value);
        } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
        }
    }

    private static void setPrivateInt(Entity entity, String fieldName, int value) {
        try {
            Field field = entity.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.setInt(entity, value);
        } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
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

    private enum ActionKind {
        SLAM,
        FANG
    }

    private static final class PendingAction {
        private final ActionKind kind;
        private final ResourceKey<Level> dimension;
        private final UUID bossUuid;
        private final Vec3 center;
        private final int effectPhase;
        private int delayTicks;

        private PendingAction(ActionKind kind, ResourceKey<Level> dimension, UUID bossUuid,
                              Vec3 center, int effectPhase, int delayTicks) {
            this.kind = kind;
            this.dimension = dimension;
            this.bossUuid = bossUuid;
            this.center = center;
            this.effectPhase = effectPhase;
            this.delayTicks = delayTicks;
        }

        private static PendingAction slam(ChaosMonarch boss, Vec3 center, int effectPhase, int delayTicks) {
            return new PendingAction(ActionKind.SLAM, boss.level().dimension(), boss.getUUID(),
                    center, effectPhase, delayTicks);
        }

        private static PendingAction fang(ChaosMonarch boss, Vec3 center, int effectPhase, int delayTicks) {
            return new PendingAction(ActionKind.FANG, boss.level().dimension(), boss.getUUID(),
                    center, effectPhase, delayTicks);
        }
    }
}
