package me.wuntare.tradeautomat.network;

import me.wuntare.tradeautomat.Main;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SetCodePayload(BlockPos pos, String code) implements CustomPacketPayload {

    public static final Type<SetCodePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Main.MOD_ID, "set_code"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetCodePayload> CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, SetCodePayload::pos,
                    ByteBufCodecs.STRING_UTF8, SetCodePayload::code,
                    SetCodePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}