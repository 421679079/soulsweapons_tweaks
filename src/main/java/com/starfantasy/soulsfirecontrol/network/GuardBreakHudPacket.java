package com.starfantasy.soulsfirecontrol.network;

import com.starfantasy.soulsfirecontrol.client.hud.GuardBreakHudClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record GuardBreakHudPacket(int entityId, UUID bossEventId, int guardCount, int requiredGuards,
                                  boolean visible) {
    public static void encode(GuardBreakHudPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId);
        buffer.writeUUID(packet.bossEventId);
        buffer.writeVarInt(packet.guardCount);
        buffer.writeVarInt(packet.requiredGuards);
        buffer.writeBoolean(packet.visible);
    }

    public static GuardBreakHudPacket decode(FriendlyByteBuf buffer) {
        return new GuardBreakHudPacket(
                buffer.readVarInt(),
                buffer.readUUID(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readBoolean()
        );
    }

    public static void handle(GuardBreakHudPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> GuardBreakHudClientState.accept(packet));
        context.setPacketHandled(true);
    }
}
