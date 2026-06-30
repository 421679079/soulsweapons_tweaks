package com.starfantasy.soulsfirecontrol.combat.chaosmonarch;

import com.starfantasy.soulsfirecontrol.combat.buff.BossPhaseBuffManager;
import com.starfantasy.soulsfirecontrol.combat.effect.ConfiguredMobEffect;
import com.starfantasy.soulsfirecontrol.combat.guard.ChaosMonarchGuardBreakTracker;
import com.starfantasy.soulsfirecontrol.config.ChaosMonarchConfig;
import com.starfantasy.soulsfirecontrol.entity.NightProwlerLightningAoeEntity;
import com.starfantasy.soulsfirecontrol.mixin.BossEntityAccessor;
import com.starfantasy.soulsfirecontrol.vfx.telegraph.TelegraphVfx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.soulsweaponry.config.ConfigConstructor;
import net.soulsweaponry.entity.mobs.ChaosMonarch;
import net.soulsweaponry.entity.projectile.noclip.BlackflameExplosionEntity;
import net.soulsweaponry.entity.projectile.noclip.DamagingWarmupEntity;
import net.soulsweaponry.entity.projectile.noclip.DamagingWarmupEntityEvents;
import net.soulsweaponry.entity.projectile.noclip.FlamePillar;
import net.soulsweaponry.entity.projectile.noclip.HolyMoonlightPillar;
import net.soulsweaponry.particles.ParticleHandler;
import net.soulsweaponry.registry.EntityRegistry;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ChaosMonarchPhaseManager {
    public static final String PHASE_VISUAL_ENTITY_TAG = "starfantasy_chaos_monarch_phase_visual";
    private static final String PHASE_KEY = "starfantasy_chaos_monarch_phase";
    private static final String TRANSITION_TARGET_KEY = "starfantasy_chaos_monarch_transition_target";
    private static final String TRANSITION_TICKS_KEY = "starfantasy_chaos_monarch_transition_ticks";
    private static final String GOAL_RESET_KEY = "starfantasy_chaos_monarch_goal_reset";
    private static final String FORCE_LIGHTNING_KEY = "starfantasy_chaos_monarch_force_lightning";
    private static final int FIRST_PHASE = 1;
    private static final int LAST_PHASE = 6;
    private static final int IDLE_ATTACK_ID = ChaosMonarch.Attack.IDLE.ordinal();
    private static final int TELEPORT_ATTACK_ID = ChaosMonarch.Attack.TELEPORT.ordinal();
    private static final int TRANSITION_EXPLOSION_TICK = 30;
    private static final int TRANSITION_TOTAL_TICKS = TRANSITION_EXPLOSION_TICK + 24;
    private static final double TRANSITION_DAMAGE_RADIUS = 9.0D;
    private static final float TRANSITION_DAMAGE = 50.0F;
    private static final double TRANSITION_RING_WARNING_HEIGHT = 2.8D;
    private static final int[] STEP_RING_DELAYS = {0, 5, 10, 15};
    private static final double[] STEP_RING_RADII = {2.0D, 4.0D, 6.0D, 8.0D};
    private static final int[] STEP_RING_COUNTS = {6, 8, 10, 12};
    private static final int STEP_VISUAL_WARMUP = -6;
    private static final float STEP_VISUAL_RADIUS = 2.0F;
    private static final int STEP_VISUAL_DURATION_TICKS = 14;
    private static final int DEBUFF_REFRESH_INTERVAL_TICKS = 20;
    private static final int DEBUFF_REFRESH_THRESHOLD_TICKS = 60;
    private static final int PHASE_SIX_HEAL_INTERVAL_TICKS = 20;

    private ChaosMonarchPhaseManager() {
    }

    public static void tick(ChaosMonarch boss) {
        if (boss.level().isClientSide() || !boss.isAlive()) {
            return;
        }
        if (isTransitioning(boss)) {
            tickTransition(boss);
        } else {
            syncPhaseFromHealth(boss);
        }
        int phase = getCurrentPhase(boss);
        applyDisplay(boss, phase);
        tickPhaseSixHealing(boss, phase);
        BossPhaseBuffManager.tickChaosMonarch(boss, phase);
        if (boss.tickCount % DEBUFF_REFRESH_INTERVAL_TICKS == 0) {
            applyNearbyDebuffs(boss, phase);
        }
    }

    public static float clampFinalDamageForPhaseLock(ChaosMonarch boss, DamageSource source, float amount) {
        if (boss.level().isClientSide() || !boss.isAlive()) {
            return amount;
        }
        if (bypassesPhaseLock(source)) {
            return amount;
        }
        if (isTransitioning(boss)) {
            return 0.0F;
        }
        if (amount <= 0.0F) {
            return amount;
        }
        int phase = getCurrentPhase(boss);
        if (phase >= LAST_PHASE) {
            return amount;
        }
        float thresholdHealth = boss.getMaxHealth() * thresholdForPhase(phase + 1);
        float currentHealth = boss.getHealth();
        float projectedHealth = currentHealth - amount;
        if (projectedHealth > thresholdHealth) {
            return amount;
        }
        startTransition(boss, phase + 1);
        return Math.max(0.0F, currentHealth - Math.max(1.0F, thresholdHealth));
    }

    public static boolean isTransitioning(ChaosMonarch boss) {
        return transitionTarget(boss) > 0;
    }

    public static boolean bypassesPhaseLock(DamageSource source) {
        return source != null && source.is(DamageTypes.GENERIC_KILL);
    }

    public static boolean consumeGoalReset(ChaosMonarch boss) {
        CompoundTag data = boss.getPersistentData();
        if (!data.getBoolean(GOAL_RESET_KEY)) {
            return false;
        }
        data.remove(GOAL_RESET_KEY);
        return true;
    }

    public static boolean consumeForcedLightning(ChaosMonarch boss) {
        CompoundTag data = boss.getPersistentData();
        if (!data.getBoolean(FORCE_LIGHTNING_KEY)) {
            return false;
        }
        data.remove(FORCE_LIGHTNING_KEY);
        return true;
    }

    public static void requestGoalReset(ChaosMonarch boss) {
        boss.getPersistentData().putBoolean(GOAL_RESET_KEY, true);
    }

    public static void requestForcedLightning(ChaosMonarch boss) {
        CompoundTag data = boss.getPersistentData();
        data.putBoolean(GOAL_RESET_KEY, true);
        data.putBoolean(FORCE_LIGHTNING_KEY, true);
    }

    public static int getCurrentPhase(ChaosMonarch boss) {
        CompoundTag data = boss.getPersistentData();
        int phase = data.getInt(PHASE_KEY);
        if (phase < FIRST_PHASE || phase > LAST_PHASE) {
            phase = phaseForHealth(boss);
            data.putInt(PHASE_KEY, phase);
        }
        return phase;
    }

    private static void startTransition(ChaosMonarch boss, int targetPhase) {
        CompoundTag data = boss.getPersistentData();
        ChaosMonarchGuardBreakTracker.clearStunAndReset(boss);
        data.putInt(TRANSITION_TARGET_KEY, clampPhase(targetPhase));
        data.putInt(TRANSITION_TICKS_KEY, 0);
        boss.setAttack(TELEPORT_ATTACK_ID);
        boss.setAggressive(true);
        boss.getNavigation().stop();
        boss.setDeltaMovement(0.0D, boss.getDeltaMovement().y, 0.0D);
        spawnTransitionWarnings(boss, targetPhase);
    }

    private static void tickTransition(ChaosMonarch boss) {
        CompoundTag data = boss.getPersistentData();
        int targetPhase = transitionTarget(boss);
        int ticks = data.getInt(TRANSITION_TICKS_KEY) + 1;
        data.putInt(TRANSITION_TICKS_KEY, ticks);
        boss.setAttack(TELEPORT_ATTACK_ID);
        boss.setAggressive(true);
        boss.getNavigation().stop();
        boss.setDeltaMovement(0.0D, boss.getDeltaMovement().y, 0.0D);

        if (ticks == TRANSITION_EXPLOSION_TICK) {
            explodeTransition(boss, targetPhase);
            setCurrentPhase(boss, targetPhase);
        }
        spawnStepVisualsIfDue(boss, targetPhase, ticks);
        if (ticks >= TRANSITION_TOTAL_TICKS) {
            finishTransition(boss);
        }
    }

    private static void finishTransition(ChaosMonarch boss) {
        CompoundTag data = boss.getPersistentData();
        data.remove(TRANSITION_TARGET_KEY);
        data.remove(TRANSITION_TICKS_KEY);
        data.putBoolean(GOAL_RESET_KEY, true);
        data.putBoolean(FORCE_LIGHTNING_KEY, true);
        boss.setAttack(IDLE_ATTACK_ID);
        boss.setAggressive(false);
    }

    private static void syncPhaseFromHealth(ChaosMonarch boss) {
        int current = getCurrentPhase(boss);
        int computed = phaseForHealth(boss);
        if (computed < current) {
            setCurrentPhase(boss, computed);
            return;
        }
        if (computed > current && current < LAST_PHASE) {
            float thresholdHealth = boss.getMaxHealth() * thresholdForPhase(current + 1);
            boss.setHealth(Math.max(1.0F, thresholdHealth));
            startTransition(boss, current + 1);
        }
    }

    private static int phaseForHealth(ChaosMonarch boss) {
        float maxHealth = Math.max(1.0F, boss.getMaxHealth());
        float rate = boss.getHealth() / maxHealth;
        if (rate > 0.85F) {
            return 1;
        }
        if (rate > 0.70F) {
            return 2;
        }
        if (rate > 0.55F) {
            return 3;
        }
        if (rate > 0.40F) {
            return 4;
        }
        if (rate > 0.25F) {
            return 5;
        }
        return 6;
    }

    private static float thresholdForPhase(int phase) {
        return switch (clampPhase(phase)) {
            case 2 -> 0.85F;
            case 3 -> 0.70F;
            case 4 -> 0.55F;
            case 5 -> 0.40F;
            case 6 -> 0.25F;
            default -> 1.0F;
        };
    }

    private static void tickPhaseSixHealing(ChaosMonarch boss, int phase) {
        if (phase < LAST_PHASE || boss.tickCount % PHASE_SIX_HEAL_INTERVAL_TICKS != 0) {
            return;
        }
        float heal = ChaosMonarchConfig.getChaosMonarchPhaseSixHealPerSecond();
        if (heal <= 0.0F) {
            return;
        }
        float cap = boss.getMaxHealth() * thresholdForPhase(LAST_PHASE);
        if (boss.getHealth() >= cap) {
            return;
        }
        boss.setHealth(Math.min(cap, boss.getHealth() + heal));
    }

    private static int transitionTarget(ChaosMonarch boss) {
        int target = boss.getPersistentData().getInt(TRANSITION_TARGET_KEY);
        return target >= FIRST_PHASE && target <= LAST_PHASE ? target : 0;
    }

    private static void setCurrentPhase(ChaosMonarch boss, int phase) {
        boss.getPersistentData().putInt(PHASE_KEY, clampPhase(phase));
    }

    private static int clampPhase(int phase) {
        return Math.max(FIRST_PHASE, Math.min(LAST_PHASE, phase));
    }

    private static void explodeTransition(ChaosMonarch boss, int targetPhase) {
        Level level = boss.level();
        level.playSound(null, boss.blockPosition(), SoundEvents.GENERIC_EXPLODE,
                SoundSource.HOSTILE, 5.0F, 1.0F);
        Vec3 center = boss.position();
        AABB area = boss.getBoundingBox().inflate(TRANSITION_DAMAGE_RADIUS);
        DamageSource source = targetPhase >= LAST_PHASE
                ? level.damageSources().fellOutOfWorld()
                : level.damageSources().indirectMagic(boss, boss);
        for (Entity entity : level.getEntities(boss, area)) {
            if (!(entity instanceof LivingEntity target) || target == boss) {
                continue;
            }
            if (!intersectsSphere(target.getBoundingBox(), center, TRANSITION_DAMAGE_RADIUS)) {
                continue;
            }
            double x = target.getX() - boss.getX();
            double z = target.getZ() - boss.getZ();
            target.knockback(10.0D, -x, -z);
            target.hurt(source, TRANSITION_DAMAGE * ConfigConstructor.chaos_monarch_damage_modifier);
        }
        if (!level.isClientSide()) {
            ParticleHandler.particleSphereList(level, 1000, boss.getX(), boss.getY(), boss.getZ(), 1.0F,
                    new ParticleOptions[]{ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.LARGE_SMOKE});
        }
    }

    private static void spawnTransitionWarnings(ChaosMonarch boss, int targetPhase) {
        int warningTicks = Math.max(1, TRANSITION_EXPLOSION_TICK);
        if (targetPhase >= LAST_PHASE) {
            TelegraphVfx.purpleAttackWarningRing(boss, warningTicks,
                    TRANSITION_DAMAGE_RADIUS, TRANSITION_RING_WARNING_HEIGHT);
        } else {
            TelegraphVfx.redAttackWarningRing(boss, warningTicks,
                    TRANSITION_DAMAGE_RADIUS, TRANSITION_RING_WARNING_HEIGHT);
        }
        Vec3 ground = groundCenterAt(boss.level(), boss.getX(), boss.getY() + 8.0D, boss.getZ(),
                Mth.floor(boss.getX()), Mth.floor(boss.getZ()));
        if (targetPhase >= LAST_PHASE) {
            TelegraphVfx.purpleGroundWarningCircle(boss, ground.add(0.0D, 0.06D, 0.0D),
                    warningTicks, TRANSITION_DAMAGE_RADIUS);
        } else {
            TelegraphVfx.redGroundWarningCircle(boss, ground.add(0.0D, 0.06D, 0.0D),
                    warningTicks, TRANSITION_DAMAGE_RADIUS);
        }
    }

    private static void spawnStepVisualsIfDue(ChaosMonarch boss, int targetPhase, int transitionTicks) {
        for (int i = 0; i < STEP_RING_DELAYS.length; ++i) {
            if (transitionTicks == TRANSITION_EXPLOSION_TICK + STEP_RING_DELAYS[i]) {
                spawnStepVisualRing(boss, targetPhase, i);
                return;
            }
        }
    }

    private static void spawnStepVisualRing(ChaosMonarch boss, int targetPhase, int ringIndex) {
        if (!(boss.level() instanceof ServerLevel level)) {
            return;
        }
        double radius = STEP_RING_RADII[ringIndex];
        int count = STEP_RING_COUNTS[ringIndex];
        for (int i = 0; i < count; ++i) {
            double angle = Math.PI * 2.0D * i / count;
            double x = boss.getX() + Math.cos(angle) * radius;
            double z = boss.getZ() + Math.sin(angle) * radius;
            Vec3 center = groundCenterAt(level, x, boss.getY() + 8.0D, z, Mth.floor(x), Mth.floor(z));
            spawnStepVisual(level, boss, center, visualFor(targetPhase, ringIndex));
        }
    }

    private static StepVisual visualFor(int targetPhase, int ringIndex) {
        return switch (clampPhase(targetPhase)) {
            case 2 -> StepVisual.FLAME_PILLAR;
            case 3 -> StepVisual.HOLY_MOONLIGHT_PILLAR;
            case 4 -> StepVisual.SLAM;
            case 5 -> StepVisual.BLACKFLAME_EXPLOSION;
            case 6 -> switch (ringIndex) {
                case 0 -> StepVisual.FLAME_PILLAR;
                case 1 -> StepVisual.HOLY_MOONLIGHT_PILLAR;
                case 2 -> StepVisual.SLAM;
                default -> StepVisual.BLACKFLAME_EXPLOSION;
            };
            default -> StepVisual.FLAME_PILLAR;
        };
    }

    private static void spawnStepVisual(ServerLevel level, ChaosMonarch boss, Vec3 center, StepVisual visual) {
        switch (visual) {
            case FLAME_PILLAR -> {
                FlamePillar pillar = new FlamePillar(level, boss, STEP_VISUAL_RADIUS,
                        STEP_VISUAL_WARMUP, -1);
                prepareVisualWarmupEntity(pillar, center);
                level.addFreshEntity(pillar);
            }
            case HOLY_MOONLIGHT_PILLAR -> {
                HolyMoonlightPillar pillar = new HolyMoonlightPillar(EntityRegistry.HOLY_MOONLIGHT_PILLAR.get(), level);
                pillar.setOwner(boss);
                pillar.setRadius(STEP_VISUAL_RADIUS);
                pillar.setWarmup(STEP_VISUAL_WARMUP);
                pillar.setEventId(-1);
                pillar.setKnockUp(0.0F);
                prepareVisualWarmupEntity(pillar, center);
                level.addFreshEntity(pillar);
            }
            case BLACKFLAME_EXPLOSION -> {
                BlackflameExplosionEntity explosion = new BlackflameExplosionEntity(level);
                explosion.setOwner(boss);
                explosion.setRadius(STEP_VISUAL_RADIUS);
                explosion.setWarmup(STEP_VISUAL_WARMUP);
                explosion.setEventId(-1);
                prepareVisualWarmupEntity(explosion, center);
                level.addFreshEntity(explosion);
            }
            case SLAM -> spawnSlamStepVisual(level, center);
        }
    }

    private static void spawnSlamStepVisual(ServerLevel level, Vec3 center) {
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
        if (lightning != null) {
            lightning.moveTo(center.x, center.y, center.z);
            lightning.setVisualOnly(true);
            level.addFreshEntity(lightning);
        }
        level.addFreshEntity(new NightProwlerLightningAoeEntity(level, center,
                STEP_VISUAL_DURATION_TICKS, STEP_VISUAL_RADIUS));
    }

    private static void prepareVisualWarmupEntity(DamagingWarmupEntity entity, Vec3 center) {
        entity.addTag(PHASE_VISUAL_ENTITY_TAG);
        entity.setBaseDamage(0.0D);
        entity.setParticleAmountMod(1.0F);
        entity.setSilent(true);
        entity.moveTo(center.x, center.y, center.z);
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

    private static void applyDisplay(ChaosMonarch boss, int phase) {
        Component name = Component.literal(displayName(phase));
        if (!name.equals(boss.getCustomName())) {
            boss.setCustomName(name);
            boss.setCustomNameVisible(true);
        }
        try {
            ServerBossEvent bossBar = ((BossEntityAccessor) boss).starfantasy$getBossBar();
            bossBar.setName(name);
            bossBar.setColor(barColor(phase));
        } catch (ClassCastException ignored) {
        }
    }

    private static String displayName(int phase) {
        return switch (clampPhase(phase)) {
            case 2 -> "\u00a7b\u6df7\u6c8c\u541b\u4e3b \u300c\u5b8c\u7f8e\u51bb\u7ed3\u300d";
            case 3 -> "\u00a7e\u6df7\u6c8c\u541b\u4e3b \u300c\u72c2\u96f7\u5954\u817e\u300d";
            case 4 -> "\u00a75\u6df7\u6c8c\u541b\u4e3b \u300c\u626d\u8f6c\u4e07\u8c61\u300d";
            case 5 -> "\u00a78\u6df7\u6c8c\u541b\u4e3b \u300c\u751f\u8005\u5fc5\u706d\u300d";
            case 6 -> "\u00a74\u6df7\u6c8c\u541b\u4e3b \u300c\u4e07\u7269\u7ec8\u7ed3\u300d";
            default -> "\u00a76\u6df7\u6c8c\u541b\u4e3b \u300c\u7687\u5bb6\u5723\u7130\u300d";
        };
    }

    private static BossEvent.BossBarColor barColor(int phase) {
        return switch (clampPhase(phase)) {
            case 2 -> BossEvent.BossBarColor.BLUE;
            case 3 -> BossEvent.BossBarColor.YELLOW;
            case 4 -> BossEvent.BossBarColor.PURPLE;
            case 5 -> BossEvent.BossBarColor.WHITE;
            case 6 -> BossEvent.BossBarColor.RED;
            default -> BossEvent.BossBarColor.YELLOW;
        };
    }

    private static void applyNearbyDebuffs(ChaosMonarch boss, int phase) {
        List<ConfiguredMobEffect> effects = ConfiguredMobEffect.parseList(
                ChaosMonarchConfig.getChaosMonarchPhaseDebuffs(phase));
        if (effects.isEmpty()) {
            return;
        }
        for (ServerPlayer player : collectPlayers(boss, ChaosMonarchConfig.getChaosMonarchNearbyPlayerDebuffRadius())) {
            for (ConfiguredMobEffect effect : effects) {
                MobEffectInstance current = player.getEffect(effect.effect());
                if (current == null || current.getAmplifier() < effect.amplifier()
                        || current.getDuration() < DEBUFF_REFRESH_THRESHOLD_TICKS) {
                    player.addEffect(effect.createInstance());
                }
            }
        }
    }

    private static Set<ServerPlayer> collectPlayers(ChaosMonarch boss, double radius) {
        Set<ServerPlayer> players = new LinkedHashSet<>();
        if (!(boss.level() instanceof ServerLevel level)) {
            return players;
        }
        try {
            players.addAll(((BossEntityAccessor) boss).starfantasy$getBossBar().getPlayers());
        } catch (ClassCastException ignored) {
        }
        LivingEntity target = boss.getTarget();
        if (target instanceof ServerPlayer player && !isIgnoredPlayer(player)) {
            players.add(player);
        }
        AABB box = boss.getBoundingBox().inflate(radius);
        players.addAll(level.getEntitiesOfClass(ServerPlayer.class, box));
        players.removeIf(ChaosMonarchPhaseManager::isIgnoredPlayer);
        return players;
    }

    private static boolean isIgnoredPlayer(Player player) {
        return player.isSpectator() || player.isCreative() || !player.isAlive();
    }

    private enum StepVisual {
        FLAME_PILLAR,
        HOLY_MOONLIGHT_PILLAR,
        SLAM,
        BLACKFLAME_EXPLOSION
    }
}
