package com.starfantasy.soulsfirecontrol.combat.twin;

import com.starfantasy.soulsfirecontrol.combat.effect.ConfiguredMobEffect;
import com.starfantasy.soulsfirecontrol.config.ChaosMonarchConfig;
import com.starfantasy.soulsfirecontrol.entity.TwinMeteorEntity;
import com.starfantasy.soulsfirecontrol.entity.TwinMeteorEntityRegistry;
import com.starfantasy.soulsfirecontrol.mixin.BossEntityAccessor;
import com.starfantasy.soulsfirecontrol.vfx.telegraph.TelegraphVfx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.soulsweaponry.entity.mobs.DayStalker;
import net.soulsweaponry.entity.mobs.NightProwler;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public final class TwinBossCombatEffects {
    private static final String DAY_METEOR_COOLDOWN_KEY = "starfantasy_day_stalker_meteor_cooldown";
    private static final String NIGHT_METEOR_COOLDOWN_KEY = "starfantasy_night_prowler_meteor_cooldown";
    private static final int DEBUFF_REFRESH_INTERVAL_TICKS = 20;
    private static final int DEBUFF_REFRESH_THRESHOLD_TICKS = 60;
    private static final int METEOR_PHASE_TWO_INTERVAL_TICKS = 15;
    private static final int METEOR_LOW_HEALTH_INTERVAL_TICKS = 5;
    private static final double METEOR_BOSS_OFFSET_MIN = 12.0D;
    private static final double METEOR_BOSS_OFFSET_MAX = 18.0D;
    private static final double METEOR_EXPLOSION_RADIUS = 2.0D;

    private TwinBossCombatEffects() {
    }

    public static void tickDayStalker(DayStalker boss) {
        if (boss.level().isClientSide() || !boss.isAlive()) {
            return;
        }
        tickShared(boss,
                boss.isPhaseTwo(),
                DAY_METEOR_COOLDOWN_KEY,
                TwinMeteorEntityRegistry.DAY_STALKER_METEOR::get,
                ChaosMonarchConfig::getDayStalkerNearbyPlayerDebuffs);
    }

    public static void tickNightProwler(NightProwler boss) {
        if (boss.level().isClientSide() || !boss.isAlive()) {
            return;
        }
        tickShared(boss,
                boss.isPhaseTwo(),
                NIGHT_METEOR_COOLDOWN_KEY,
                TwinMeteorEntityRegistry.NIGHT_PROWLER_METEOR::get,
                ChaosMonarchConfig::getNightProwlerNearbyPlayerDebuffs);
    }

    private static void tickShared(LivingEntity boss, boolean phaseTwo, String cooldownKey,
                                   Supplier<EntityType<TwinMeteorEntity>> meteorType,
                                   Supplier<List<String>> nearbyDebuffs) {
        if (boss.tickCount % DEBUFF_REFRESH_INTERVAL_TICKS == 0) {
            applyNearbyDebuffs(boss, nearbyDebuffs.get());
        }
        if (phaseTwo) {
            tickMeteors(boss, cooldownKey, meteorType.get());
        } else {
            boss.getPersistentData().remove(cooldownKey);
        }
    }

    private static void applyNearbyDebuffs(LivingEntity boss, List<String> configuredDebuffs) {
        List<ConfiguredMobEffect> effects = ConfiguredMobEffect.parseList(configuredDebuffs);
        if (effects.isEmpty()) {
            return;
        }
        for (ServerPlayer player : collectPlayers(boss, ChaosMonarchConfig.getTwinBossNearbyPlayerDebuffRadius())) {
            for (ConfiguredMobEffect effect : effects) {
                MobEffectInstance current = player.getEffect(effect.effect());
                if (current == null || current.getAmplifier() < effect.amplifier()
                        || current.getDuration() < DEBUFF_REFRESH_THRESHOLD_TICKS) {
                    player.addEffect(effect.createInstance());
                }
            }
        }
    }

    private static void tickMeteors(LivingEntity boss, String cooldownKey,
                                    EntityType<TwinMeteorEntity> meteorType) {
        CompoundTag data = boss.getPersistentData();
        int cooldown = data.getInt(cooldownKey);
        if (cooldown > 0) {
            data.putInt(cooldownKey, cooldown - 1);
            return;
        }

        if (!hasMeteorAudience(boss)) {
            data.putInt(cooldownKey, DEBUFF_REFRESH_INTERVAL_TICKS);
            return;
        }
        spawnMeteor(boss, meteorType);
        data.putInt(cooldownKey, nextMeteorCooldown(boss));
    }

    private static boolean hasMeteorAudience(LivingEntity boss) {
        LivingEntity target = boss instanceof Mob mob ? mob.getTarget() : null;
        if (target instanceof Player player && !isIgnoredPlayer(player)) {
            return true;
        }
        return !collectPlayers(boss, ChaosMonarchConfig.getTwinBossMeteorTargetRadius()).isEmpty();
    }

    private static int nextMeteorCooldown(LivingEntity boss) {
        return boss.getHealth() <= boss.getMaxHealth() * 0.5F
                ? METEOR_LOW_HEALTH_INTERVAL_TICKS
                : METEOR_PHASE_TWO_INTERVAL_TICKS;
    }

    private static void spawnMeteor(LivingEntity boss, EntityType<TwinMeteorEntity> meteorType) {
        if (!(boss.level() instanceof ServerLevel level)) {
            return;
        }
        int warningTicks = Math.max(1, ChaosMonarchConfig.getTwinBossMeteorWarningTicks());
        Vec3 impact = randomImpactAroundBoss(boss);
        Vec3 start = impact.add(0.0D, ChaosMonarchConfig.getTwinBossMeteorSpawnYOffset(), 0.0D);
        TwinMeteorEntity meteor = meteorType.create(level);
        if (meteor == null) {
            return;
        }
        meteor.moveTo(start.x, start.y, start.z, boss.getYRot(), boss.getXRot());
        meteor.configure(boss, impact, warningTicks);
        level.addFreshEntity(meteor);
        TelegraphVfx.redGroundWarningCircle(boss, impact.add(0.0D, 0.06D, 0.0D),
                warningTicks, METEOR_EXPLOSION_RADIUS);
    }

    private static Vec3 randomImpactAroundBoss(LivingEntity boss) {
        RandomSource random = boss.getRandom();
        double min = Math.min(METEOR_BOSS_OFFSET_MIN, METEOR_BOSS_OFFSET_MAX);
        double max = Math.max(METEOR_BOSS_OFFSET_MIN, METEOR_BOSS_OFFSET_MAX);
        double distance = min + random.nextDouble() * Math.max(0.0D, max - min);
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double x = boss.getX() + Math.cos(angle) * distance;
        double z = boss.getZ() + Math.sin(angle) * distance;
        return groundCenterAt(boss.level(), x,
                boss.getY() + ChaosMonarchConfig.getTwinBossMeteorSpawnYOffset(),
                z, Mth.floor(x), Mth.floor(z));
    }

    private static Set<ServerPlayer> collectPlayers(LivingEntity boss, double radius) {
        Set<ServerPlayer> players = new LinkedHashSet<>();
        if (!(boss.level() instanceof ServerLevel level)) {
            return players;
        }
        try {
            ServerBossEvent bossBar = ((BossEntityAccessor) boss).starfantasy$getBossBar();
            players.addAll(bossBar.getPlayers());
        } catch (ClassCastException ignored) {
            // Some non-standard boss wrappers may not expose the Souls boss bar.
        }

        AABB searchBox = boss.getBoundingBox().inflate(radius);
        players.addAll(level.getEntitiesOfClass(ServerPlayer.class, searchBox));
        players.removeIf(TwinBossCombatEffects::isIgnoredPlayer);
        return players;
    }

    private static boolean isIgnoredPlayer(Player player) {
        return player.isSpectator() || player.isCreative() || !player.isAlive();
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
}
