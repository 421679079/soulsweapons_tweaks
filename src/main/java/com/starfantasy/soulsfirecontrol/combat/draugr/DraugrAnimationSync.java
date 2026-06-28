package com.starfantasy.soulsfirecontrol.combat.draugr;

import com.starfantasy.soulsfirecontrol.network.DraugrAnimationSyncPacket;
import com.starfantasy.soulsfirecontrol.network.SoulsTweaksNetwork;
import net.soulsweaponry.entity.mobs.DraugrBoss;
import net.minecraftforge.network.PacketDistributor;

public final class DraugrAnimationSync {
    private DraugrAnimationSync() {
    }

    public static void syncStun(DraugrBoss boss, int durationTicks) {
        if (boss.level().isClientSide() || durationTicks <= 0) {
            return;
        }
        send(boss, DraugrAnimationSyncPacket.MODE_STUN, DraugrBoss.States.IDLE.ordinal(), durationTicks);
    }

    public static void clearStun(DraugrBoss boss) {
        if (boss.level().isClientSide()) {
            return;
        }
        send(boss, DraugrAnimationSyncPacket.MODE_CLEAR, DraugrBoss.States.IDLE.ordinal(), 0);
    }

    private static void send(DraugrBoss boss, int mode, int stateOrdinal, int durationTicks) {
        SoulsTweaksNetwork.CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY.with(() -> boss),
                new DraugrAnimationSyncPacket(boss.getId(), mode, stateOrdinal, durationTicks, boss.tickCount)
        );
    }
}
