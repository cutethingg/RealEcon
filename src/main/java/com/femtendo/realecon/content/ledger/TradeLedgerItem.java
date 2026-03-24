package com.femtendo.realecon.content.ledger;

import com.femtendo.realecon.content.ledger.gui.TradeLedgerMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TradeLedgerItem extends Item {

    public TradeLedgerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack ledgerStack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                    (id, inv, p) -> new TradeLedgerMenu(id, inv),
                    Component.literal("Trade Ledger")
            ));
        }

        return InteractionResultHolder.sidedSuccess(ledgerStack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("SavedTrades")) {
            ListTag trades = tag.getList("SavedTrades", Tag.TAG_COMPOUND);
            tooltip.add(Component.literal("§6Registered Trades:"));

            for (int i = 0; i < trades.size(); i++) {
                CompoundTag tradeTag = trades.getCompound(i);
                ItemStack bounty = ItemStack.of(tradeTag.getCompound("Bounty"));
                ItemStack reward = ItemStack.of(tradeTag.getCompound("Reward"));

                tooltip.add(Component.literal("§7 " + (i + 1) + ". ")
                        .append(bounty.getHoverName()).append(" §8-> §f")
                        .append(reward.getHoverName()));
            }
        } else {
            tooltip.add(Component.literal("§8Empty Ledger - Open to add trades."));
        }
    }
}