package me.wuntare.tradeautomat.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SetTerminalCodePayload(BlockPos pos, String code) implements CustomPacketPayload {
    public static final Type<SetTerminalCodePayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath("tradeautomat", "set_terminal_code")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SetTerminalCodePayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SetTerminalCodePayload::pos,
            ByteBufCodecs.STRING_UTF8, SetTerminalCodePayload::code,
            SetTerminalCodePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}