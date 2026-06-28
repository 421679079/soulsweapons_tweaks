package com.starfantasy.soulsfirecontrol.combat.draugr;

import net.soulsweaponry.entity.mobs.DraugrBoss;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class DraugrActionLockTracker {
    private static final Map<UUID, LockState> STATES = new HashMap<>();

    private DraugrActionLockTracker() {
    }

    public static void lockCurrentAction(DraugrBoss boss, int maxTicks) {
        if (boss.level().isClientSide() || maxTicks <= 0) {
            return;
        }
        DraugrBoss.States state = boss.getState();
        if (!isAttackState(state)) {
            return;
        }
        LockState lock = STATES.computeIfAbsent(boss.getUUID(), uuid -> new LockState());
        if (lock.ticks > 0 && lock.state == state) {
            return;
        }
        lock.state = state;
        lock.ticks = maxTicks + 2;
    }

    public static int normalizeAttackStatus(DraugrBoss boss, int attackStatus, int maxTicks) {
        if (boss.level().isClientSide() || maxTicks <= 0) {
            return attackStatus;
        }
        DraugrBoss.States state = boss.getState();
        if (!isAttackState(state)) {
            return attackStatus;
        }
        LockState lock = STATES.get(boss.getUUID());
        if (lock == null || lock.ticks <= 0 || lock.state != state) {
            return 0;
        }
        return attackStatus > maxTicks + 2 ? 0 : attackStatus;
    }

    public static void clear(DraugrBoss boss) {
        STATES.remove(boss.getUUID());
    }

    public static boolean canChangeState(DraugrBoss boss, DraugrBoss.States nextState) {
        LockState lock = STATES.get(boss.getUUID());
        if (lock == null || lock.ticks <= 0 || lock.state == null) {
            return true;
        }
        return nextState == lock.state || isUninterruptibleOverride(nextState);
    }

    public static void tick(DraugrBoss boss) {
        if (boss.level().isClientSide()) {
            return;
        }
        LockState lock = STATES.get(boss.getUUID());
        if (lock == null) {
            return;
        }
        if (boss.isDeadOrDying()) {
            STATES.remove(boss.getUUID());
            return;
        }
        if (lock.ticks > 0) {
            --lock.ticks;
        }
        pruneExpiredStates();
    }

    public static boolean isUninterruptibleOverride(DraugrBoss.States state) {
        return state == DraugrBoss.States.SPAWN || state == DraugrBoss.States.DEATH;
    }

    private static boolean isAttackState(DraugrBoss.States state) {
        return state != DraugrBoss.States.IDLE
                && state != DraugrBoss.States.SPAWN
                && state != DraugrBoss.States.DEATH;
    }

    private static void pruneExpiredStates() {
        Iterator<Map.Entry<UUID, LockState>> iterator = STATES.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().ticks <= 0) {
                iterator.remove();
            }
        }
    }

    private static final class LockState {
        private DraugrBoss.States state;
        private int ticks;
    }
}
