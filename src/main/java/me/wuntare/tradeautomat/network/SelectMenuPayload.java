package me.wuntare.tradeautomat.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SelectMenuPayload(BlockPos pos, int menuId) implements CustomPacketPayload {

    public static final Type<SelectMenuPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("tradeautomat", "select_menu"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SelectMenuPayload> CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, SelectMenuPayload::pos,
                    ByteBufCodecs.VAR_INT, SelectMenuPayload::menuId,
                    SelectMenuPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}