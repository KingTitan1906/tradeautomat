package me.wuntare.tradeautomat.network;

import me.wuntare.tradeautomat.Main;
import me.wuntare.tradeautomat.model.TradeOffer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

public record SaveTradeSetupPayload(BlockPos pos, List<TradeOffer> trades) implements CustomPacketPayload {
    public static final Type<SaveTradeSetupPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Main.MOD_ID, "save_trade_setup"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SaveTradeSetupPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SaveTradeSetupPayload::pos,
            TradeOffer.STREAM_CODEC.apply(ByteBufCodecs.list()), SaveTradeSetupPayload::trades,
            SaveTradeSetupPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}