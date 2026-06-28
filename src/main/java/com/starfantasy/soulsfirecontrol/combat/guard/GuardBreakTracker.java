package com.starfantasy.soulsfirecontrol.combat.guard;

import com.starfantasy.soulsfirecontrol.combat.draugr.DraugrActionLockTracker;
import com.starfantasy.soulsfirecontrol.combat.draugr.DraugrAnimationSync;
import com.starfantasy.soulsfirecontrol.config.ChaosMonarchConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.soulsweaponry.entity.mobs.DraugrBoss;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class GuardBreakTracker {
    private static final int STUN_TICKS = 100;
    private static final Map<UUID, GuardBreakState> STATES = new HashMap<>();

    private GuardBreakTracker() {
    }

    public static void recordPerfectGuard(DraugrBoss boss) {
        if (boss.level().isClientSide() || boss.isDeadOrDying()) {
            return;
        }
        GuardBreakState state = STATES.computeIfAbsent(boss.getUUID(), uuid -> new GuardBreakState());
        if (state.stunTicks > 0) {
            return;
        }
        ++state.guardCount;
        if (state.guardCount >= ChaosMonarchConfig.getDraugrBossGuardBreakRequiredGuards()) {
            GuardBreakHudSync.syncStunTriggered(boss);
            startStun(boss, state);
        } else {
            GuardBreakHudSync.syncGuarded(boss, state.guardCount);
        }
    }

    public static void tick(DraugrBoss boss) {
        if (boss.level().isClientSide()) {
            return;
        }
        GuardBreakState state = STATES.get(boss.getUUID());
        if (state == null) {
            if (boss.tickCount % 10 == 0) {
                GuardBreakHudSync.syncIdle(boss);
            }
            return;
        }
        if (boss.isDeadOrDying()) {
            STATES.remove(boss.getUUID());
            GuardBreakHudSync.clearOrReset(boss);
            return;
        }
        if (state.stunTicks > 0) {
            boss.setPostureBroken(true);
            boss.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 20, false, true));
            pinBossInBreak(boss);
            spawnStunParticles(boss);
            --state.stunTicks;
            if (state.stunTicks <= 0) {
                clearStun(boss, state);
                return;
            }
        }
        if (boss.tickCount % 20 == 0) {
            int hudGuardCount = state.stunTicks > 0
                    ? ChaosMonarchConfig.getDraugrBossGuardBreakRequiredGuards()
                    : state.guardCount;
            GuardBreakHudSync.refresh(boss, hudGuardCount);
        }
        pruneEmptyStates();
    }

    public static boolean isStunned(DraugrBoss boss) {
        GuardBreakState state = STATES.get(boss.getUUID());
        return state != null && state.stunTicks > 0;
    }

    public static boolean consumeStunStarted(DraugrBoss boss) {
        GuardBreakState state = STATES.get(boss.getUUID());
        if (state == null || !state.stunStarted) {
            return false;
        }
        state.stunStarted = false;
        return true;
    }

    public static void clear(DraugrBoss boss) {
        STATES.remove(boss.getUUID());
        boss.setPostureBroken(false);
        DraugrAnimationSync.clearStun(boss);
        GuardBreakHudSync.clearOrReset(boss);
    }

    private static void startStun(DraugrBoss boss, GuardBreakState state) {
        state.guardCount = ChaosMonarchConfig.getDraugrBossGuardBreakRequiredGuards();
        state.stunTicks = STUN_TICKS;
        state.stunStarted = true;
        DraugrActionLockTracker.clear(boss);
        boss.setPostureBroken(true);
        boss.setState(DraugrBoss.States.IDLE);
        boss.setShielding(false);
        boss.updateDisableShield(false);
        pinBossInBreak(boss);
        playStunBurst(boss);
        DraugrAnimationSync.syncStun(boss, STUN_TICKS);
    }

    private static void clearStun(DraugrBoss boss, GuardBreakState state) {
        state.stunTicks = 0;
        state.stunStarted = false;
        clear(boss);
    }

    private static void pinBossInBreak(DraugrBoss boss) {
        boss.getNavigation().stop();
        boss.setDeltaMovement(0.0D, boss.getDeltaMovement().y, 0.0D);
        boss.hurtMarked = true;
    }

    private static void spawnStunParticles(DraugrBoss boss) {
        if (!(boss.level() instanceof ServerLevel level) || boss.tickCount % 2 != 0) {
            return;
        }
        double centerY = boss.getY() + boss.getBbHeight() + 0.25D;
        double radius = 0.85D;
        double baseAngle = boss.tickCount * 0.35D;
        for (int i = 0; i < 6; ++i) {
            double angle = baseAngle + i * (Math.PI / 3.0D);
            double x = boss.getX() + Math.cos(angle) * radius;
            double z = boss.getZ() + Math.sin(angle) * radius;
            level.sendParticles(ParticleTypes.ENCHANTED_HIT,
                    x, centerY, z,
                    1,
                    0.05D, 0.05D, 0.05D,
                    0.04D);
            level.sendParticles(ParticleTypes.CRIT,
                    x, centerY, z,
                    1,
                    0.04D, 0.04D, 0.04D,
                    0.03D);
        }
    }

    private static void playStunBurst(DraugrBoss boss) {
        if (!(boss.level() instanceof ServerLevel level)) {
            return;
        }
        double x = boss.getX();
        double y = boss.getY() + boss.getBbHeight() * 0.65D;
        double z = boss.getZ();
        level.playSound(null, x, y, z,
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.HOSTILE,
                1.1F,
                0.75F);
        level.sendParticles(ParticleTypes.EXPLOSION, x, y, z,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.ENCHANTED_HIT, x, y + 0.35D, z,
                64, 1.0D, 0.9D, 1.0D, 0.28D);
        level.sendParticles(ParticleTypes.CRIT, x, y + 0.35D, z,
                48, 0.85D, 0.8D, 0.85D, 0.2D);
    }

    private static void pruneEmptyStates() {
        Iterator<Map.Entry<UUID, GuardBreakState>> iterator = STATES.entrySet().iterator();
        while (iterator.hasNext()) {
            GuardBreakState state = iterator.next().getValue();
            if (state.stunTicks <= 0 && state.guardCount <= 0) {
                iterator.remove();
            }
        }
    }

    private static final class GuardBreakState {
        private int guardCount;
        private int stunTicks;
        private boolean stunStarted;
    }
}
