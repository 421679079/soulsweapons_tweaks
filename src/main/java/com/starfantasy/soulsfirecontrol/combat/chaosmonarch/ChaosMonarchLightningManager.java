package com.starfantasy.soulsfirecontrol.combat.chaosmonarch;

import com.starfantasy.soulsfirecontrol.StarFantasySoulsFireControl;
import com.starfantasy.soulsfirecontrol.combat.effect.ConfiguredMobEffect;
import com.starfantasy.soulsfirecontrol.config.ChaosMonarchConfig;
import com.starfantasy.soulsfirecontrol.entity.NightProwlerLightningAoeEntity;
import com.starfantasy.soulsfirecontrol.util.ChaosMonarchTweaks;
import com.starfantasy.soulsfirecontrol.vfx.telegraph.TelegraphVfx;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
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
import net.soulsweaponry.entity.projectile.NightsEdge;
import net.soulsweaponry.registry.EntityRegistry;

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
public final class ChaosMonarchLightningManager {
    private static final String MANAGED_GOETY_TAG = "starfantasy_chaos_monarch_goety_lightning";
    private static final String BOSS_UUID_KEY = "StarfantasyChaosMonarchUuid";
    private static final String KIND_KEY = "StarfantasyChaosMonarchKind";
    private static final String EFFECT_PHASE_KEY = "StarfantasyChaosMonarchEffectPhase";

    private static final int KIND_FIRE_TRAP = 1;
    private static final int KIND_ICE_CHUNK = 2;
    private static final int KIND_VOID_RIFT = 4;
    private static final int KIND_NIGHTS_EDGE = 5;

    private static final int PHASE_SIX_ICE_START_TICKS = 40;
    private static final int PHASE_SIX_SLAM_START_TICKS = 145;
    private static final int PHASE_SIX_FANG_START_TICKS = 145;
    private static final int PHASE_SIX_VOID_RIFT_DURATION_TICKS = 200;

    private static final ResourceLocation FIRE_BLAST_TRAP = new ResourceLocation("goety", "fire_blast_trap");
    private static final ResourceLocation ICE_CHUNK = new ResourceLocation("goety", "ice_chunk");
    private static final ResourceLocation VOID_RIFT = new ResourceLocation("goety", "void_rift");
    private static final ResourceLocation GOETY_STUNNED = new ResourceLocation("goety", "stunned");

    private static final int FIRE_WARNING_TICKS = 20;
    private static final int FIRE_DAMAGE_DELAY_TICKS = FIRE_WARNING_TICKS + 1;
    private static final double FIRE_TRAP_RADIUS = 2.5D;
    private static final float FIRE_TRAP_DAMAGE = 20.0F;
    private static final int[] FIRE_CROSS_DISTANCES = {3, 6, 9};
    private static final int FIRE_RING_COUNT = 18;
    private static final double FIRE_RING_RADIUS = 9.0D;
    private static final int FIRE_RING_DELAY_TICKS = 20;

    private static final int ICE_COUNT = 5;
    private static final int ICE_INTERVAL_TICKS = 4;
    private static final double ICE_RING_RADIUS = 3.0D;
    private static final int ICE_WARNING_TICKS = 100;
    private static final double ICE_WARNING_RADIUS = 2.5D;
    private static final float ICE_DAMAGE = 20.0F;

    private static final double SLAM_AOE_RADIUS = 4.0D;
    private static final float SLAM_DAMAGE = 30.0F;
    private static final int SLAM_VISUAL_DURATION_TICKS = 14;
    private static final int SLAM_WARNING_TICKS = 60;
    private static final int[] SLAM_RING_DELAYS = {0, 20, 40};
    private static final double[] SLAM_RING_RADII = {0.0D, 12.0D, 24.0D};
    private static final int[] SLAM_RING_COUNTS = {1, 10, 20};

