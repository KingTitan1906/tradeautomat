package me.wuntare.tradeautomat.network;

import me.wuntare.tradeautomat.Main;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record OpenHubScreenPayload(BlockPos pos) implements CustomPacketPayload {

    public static final Type<OpenHubScreenPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Main.MOD_ID, "open_hub_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenHubScreenPayload> CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, OpenHubScreenPayload::pos,
                    OpenHubScreenPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}