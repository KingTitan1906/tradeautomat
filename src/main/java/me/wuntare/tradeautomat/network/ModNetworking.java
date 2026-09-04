package me.wuntare.tradeautomat.network;

import me.wuntare.tradeautomat.block.entity.TradeAutomatEntity;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class ModNetworking {

    public static void registerPackets() {
        PayloadTypeRegistry.clientboundPlay().register(OpenCodeScreenPayload.TYPE, OpenCodeScreenPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(OpenHubScreenPayload.TYPE, OpenHubScreenPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetCodePayload.TYPE, SetCodePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SelectMenuPayload.TYPE, SelectMenuPayload.CODEC);

        registerServerReceivers();
    }

    private static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(SetCodePayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                if (player.level().getBlockEntity(payload.pos()) instanceof TradeAutomatEntity te) {
                    te.setCode(payload.code());
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(SelectMenuPayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                if (player.level().getBlockEntity(payload.pos()) instanceof TradeAutomatEntity te) {
                    if (payload.menuId() == 1) {
                        player.openMenu(te);
                    }
                }
            });
        });
    }
}