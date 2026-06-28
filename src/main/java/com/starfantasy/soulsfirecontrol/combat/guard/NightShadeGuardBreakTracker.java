package com.starfantasy.soulsfirecontrol.combat.guard;

import com.starfantasy.soulsfirecontrol.config.ChaosMonarchConfig;
import com.starfantasy.soulsfirecontrol.util.NightShadeTweaks;
import net.minecraft.server.level.ServerPlayer;
import net.soulsweaponry.entity.mobs.NightShade;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class NightShadeGuardBreakTracker {
    private static final Map<UUID, GuardBreakState> STATES = new HashMap<>();

    private NightShadeGuardBreakTracker() {
    }

    public static void recordPerfectGuard(NightShade boss, ServerPlayer player) {
        if (boss.level().isClientSide() || boss.isDeadOrDying() || boss.isCopy()
                || !NightShadeTweaks.rewardsGuardBreak(boss)) {
            return;
        }
        int requiredGuards = requiredGuards();
        GuardBreakState state = STATES.computeIfAbsent(boss.getUUID(), uuid -> new GuardBreakState());
        state.guardCount = Math.min(requiredGuards, state.guardCount + 1);
        if (state.guardCount >= requiredGuards) {
            GuardBreakHudSync.syncTriggered(boss, requiredGuards);
            executeBoss(boss, player);
        } else {
            GuardBreakHudSync.syncGuarded(boss, state.guardCount, requiredGuards);
        }
    }

    public static void tick(NightShade boss) {
        if (boss.level().isClientSide() || boss.isCopy()) {
            return;
        }
        int requiredGuards = requiredGuards();
        GuardBreakState state = STATES.get(boss.getUUID());
        if (boss.isDeadOrDying()) {
            STATES.remove(boss.getUUID());
            GuardBreakHudSync.clearOrReset(boss, requiredGuards);
            return;
        }
        if (state == null) {
            if (boss.tickCount % 10 == 0) {
                GuardBreakHudSync.syncIdle(boss, requiredGuards);
            }
            return;
        }
        if (boss.tickCount % 20 == 0) {
            GuardBreakHudSync.refresh(boss, state.guardCount, requiredGuards);
        }
        pruneEmptyStates();
    }

    public static void clear(NightShade boss) {
        STATES.remove(boss.getUUID());
        GuardBreakHudSync.clearOrReset(boss, requiredGuards());
    }

    private static int requiredGuards() {
        return Math.max(1, ChaosMonarchConfig.getNightShadeGuardBreakRequiredGuards());
    }

    private static void executeBoss(NightShade boss, ServerPlayer player) {
        boss.invulnerableTime = 0;
        float lethalDamage = Math.max(1000.0F, boss.getMaxHealth() * 100.0F);
        boss.hurt(boss.damageSources().playerAttack(player), lethalDamage);
        if (!boss.isDeadOrDying()) {
            boss.setHealth(0.0F);
            boss.setDeath();
        }
    }

    private static void pruneEmptyStates() {
        Iterator<Map.Entry<UUID, GuardBreakState>> iterator = STATES.entrySet().iterator();
        while (iterator.hasNext()) {
            GuardBreakState state = iterator.next().getValue();
            if (state.guardCount <= 0) {
                iterator.remove();
            }
        }
    }

    private static final class GuardBreakState {
        private int guardCount;
    }
}
