package me.wuntare.tradeautomat.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record OpenCodeScreenPayload(BlockPos pos) implements CustomPacketPayload {

    public static final Type<OpenCodeScreenPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("tradeautomat", "open_code_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenCodeScreenPayload> CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, OpenCodeScreenPayload::pos,
                    OpenCodeScreenPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}