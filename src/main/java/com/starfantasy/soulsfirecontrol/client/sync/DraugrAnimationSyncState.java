package com.starfantasy.soulsfirecontrol.client.sync;

import com.starfantasy.soulsfirecontrol.network.DraugrAnimationSyncPacket;
import net.soulsweaponry.entity.mobs.DraugrBoss;
import software.bernie.geckolib.core.animation.Animation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class DraugrAnimationSyncState {
    private static final Map<Integer, SyncedState> STATES = new HashMap<>();
    private static final int EXPIRY_GRACE_TICKS = 5;

    private DraugrAnimationSyncState() {
    }

    public static void accept(DraugrAnimationSyncPacket packet) {
        if (packet.mode() == DraugrAnimationSyncPacket.MODE_CLEAR) {
            STATES.remove(packet.entityId());
            return;
        }
        if (packet.mode() != DraugrAnimationSyncPacket.MODE_STUN || packet.durationTicks() <= 0) {
            return;
        }
        STATES.put(packet.entityId(), new SyncedState(
                packet.mode(),
                packet.stateOrdinal(),
                packet.durationTicks(),
                packet.sequence()
        ));
    }

    public static SyncedAnimation getActiveAnimation(DraugrBoss boss) {
        if (!boss.level().isClientSide()) {
            return null;
        }
        SyncedState state = STATES.get(boss.getId());
        if (state == null) {
            return null;
        }
        if (!boss.isAlive()
                || boss.isDeadOrDying()
                || boss.getState() == DraugrBoss.States.DEATH
                || boss.getDeathTicks() > 0) {
            STATES.remove(boss.getId());
            return null;
        }
        long now = boss.level().getGameTime();
        if (state.clientStartTick < 0L) {
            state.clientStartTick = now;
        }
        if (now - state.clientStartTick > state.durationTicks + EXPIRY_GRACE_TICKS) {
            STATES.remove(boss.getId());
            return null;
        }
        if (state.mode == DraugrAnimationSyncPacket.MODE_STUN) {
            return new SyncedAnimation("posture_break", Animation.LoopType.HOLD_ON_LAST_FRAME, state.sequence);
        }
        STATES.remove(boss.getId());
        return null;
    }

    public static boolean consumeReset(DraugrBoss boss, int sequence) {
        SyncedState state = STATES.get(boss.getId());
        if (state == null || state.sequence != sequence || !state.needsReset) {
            return false;
        }
        state.needsReset = false;
        return true;
    }

    public static void pruneForClientTick(long gameTime) {
        Iterator<Map.Entry<Integer, SyncedState>> iterator = STATES.entrySet().iterator();
        while (iterator.hasNext()) {
            SyncedState state = iterator.next().getValue();
            if (state.clientStartTick >= 0L && gameTime - state.clientStartTick > state.durationTicks + EXPIRY_GRACE_TICKS) {
                iterator.remove();
            }
        }
    }

    private static final class SyncedState {
        private final int mode;
        private final int stateOrdinal;
        private final int durationTicks;
        private final int sequence;
        private long clientStartTick = -1L;
        private boolean needsReset = true;

        private SyncedState(int mode, int stateOrdinal, int durationTicks, int sequence) {
            this.mode = mode;
            this.stateOrdinal = stateOrdinal;
            this.durationTicks = durationTicks;
            this.sequence = sequence;
        }
    }

    public record SyncedAnimation(String animationName, Animation.LoopType loopType, int sequence) {
    }
}
