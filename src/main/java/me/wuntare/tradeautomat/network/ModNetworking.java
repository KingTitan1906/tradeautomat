package me.wuntare.tradeautomat.network;

import me.wuntare.tradeautomat.block.entity.TradeAutomatEntity;
import me.wuntare.tradeautomat.gui.AutomatTradeSetupMenu;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

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
                    if (payload.menuId() == 0) {
                        player.openMenu(new ExtendedMenuProvider<BlockPos>() {
                            @Override
                            public BlockPos getScreenOpeningData(ServerPlayer player) {
                                return te.getBlockPos();
                            }

                            @Override
                            public Component getDisplayName() {
                                return Component.literal("Trade Setup");
                            }

                            @Override
                            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
                                return new AutomatTradeSetupMenu(containerId, inventory, te);
                            }
                        });
                    } else if (payload.menuId() == 1) {
                        player.openMenu(te);
                    }
                }
            });
        });
    }
}