package com.femtendo.realecon.content.ledger;

import com.femtendo.realecon.Config;
import com.femtendo.realecon.capability.PlayerWealth;
import com.femtendo.realecon.capability.PlayerWealthProvider;
import com.femtendo.realecon.content.ledger.data.ActiveTradeNode;
import com.femtendo.realecon.logic.EmpireManager;
import com.femtendo.realecon.logic.market.GlobalMarketManager;
import com.femtendo.realecon.network.PacketHandler;
import com.femtendo.realecon.network.SyncEmpireDataPacket;
import com.femtendo.realecon.network.SyncMarketIndexPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

public class MasterLedgerItem extends Item {

    public MasterLedgerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack ledgerStack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ServerLevel serverLevel = (ServerLevel) level;
            EmpireManager manager = EmpireManager.get(serverLevel);

            CompoundTag profileTag = manager.getProfile(player.getUUID()).serializeNBT();

            long netWorth = player.getCapability(PlayerWealthProvider.PLAYER_WEALTH).map(PlayerWealth::getNetWorth).orElse(0L);
            profileTag.putLong("NetWorth", netWorth);

            GlobalMarketManager market = GlobalMarketManager.get(serverPlayer.getServer());
            Map<String, Double> index = market.getPriceIndex();

            double totalStockValue = 0.0;

            CompoundTag yellowPagesTag = new CompoundTag();
            for (Map.Entry<Long, ActiveTradeNode> entry : manager.getActiveWageboxes().entrySet()) {
                ActiveTradeNode node = entry.getValue();
                yellowPagesTag.put(entry.getKey().toString(), node.serializeNBT());

                // THE PHYSICAL INVENTORY SCANNER
                if (node.getOwnerId().equals(player.getUUID())) {
                    BlockPos pos = BlockPos.of(entry.getKey());
                    if (serverLevel.isLoaded(pos)) { // Prevents chunk-loading lag
                        if (serverLevel.getBlockEntity(pos) instanceof com.femtendo.realecon.content.wagebox.WageboxBlockEntity wagebox) {
                            net.minecraftforge.items.ItemStackHandler inv = wagebox.getInventory();
                            for (int i = 0; i < inv.getSlots(); i++) {
                                ItemStack stack = inv.getStackInSlot(i);
                                if (!stack.isEmpty()) {
                                    String key = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
                                    totalStockValue += index.getOrDefault(key, 0.0) * stack.getCount();
                                }
                            }
                        }
                    }
                }
            }

            profileTag.putDouble("ShopStockValue", totalStockValue);

            int tradesSince = market.getTradesSinceLastUpdate();
            int maxTrades = Config.MARKET_UPDATE_TRADES.get();
            PacketHandler.sendToPlayer(new SyncMarketIndexPacket(index, tradesSince, maxTrades), serverPlayer);

            PacketHandler.sendToPlayer(new SyncEmpireDataPacket(profileTag, yellowPagesTag), serverPlayer);
        }

        return InteractionResultHolder.sidedSuccess(ledgerStack, level.isClientSide());
    }
}