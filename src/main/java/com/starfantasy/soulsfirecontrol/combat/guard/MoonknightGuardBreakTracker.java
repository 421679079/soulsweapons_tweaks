package com.starfantasy.soulsfirecontrol.combat.guard;

import com.starfantasy.soulsfirecontrol.config.ChaosMonarchConfig;
import com.starfantasy.soulsfirecontrol.util.MoonknightTweaks;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.soulsweaponry.entity.mobs.Moonknight;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class MoonknightGuardBreakTracker {
    private static final int GUARD_CHANGE_COOLDOWN_TICKS = 10;
    private static final Map<UUID, GuardBreakState> STATES = new HashMap<>();

    private MoonknightGuardBreakTracker() {
    }

    public static void recordPerfectGuard(Moonknight boss, ServerPlayer player) {
        if (boss.level().isClientSide() || boss.isDeadOrDying() || !boss.isPhaseTwo()
                || !MoonknightTweaks.rewardsGuardBreak(boss)) {
            return;
        }
        int required = requiredGuards();
        GuardBreakState state = STATES.computeIfAbsent(boss.getUUID(), uuid -> new GuardBreakState());
        if (state.awaitingCoreBeam || state.coreBeamActive || isOnCooldown(boss.tickCount, state.lastGuardTick)) {
            return;
        }
        state.lastGuardTick = boss.tickCount;
        state.guardCount = Math.min(required, state.guardCount + 1);
        if (state.guardCount >= required) {
            state.guardCount = required;
            state.awaitingCoreBeam = true;
            GuardBreakHudSync.syncTriggered(boss, required);
            playBreakBurst(boss);
        } else {
            GuardBreakHudSync.syncGuarded(boss, state.guardCount, required);
        }
    }

    public static void tick(Moonknight boss) {
        if (boss.level().isClientSide()) {
            return;
        }
        int required = requiredGuards();
        GuardBreakState state = STATES.get(boss.getUUID());
        if (boss.isDeadOrDying()) {
            STATES.remove(boss.getUUID());
            GuardBreakHudSync.clearOrReset(boss, required);
            return;
        }
        if (!boss.isPhaseTwo()) {
            if (state != null) {
                STATES.remove(boss.getUUID());
            }
            if (boss.tickCount % 20 == 0) {
                GuardBreakHudSync.hide(boss, required);
            }
            return;
        }
        if (state == null) {
            if (boss.tickCount % 10 == 0) {
                GuardBreakHudSync.syncIdle(boss, required);
            }
            return;
        }
        if (state.coreBeamActive && !boss.getPhaseTwoAttack().equals(Moonknight.MoonknightPhaseTwo.CORE_BEAM)) {
            resetAfterCoreBeam(boss, state, required);
            return;
        }
        if ((state.awaitingCoreBeam || state.coreBeamActive) && boss.tickCount % 3 == 0) {
            spawnChargedParticles(boss);
        }
        if (boss.tickCount % 20 == 0) {
            GuardBreakHudSync.refresh(boss, state.guardCount, required);
        }
        pruneEmptyStates();
    }

    public static boolean shouldForceCoreBeam(Moonknight boss) {
        GuardBreakState state = STATES.get(boss.getUUID());
        return state != null && state.awaitingCoreBeam && !state.coreBeamActive && boss.isPhaseTwo();
    }

    public static void markCoreBeamStarted(Moonknight boss) {
        GuardBreakState state = STATES.get(boss.getUUID());
        if (state == null) {
            return;
        }
        state.awaitingCoreBeam = false;
        state.coreBeamActive = true;
        state.guardCount = requiredGuards();
        GuardBreakHudSync.syncTriggered(boss, requiredGuards());
    }

    public static boolean isGuardBroken(Moonknight boss) {
        GuardBreakState state = STATES.get(boss.getUUID());
        return state != null && (state.awaitingCoreBeam || state.coreBeamActive);
    }

    private static int requiredGuards() {
        return Math.max(1, ChaosMonarchConfig.getMoonknightGuardBreakRequiredGuards());
    }

    private static boolean isOnCooldown(int currentTick, int lastTick) {
        return lastTick != Integer.MIN_VALUE && currentTick - lastTick < GUARD_CHANGE_COOLDOWN_TICKS;
    }

    private static void resetAfterCoreBeam(Moonknight boss, GuardBreakState state, int required) {
        state.guardCount = 0;
        state.awaitingCoreBeam = false;
        state.coreBeamActive = false;
        GuardBreakHudSync.syncIdle(boss, required);
    }

    private static void playBreakBurst(Moonknight boss) {
        if (!(boss.level() instanceof ServerLevel level)) {
            return;
        }
        double x = boss.getX();
        double y = boss.getY() + boss.getBbHeight() * 0.65D;
        double z = boss.getZ();
        level.playSound(null, x, y, z,
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.HOSTILE,
                1.35F,
                0.6F);
        level.sendParticles(ParticleTypes.FLASH, x, y, z,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.END_ROD, x, y + 0.25D, z,
                80, 1.4D, 1.0D, 1.4D, 0.2D);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y + 0.1D, z,
                60, 1.2D, 0.8D, 1.2D, 0.12D);
    }

    private static void spawnChargedParticles(Moonknight boss) {
        if (!(boss.level() instanceof ServerLevel level)) {
            return;
        }
        double centerY = boss.getY() + boss.getBbHeight() * 0.75D;
        double radius = 1.35D;
        double baseAngle = boss.tickCount * 0.45D;
        for (int i = 0; i < 8; ++i) {
            double angle = baseAngle + i * (Math.PI / 4.0D);
            double x = boss.getX() + Math.cos(angle) * radius;
            double z = boss.getZ() + Math.sin(angle) * radius;
            level.sendParticles(ParticleTypes.END_ROD,
                    x, centerY, z,
                    1,
                    0.02D, 0.06D, 0.02D,
                    0.025D);
        }
    }

    private static void pruneEmptyStates() {
        Iterator<Map.Entry<UUID, GuardBreakState>> iterator = STATES.entrySet().iterator();
        while (iterator.hasNext()) {
            GuardBreakState state = iterator.next().getValue();
            if (state.guardCount <= 0 && !state.awaitingCoreBeam && !state.coreBeamActive) {
                iterator.remove();
            }
        }
    }

    private static final class GuardBreakState {
        private int guardCount;
        private boolean awaitingCoreBeam;
        private boolean coreBeamActive;
        private int lastGuardTick = Integer.MIN_VALUE;
    }
}
