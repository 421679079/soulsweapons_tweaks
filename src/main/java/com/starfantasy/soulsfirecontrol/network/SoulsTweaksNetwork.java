package com.starfantasy.soulsfirecontrol.network;

import com.starfantasy.soulsfirecontrol.StarFantasySoulsFireControl;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class SoulsTweaksNetwork {
    private static final String PROTOCOL_VERSION = "2";
    private static int packetId;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            StarFantasySoulsFireControl.id("main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private SoulsTweaksNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(
                packetId++,
                DraugrAnimationSyncPacket.class,
                DraugrAnimationSyncPacket::encode,
                DraugrAnimationSyncPacket::decode,
                DraugrAnimationSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                packetId++,
                GuardBreakHudPacket.class,
                GuardBreakHudPacket::encode,
                GuardBreakHudPacket::decode,
                GuardBreakHudPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }
}
