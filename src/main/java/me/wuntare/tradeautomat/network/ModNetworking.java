package me.wuntare.tradeautomat.network;

import me.wuntare.tradeautomat.block.entity.TradeAutomatEntity;
import me.wuntare.tradeautomat.gui.AutomatTradeMenu;
import me.wuntare.tradeautomat.gui.AutomatTradeSetupMenu;
import me.wuntare.tradeautomat.gui.EngineeringTerminalMenu;
import me.wuntare.tradeautomat.model.TradeOffer;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ModNetworking {

    public static void registerPackets() {
        PayloadTypeRegistry.clientboundPlay().register(OpenCodeScreenPayload.TYPE, OpenCodeScreenPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(OpenHubScreenPayload.TYPE, OpenHubScreenPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetCodePayload.TYPE, SetCodePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SelectMenuPayload.TYPE, SelectMenuPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ExecuteTradePayload.TYPE, ExecuteTradePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SaveTradeSetupPayload.TYPE, SaveTradeSetupPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetScrollOffsetPayload.TYPE, SetScrollOffsetPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetTerminalCodePayload.TYPE, SetTerminalCodePayload.CODEC);

        registerServerReceivers();
    }

    private static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(SetTerminalCodePayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                BlockPos pos = payload.pos();

                if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 64.0) return;

                if (player.containerMenu instanceof EngineeringTerminalMenu menu) {
                    if (menu.getBlockEntity() != null && menu.getBlockEntity().getBlockPos().equals(pos)) {
                        menu.updateCodeFromClient(payload.code());
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(SetCodePayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                if (player.level().getBlockEntity(payload.pos()) instanceof TradeAutomatEntity te) {
                    te.setCode(payload.code());
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(SetScrollOffsetPayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                if (player.containerMenu != null && player.containerMenu.containerId == payload.containerId()) {

                    if (player.containerMenu instanceof AutomatTradeSetupMenu setupMenu) {
                        setupMenu.setScrollOffset(payload.scrollOffset());
                    }
                    else if (player.containerMenu instanceof AutomatTradeMenu tradeMenu) {
                        tradeMenu.setScrollOffset(payload.scrollOffset());
                    }

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
                                return Component.translatable("tradeautomat.trade_setup");
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

        ServerPlayNetworking.registerGlobalReceiver(ExecuteTradePayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                BlockPos pos = payload.pos();

                if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 64.0) return;

                if (!(player.containerMenu instanceof AutomatTradeMenu menu) || !menu.getBlockEntity().getBlockPos().equals(pos)) return;

                TradeAutomatEntity te = menu.getBlockEntity();
                if (te == null) return;

                te.recalculateModules();

                int rawIndex = payload.tradeIndex();

                if (rawIndex < 0 || rawIndex >= te.getUnlockedTradeOffers() || rawIndex >= te.getTrades().size()) return;

                if (!menu.isTradeExecutableByRawIndex(rawIndex, player)) return;

                TradeOffer offer = te.getTrades().get(rawIndex);
                if (offer == null || !offer.isValid() || !offer.isActive()) return;

                if (!te.hasProductsInStock(offer.getOutputs())) return;
                if (!te.canAcceptTradeInputs(offer)) return;

                List<ItemStack> aggregatedInputs = aggregateItemStacks(offer.getInputs());

                for (ItemStack input : aggregatedInputs) {
                    int countInInventory = player.getInventory().clearOrCountMatchingItems(
                            stack -> ItemStack.isSameItemSameComponents(stack, input),
                            0,
                            player.inventoryMenu.getCraftSlots()
                    );

                    if (countInInventory < input.getCount()) {
                        menu.broadcastChanges();
                        player.containerMenu.sendAllDataToRemote();
                        return;
                    }
                }

                for (ItemStack input : aggregatedInputs) {
                    int removed = player.getInventory().clearOrCountMatchingItems(
                            stack -> ItemStack.isSameItemSameComponents(stack, input),
                            input.getCount(),
                            player.inventoryMenu.getCraftSlots()
                    );

                    if (removed > 0) {
                        ItemStack payment = input.copyWithCount(removed);
                        te.depositPayment(payment);
                    }
                }

                for (ItemStack output : offer.getOutputs()) {
                    if (!output.isEmpty()) {
                        ItemStack reward = output.copy();
                        te.extractProduct(reward);

                        if (!player.getInventory().add(reward)) {
                            player.drop(reward, false);
                        }
                    }
                }

                te.syncToClient();
                menu.broadcastChanges();
                player.containerMenu.sendAllDataToRemote();
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(SaveTradeSetupPayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                BlockPos pos = payload.pos();

                if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 64.0) {
                    return;
                }

                if (!(player.containerMenu instanceof AutomatTradeSetupMenu setupMenu)) {
                    return;
                }

                TradeAutomatEntity te = setupMenu.getBlockEntity();
                if (te == null || !te.getBlockPos().equals(pos)) {
                    return;
                }

                te.setTrades(payload.trades());
                te.setChanged();
            });
        });
    }
    private static List<ItemStack> aggregateItemStacks(List<ItemStack> stacks) {
        List<ItemStack> aggregated = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) continue;
            boolean merged = false;
            for (ItemStack agg : aggregated) {
                if (ItemStack.isSameItemSameComponents(agg, stack)) {
                    agg.grow(stack.getCount());
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                aggregated.add(stack.copy());
            }
        }
        return aggregated;
    }
}