    private static final int VOID_RIFT_DURATION_TICKS = 60;
    private static final float VOID_RIFT_DAMAGE = 10.0F;

    private static final int NIGHTS_EDGE_COUNT = 16;
    private static final int NIGHTS_EDGE_INTERVAL_TICKS = 2;
    private static final int NIGHTS_EDGE_WARNING_TICKS = 20;
    private static final int NIGHTS_EDGE_DAMAGE_DELAY_TICKS = 7;
    private static final double NIGHTS_EDGE_WARNING_RADIUS = 1.6D;
    private static final float NIGHTS_EDGE_DAMAGE = 15.0F;

    private static final List<PendingAction> PENDING_ACTIONS = new ArrayList<>();
    private static final ThreadLocal<Boolean> APPLYING_MANAGED_DAMAGE = ThreadLocal.withInitial(() -> false);

    private ChaosMonarchLightningManager() {
    }

    public static void startLightning(ChaosMonarch boss, int phase, LivingEntity target) {
        if (boss == null || boss.level().isClientSide() || !boss.isAlive() || target == null || !target.isAlive()) {
            return;
        }
        if (phase >= 6) {
            spawnVoidRift(boss, PHASE_SIX_VOID_RIFT_DURATION_TICKS);
            startFireTrapPattern(boss, target);
            PENDING_ACTIONS.add(PendingAction.phaseStart(boss, target.getUUID(),
                    2, PHASE_SIX_ICE_START_TICKS));
            PENDING_ACTIONS.add(PendingAction.phaseStart(boss, target.getUUID(),
                    3, PHASE_SIX_SLAM_START_TICKS));
            PENDING_ACTIONS.add(PendingAction.phaseStart(boss, target.getUUID(),
                    5, PHASE_SIX_FANG_START_TICKS));
            return;
        }
        startPhase(boss, Math.max(1, Math.min(5, phase)), target);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void replaceManagedGoetyDamage(LivingAttackEvent event) {
        if (APPLYING_MANAGED_DAMAGE.get()) {
            return;
        }
        Entity managed = managedEntity(event.getSource().getDirectEntity());
        if (managed == null) {
            managed = managedEntity(event.getSource().getEntity());
        }
        if (managed == null) {
            return;
        }
        event.setCanceled(true);
        int kind = managed.getPersistentData().getInt(KIND_KEY);
        if (kind == KIND_FIRE_TRAP) {
            return;
        }
        if (!(managed.level() instanceof ServerLevel level)) {
            return;
        }
        LivingEntity target = event.getEntity();
        ChaosMonarch boss = resolveBoss(level, managed);
        if (boss == null || !boss.isAlive() || shouldSkipTarget(boss, target)) {
            return;
        }
        int effectPhase = managed.getPersistentData().getInt(EFFECT_PHASE_KEY);
        DamageSource source = boss.damageSources().indirectMagic(boss, boss);
        float damage = switch (kind) {
            case KIND_ICE_CHUNK -> ICE_DAMAGE;
            case KIND_VOID_RIFT -> VOID_RIFT_DAMAGE;
            case KIND_NIGHTS_EDGE -> NIGHTS_EDGE_DAMAGE;
            default -> 0.0F;
        };
        if (damage <= 0.0F) {
            return;
        }
        if (kind == KIND_NIGHTS_EDGE) {
            source = boss.damageSources().fellOutOfWorld();
        }
        if (kind == KIND_ICE_CHUNK || kind == KIND_NIGHTS_EDGE) {
            damage = ChaosMonarchTweaks.modifiedDamage(damage);
        }
        if (hurtManagedGoetyTarget(boss, target, source, damage)) {
            applyEffects(target, effectsFor(effectPhase));
            if (kind == KIND_ICE_CHUNK) {
                applyGoetyStunned(target);
            }
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

    private static void startPhase(ChaosMonarch boss, int effectPhase, LivingEntity target) {
        switch (effectPhase) {
            case 1 -> startFireTrapPattern(boss, target);
            case 2 -> startIceChunkPattern(boss, target);
            case 3 -> startSlamPattern(boss, target);
            case 4 -> spawnVoidRift(boss);
            case 5 -> startFangPattern(boss, target);
            default -> {
            }
        }
    }

    private static void startFireTrapPattern(ChaosMonarch boss, LivingEntity target) {
        Vec3 base = ChaosMonarchTweaks.groundCenterAt(boss.level(), target.getX(),
                Math.max(target.getY(), boss.getY()) + 8.0D,
                target.getZ(), Mth.floor(target.getX()), Mth.floor(target.getZ()));
        Set<BlockPos> spawned = new LinkedHashSet<>();
        spawnFireTrapOnce(boss, base, spawned);
        for (int distance : FIRE_CROSS_DISTANCES) {
            spawnFireTrapOnce(boss, base.add(distance, 0.0D, 0.0D), spawned);
            spawnFireTrapOnce(boss, base.add(-distance, 0.0D, 0.0D), spawned);
            spawnFireTrapOnce(boss, base.add(0.0D, 0.0D, distance), spawned);
            spawnFireTrapOnce(boss, base.add(0.0D, 0.0D, -distance), spawned);
        }
        PENDING_ACTIONS.add(PendingAction.fireRing(boss, base, FIRE_RING_DELAY_TICKS));
    }

    private static void spawnFireRing(ChaosMonarch boss, Vec3 base) {
        Set<BlockPos> spawned = new LinkedHashSet<>();
        for (int i = 0; i < FIRE_RING_COUNT; ++i) {
            double angle = (Math.PI * 2.0D) * i / FIRE_RING_COUNT;
            spawnFireTrapOnce(boss, base.add(
                    Math.cos(angle) * FIRE_RING_RADIUS,
                    0.0D,
                    Math.sin(angle) * FIRE_RING_RADIUS), spawned);
        }
    }

    private static void spawnFireTrapOnce(ChaosMonarch boss, Vec3 desired, Set<BlockPos> spawned) {
        Vec3 center = ChaosMonarchTweaks.groundCenterAt(boss.level(), desired.x, desired.y + 8.0D, desired.z,
                Mth.floor(desired.x), Mth.floor(desired.z));
        BlockPos key = BlockPos.containing(center.x, center.y, center.z);
        if (!spawned.add(key)) {
            return;
        }
        TelegraphVfx.redGroundWarningCircle(boss, center.add(0.0D, 0.06D, 0.0D),
                FIRE_WARNING_TICKS, FIRE_TRAP_RADIUS);
        if (spawnFireTrapVisual(boss, center)) {
            PENDING_ACTIONS.add(PendingAction.fireDamage(boss, center, FIRE_DAMAGE_DELAY_TICKS));
        }
    }

    private static boolean spawnFireTrapVisual(ChaosMonarch boss, Vec3 center) {
        if (!(boss.level() instanceof ServerLevel level)) {
            return false;
        }
        Entity visual = createEntity(level, FIRE_BLAST_TRAP);
        if (visual == null) {
            return false;
        }
        markManaged(visual, boss, KIND_FIRE_TRAP, 1);
        visual.moveTo(center.x, center.y, center.z, boss.getYRot(), boss.getXRot());
        visual.setDeltaMovement(Vec3.ZERO);
        invokeIfPresent(visual, "setOwner", LivingEntity.class, boss);
        invokeIfPresent(visual, "setAreaOfEffect", float.class, 0.0F);
        invokeIfPresent(visual, "setExtraDamage", float.class, 0.0F);
        invokeIfPresent(visual, "setBurning", int.class, 0);
        invokeIfPresent(visual, "setImmediate", boolean.class, false);
        return level.addFreshEntity(visual);
    }

    private static void startIceChunkPattern(ChaosMonarch boss, LivingEntity target) {
        UUID targetUuid = target.getUUID();
        for (int i = 0; i < ICE_COUNT; ++i) {
            PENDING_ACTIONS.add(PendingAction.iceSpawn(boss, targetUuid, i, i * ICE_INTERVAL_TICKS));
        }
    }

    private static void spawnIceChunk(ChaosMonarch boss, LivingEntity target, int index) {
        if (!(boss.level() instanceof ServerLevel level)) {
            return;
        }
        Entity chunk = createEntity(level, ICE_CHUNK);
        if (chunk == null) {
            return;
        }
        double angle = Math.PI * 2.0D * index / ICE_COUNT;
        double x = target.getX() + Math.cos(angle) * ICE_RING_RADIUS;
        double z = target.getZ() + Math.sin(angle) * ICE_RING_RADIUS;
        Vec3 pos = ChaosMonarchTweaks.groundCenterAt(level, x,
                Math.max(target.getY(), boss.getY()) + 8.0D,
                z, Mth.floor(x), Mth.floor(z));
        chunk.moveTo(pos.x, pos.y, pos.z, boss.getYRot(), boss.getXRot());
        markManaged(chunk, boss, KIND_ICE_CHUNK, 2);
        invokeIfPresent(chunk, "setOwner", LivingEntity.class, boss);
        invokeIfPresent(chunk, "setTarget", LivingEntity.class, target);
        invokeIfPresent(chunk, "setExtraDamage", float.class, 0.0F);
        if (level.addFreshEntity(chunk)) {
            TelegraphVfx.groundWarningCircleTrackingGround(chunk, ICE_WARNING_TICKS, ICE_WARNING_RADIUS, true);
        }
    }

    private static void startSlamPattern(ChaosMonarch boss, LivingEntity target) {
        Vec3 base = ChaosMonarchTweaks.groundCenterAt(boss.level(), target.getX(),
                Math.max(target.getY(), boss.getY()) + 8.0D,
                target.getZ(), Mth.floor(target.getX()), Mth.floor(target.getZ()));
        for (int i = 0; i < SLAM_RING_DELAYS.length; ++i) {
            PENDING_ACTIONS.add(PendingAction.slamRing(boss, base, i, SLAM_RING_DELAYS[i]));
        }
    }

    private static void spawnSlamRing(ChaosMonarch boss, Vec3 base, int ringIndex) {
        if (ringIndex < 0 || ringIndex >= SLAM_RING_RADII.length) {
            return;
        }
        spawnSlamWarnings(boss, base, SLAM_RING_RADII[ringIndex],
                SLAM_RING_COUNTS[ringIndex], SLAM_WARNING_TICKS);
    }

    private static void spawnSlamWarnings(ChaosMonarch boss, Vec3 base, double ringRadius,
                                          int count, int warningTicks) {
        for (int i = 0; i < count; ++i) {
            double angle = count <= 1 ? 0.0D : Math.PI * 2.0D * i / count;
            Vec3 desired = base.add(Math.cos(angle) * ringRadius, 0.0D, Math.sin(angle) * ringRadius);
            Vec3 center = ChaosMonarchTweaks.groundCenterAt(boss.level(), desired.x, desired.y + 8.0D, desired.z,
                    Mth.floor(desired.x), Mth.floor(desired.z));
            TelegraphVfx.redGroundWarningCircle(boss, center.add(0.0D, 0.06D, 0.0D),
                    warningTicks, SLAM_AOE_RADIUS);
            PENDING_ACTIONS.add(PendingAction.slamDamage(boss, center, warningTicks + 1));
        }
    }

    private static void spawnVoidRift(ChaosMonarch boss) {
        spawnVoidRift(boss, VOID_RIFT_DURATION_TICKS);
    }

    private static void spawnVoidRift(ChaosMonarch boss, int durationTicks) {
        if (!(boss.level() instanceof ServerLevel level)) {
            return;
        }
        Entity rift = createEntity(level, VOID_RIFT);
        if (rift == null) {
            return;
        }
        Vec3 pos = boss.position().add(0.0D, 10.0D, 0.0D);
        rift.moveTo(pos.x, pos.y, pos.z, boss.getYRot(), boss.getXRot());
        markManaged(rift, boss, KIND_VOID_RIFT, 4);
        invokeIfPresent(rift, "setOwner", LivingEntity.class, boss);
        invokeIfPresent(rift, "setDuration", int.class, Math.max(1, durationTicks));
        invokeIfPresent(rift, "setExtraDamage", float.class, 0.0F);
        level.addFreshEntity(rift);
    }

    private static void startFangPattern(ChaosMonarch boss, LivingEntity target) {
        UUID targetUuid = target.getUUID();
        for (int i = 0; i < NIGHTS_EDGE_COUNT; ++i) {
            PENDING_ACTIONS.add(PendingAction.nightsEdgeSpawn(boss, targetUuid, i * NIGHTS_EDGE_INTERVAL_TICKS));
        }
    }

    private static void spawnNightsEdge(ChaosMonarch boss, LivingEntity target) {
        if (!(boss.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 center = ChaosMonarchTweaks.groundCenterAt(level, target.getX(),
                Math.max(target.getY(), boss.getY()) + 8.0D,
                target.getZ(), Mth.floor(target.getX()), Mth.floor(target.getZ()));
        TelegraphVfx.purpleGroundWarningCircle(boss, center.add(0.0D, 0.06D, 0.0D),
                NIGHTS_EDGE_WARNING_TICKS, NIGHTS_EDGE_WARNING_RADIUS);
        NightsEdge edge = EntityRegistry.NIGHTS_EDGE.get().create(level);
        if (edge == null) {
            return;
        }
        double yaw = Math.atan2(target.getZ() - boss.getZ(), target.getX() - boss.getX()) - Math.PI / 2.0D;
        edge.moveTo(center.x, center.y, center.z, (float) Math.toDegrees(yaw), 0.0F);
        edge.setOwner(boss);
        markManaged(edge, boss, KIND_NIGHTS_EDGE, 5);
        edge.setDamage(NIGHTS_EDGE_DAMAGE);
        edge.setWarmup(Math.max(1, NIGHTS_EDGE_WARNING_TICKS - NIGHTS_EDGE_DAMAGE_DELAY_TICKS));
        level.addFreshEntity(edge);
    }

    private static void runAction(ServerLevel level, PendingAction action) {
        Entity entity = level.getEntity(action.bossUuid);
        if (!(entity instanceof ChaosMonarch boss) || !boss.isAlive()) {
            return;
        }
        switch (action.kind) {
            case FIRE_RING -> spawnFireRing(boss, action.center);
            case FIRE_DAMAGE -> detonateFireTrap(level, boss, action.center);
            case SLAM_RING -> spawnSlamRing(boss, action.center, action.index);
            case ICE_SPAWN -> {
                LivingEntity target = resolveTarget(level, action.targetUuid);
                if (target != null && target.isAlive()) {
                    spawnIceChunk(boss, target, action.index);
                }
            }
            case SLAM_DAMAGE -> detonateSlam(level, boss, action.center);
            case FANG_SPAWN -> {
                LivingEntity target = resolveTarget(level, action.targetUuid);
                if (target != null && target.isAlive()) {
                    spawnNightsEdge(boss, target);
                }
            }
            case PHASE_START -> {
                LivingEntity target = resolveTarget(level, action.targetUuid);
                if (target != null && target.isAlive()) {
                    startPhase(boss, action.index, target);
                }
            }
        }
    }

    private static void detonateFireTrap(ServerLevel level, ChaosMonarch boss, Vec3 center) {
        DamageSource source = boss.damageSources().indirectMagic(boss, boss);
        List<ConfiguredMobEffect> effects = effectsFor(1);
        AABB searchBox = new AABB(center, center).inflate(FIRE_TRAP_RADIUS);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, searchBox)) {
            if (shouldSkipTarget(boss, target)
                    || !intersectsSphere(target.getBoundingBox(), center, FIRE_TRAP_RADIUS)) {
                continue;
            }
            if (ChaosMonarchTweaks.hurtWithoutGuardBreak(boss, target, source, FIRE_TRAP_DAMAGE)) {
                applyEffects(target, effects);
                ChaosMonarchTweaks.igniteIfNotBurning(target, 20);
            }
        }
    }

    private static void detonateSlam(ServerLevel level, ChaosMonarch boss, Vec3 center) {
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
        if (lightning != null) {
            lightning.moveTo(center.x, center.y, center.z);
            lightning.setVisualOnly(true);
            level.addFreshEntity(lightning);
        }
        level.playSound(null, center.x, center.y, center.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0F, 1.0F);
        level.addFreshEntity(new NightProwlerLightningAoeEntity(level, center,
                SLAM_VISUAL_DURATION_TICKS, (float) SLAM_AOE_RADIUS));

        DamageSource source = boss.damageSources().indirectMagic(boss, boss);
        List<ConfiguredMobEffect> effects = effectsFor(3);
        AABB searchBox = new AABB(center, center).inflate(SLAM_AOE_RADIUS);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, searchBox)) {
            if (shouldSkipTarget(boss, target)
                    || !intersectsSphere(target.getBoundingBox(), center, SLAM_AOE_RADIUS)) {
                continue;
            }
            if (ChaosMonarchTweaks.hurtWithoutGuardBreak(boss, target, source, SLAM_DAMAGE)) {
                applyEffects(target, effects);
            }
        }
    }

    private static List<ConfiguredMobEffect> effectsFor(int phase) {
        return ConfiguredMobEffect.parseList(ChaosMonarchConfig.getChaosMonarchLightningHitEffects(phase));
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

    private static void applyGoetyStunned(LivingEntity target) {
        MobEffect stunned = ForgeRegistries.MOB_EFFECTS.getValue(GOETY_STUNNED);
        if (stunned != null) {
            target.addEffect(new MobEffectInstance(stunned, 60, 0, true, true));
        }
    }

    private static boolean hurtManagedGoetyTarget(ChaosMonarch boss, LivingEntity target,
                                                  DamageSource source, float damage) {
        APPLYING_MANAGED_DAMAGE.set(true);
        try {
            return ChaosMonarchTweaks.hurtWithoutGuardBreak(boss, target, source, damage);
        } finally {
            APPLYING_MANAGED_DAMAGE.set(false);
        }
    }

    private static boolean shouldSkipTarget(ChaosMonarch boss, LivingEntity target) {
        return ChaosMonarchTweaks.shouldSkipTarget(boss, target);
    }

    @Nullable
    private static Entity managedEntity(@Nullable Entity entity) {
        return entity != null && entity.getTags().contains(MANAGED_GOETY_TAG) ? entity : null;
    }

    @Nullable
    private static ChaosMonarch resolveBoss(ServerLevel level, Entity managed) {
        CompoundTag data = managed.getPersistentData();
        if (!data.hasUUID(BOSS_UUID_KEY)) {
            return null;
        }
        Entity entity = level.getEntity(data.getUUID(BOSS_UUID_KEY));
        return entity instanceof ChaosMonarch boss ? boss : null;
    }

    @Nullable
    private static LivingEntity resolveTarget(ServerLevel level, @Nullable UUID uuid) {
        if (uuid == null) {
            return null;
        }
        Entity entity = level.getEntity(uuid);
        return entity instanceof LivingEntity living ? living : null;
    }

    @Nullable
    private static Entity createEntity(ServerLevel level, ResourceLocation id) {
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(id);
        return type == null ? null : type.create(level);
    }

    private static void markManaged(Entity entity, ChaosMonarch boss, int kind, int effectPhase) {
        entity.addTag(MANAGED_GOETY_TAG);
        CompoundTag data = entity.getPersistentData();
        data.putUUID(BOSS_UUID_KEY, boss.getUUID());
        data.putInt(KIND_KEY, kind);
        data.putInt(EFFECT_PHASE_KEY, effectPhase);
    }

    private static void invokeIfPresent(Entity entity, String methodName, Class<?> parameterType, Object value) {
        try {
            Method method = entity.getClass().getMethod(methodName, parameterType);
            method.invoke(entity, value);
        } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
            // Goety entities expose these methods on different helper bases; missing methods are harmless.
        }
    }

    private static void setPrivateInt(Entity entity, String fieldName, int value) {
        try {
            Field field = entity.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.setInt(entity, value);
        } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
            // If Goety changes the field name, the fang still spawns, only the timing falls back to Goety's default.
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
        FIRE_RING,
        FIRE_DAMAGE,
        SLAM_RING,
        ICE_SPAWN,
        SLAM_DAMAGE,
        FANG_SPAWN,
        PHASE_START
    }

    private static final class PendingAction {
        private final ActionKind kind;
        private final ResourceKey<Level> dimension;
        private final UUID bossUuid;
        @Nullable
        private final UUID targetUuid;
        private final Vec3 center;
        private final int index;
        private int delayTicks;

        private PendingAction(ActionKind kind, ResourceKey<Level> dimension, UUID bossUuid,
                              @Nullable UUID targetUuid, Vec3 center, int index, int delayTicks) {
            this.kind = kind;
            this.dimension = dimension;
            this.bossUuid = bossUuid;
            this.targetUuid = targetUuid;
            this.center = center;
            this.index = index;
            this.delayTicks = delayTicks;
        }

        private static PendingAction fireRing(ChaosMonarch boss, Vec3 center, int delayTicks) {
            return new PendingAction(ActionKind.FIRE_RING, boss.level().dimension(), boss.getUUID(),
                    null, center, 0, delayTicks);
        }

        private static PendingAction fireDamage(ChaosMonarch boss, Vec3 center, int delayTicks) {
            return new PendingAction(ActionKind.FIRE_DAMAGE, boss.level().dimension(), boss.getUUID(),
                    null, center, 0, delayTicks);
        }

        private static PendingAction iceSpawn(ChaosMonarch boss, UUID targetUuid, int index, int delayTicks) {
            return new PendingAction(ActionKind.ICE_SPAWN, boss.level().dimension(), boss.getUUID(),
                    targetUuid, Vec3.ZERO, index, delayTicks);
        }

        private static PendingAction slamDamage(ChaosMonarch boss, Vec3 center, int delayTicks) {
            return new PendingAction(ActionKind.SLAM_DAMAGE, boss.level().dimension(), boss.getUUID(),
                    null, center, 0, delayTicks);
        }

        private static PendingAction slamRing(ChaosMonarch boss, Vec3 center, int index, int delayTicks) {
            return new PendingAction(ActionKind.SLAM_RING, boss.level().dimension(), boss.getUUID(),
                    null, center, index, delayTicks);
        }

        private static PendingAction nightsEdgeSpawn(ChaosMonarch boss, UUID targetUuid, int delayTicks) {
            return new PendingAction(ActionKind.FANG_SPAWN, boss.level().dimension(), boss.getUUID(),
                    targetUuid, Vec3.ZERO, 0, delayTicks);
        }

        private static PendingAction phaseStart(ChaosMonarch boss, UUID targetUuid, int effectPhase, int delayTicks) {
            return new PendingAction(ActionKind.PHASE_START, boss.level().dimension(), boss.getUUID(),
                    targetUuid, Vec3.ZERO, effectPhase, delayTicks);
        }
    }
}
