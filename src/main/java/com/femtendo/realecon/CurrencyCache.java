package com.femtendo.realecon;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class CurrencyCache {
    private static final Map<Item, Integer> itemValues = new HashMap<>();
    private static final Map<Block, Integer> blockValues = new HashMap<>(); // NEW: Fast block lookup
    private static final Map<TagKey<Item>, Integer> tagValues = new HashMap<>();

    public static void rebuildCache() {
        itemValues.clear();
        blockValues.clear();
        tagValues.clear();

        for (String entry : Config.CURRENCY_ITEMS.get()) {
            try {
                String[] parts = entry.split(",");
                if (parts.length != 2) continue;

                String key = parts[0].trim();
                int value = Integer.parseInt(parts[1].trim());

                if (key.startsWith("#")) {
                    ResourceLocation tagId = new ResourceLocation(key.substring(1));
                    tagValues.put(ItemTags.create(tagId), value);
                } else {
                    ResourceLocation itemId = new ResourceLocation(key);
                    Item item = ForgeRegistries.ITEMS.getValue(itemId);
                    if (item != null) {
                        itemValues.put(item, value);

                        // NEW: If the item can be placed as a block, cache the block too
                        if (item instanceof BlockItem blockItem) {
                            blockValues.put(blockItem.getBlock(), value);
                        }
                    }
                }
            } catch (NumberFormatException ignored) {
                System.out.println("[RealEcon] Invalid currency value in config: " + entry);
            }
        }
    }

    public static int getValue(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        Item item = stack.getItem();
        int amount = stack.getCount();

        if (itemValues.containsKey(item)) return itemValues.get(item) * amount;

        for (Map.Entry<TagKey<Item>, Integer> tagEntry : tagValues.entrySet()) {
            if (stack.is(tagEntry.getKey())) return tagEntry.getValue() * amount;
        }
        return 0;
    }

    // NEW: Instantly checks if a physical block in the world is currency
    public static int getBlockValue(BlockState state) {
        return blockValues.getOrDefault(state.getBlock(), 0);
    }
}