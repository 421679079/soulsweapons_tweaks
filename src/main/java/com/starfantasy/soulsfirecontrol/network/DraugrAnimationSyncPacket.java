package com.starfantasy.soulsfirecontrol.network;

import com.starfantasy.soulsfirecontrol.client.sync.DraugrAnimationSyncState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record DraugrAnimationSyncPacket(int entityId, int mode, int stateOrdinal, int durationTicks, int sequence) {
    public static final int MODE_CLEAR = 0;
    public static final int MODE_STUN = 2;

    public static void encode(DraugrAnimationSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId);
        buffer.writeVarInt(packet.mode);
        buffer.writeVarInt(packet.stateOrdinal);
        buffer.writeVarInt(packet.durationTicks);
        buffer.writeVarInt(packet.sequence);
    }

    public static DraugrAnimationSyncPacket decode(FriendlyByteBuf buffer) {
        return new DraugrAnimationSyncPacket(
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt()
        );
    }

    public static void handle(DraugrAnimationSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DraugrAnimationSyncState.accept(packet));
        context.setPacketHandled(true);
    }
}
