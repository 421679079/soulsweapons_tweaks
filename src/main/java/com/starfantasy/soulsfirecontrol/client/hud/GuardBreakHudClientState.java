package com.starfantasy.soulsfirecontrol.client.hud;

import com.starfantasy.soulsfirecontrol.network.GuardBreakHudPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class GuardBreakHudClientState {
    private static final Map<UUID, HudState> STATES = new HashMap<>();

    private GuardBreakHudClientState() {
    }

    public static void accept(GuardBreakHudPacket packet) {
        if (!packet.visible()) {
            STATES.remove(packet.bossEventId());
            return;
        }
        int requiredGuards = Math.max(1, packet.requiredGuards());
        int guardCount = Math.max(0, Math.min(packet.guardCount(), requiredGuards));
        STATES.put(packet.bossEventId(), new HudState(
                packet.entityId(),
                guardCount,
                requiredGuards,
                System.currentTimeMillis()
        ));
    }

    public static HudState get(UUID bossEventId) {
        HudState state = STATES.get(bossEventId);
        if (state == null) {
            return null;
        }
        if (System.currentTimeMillis() - state.lastUpdateMillis > 5000L) {
            STATES.remove(bossEventId);
            return null;
        }
        return state;
    }

    public record HudState(int entityId, int guardCount, int requiredGuards, long lastUpdateMillis) {
    }
}
