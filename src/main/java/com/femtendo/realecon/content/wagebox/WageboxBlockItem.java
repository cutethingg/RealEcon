package com.femtendo.realecon.content.wagebox;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WageboxBlockItem extends BlockItem {

    public WageboxBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        CompoundTag tag = stack.getTagElement("BlockEntityTag");

        if (tag != null && tag.contains("Inventory")) {
            ItemStackHandler handler = new ItemStackHandler(27);
            handler.deserializeNBT(tag.getCompound("Inventory"));

            int itemsListed = 0;
            int totalItems = 0;

            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack invStack = handler.getStackInSlot(i);
                if (!invStack.isEmpty()) {
                    totalItems++;
                    // Only list the first 4 distinct items to prevent massive screens-spanning tooltips
                    if (itemsListed < 4) {
                        tooltip.add(Component.literal(" - " + invStack.getCount() + "x ").withStyle(ChatFormatting.GRAY)
                                .append(invStack.getHoverName().copy().withStyle(ChatFormatting.YELLOW)));
                        itemsListed++;
                    }
                }
            }

            if (totalItems > 4) {
                tooltip.add(Component.literal("  ...and " + (totalItems - 4) + " more stacks").withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
            }

            if (totalItems == 0) {
                tooltip.add(Component.literal(" Empty").withStyle(ChatFormatting.GRAY));
            }
        } else {
            tooltip.add(Component.literal(" Empty").withStyle(ChatFormatting.GRAY));
        }
    }
}