package me.wuntare.tradeautomat.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SetScrollOffsetPayload(int containerId, int scrollOffset) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetScrollOffsetPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("tradeautomat", "set_scroll_offset"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetScrollOffsetPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SetScrollOffsetPayload::containerId,
            ByteBufCodecs.VAR_INT, SetScrollOffsetPayload::scrollOffset,
            SetScrollOffsetPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}