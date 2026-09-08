package me.wuntare.tradeautomat.network;

import me.wuntare.tradeautomat.Main;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ExecuteTradePayload(BlockPos pos, int tradeIndex) implements CustomPacketPayload {
    public static final Type<ExecuteTradePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Main.MOD_ID, "execute_trade"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ExecuteTradePayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ExecuteTradePayload::pos,
            StreamCodec.of(RegistryFriendlyByteBuf::writeInt, RegistryFriendlyByteBuf::readInt), ExecuteTradePayload::tradeIndex,
            ExecuteTradePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}