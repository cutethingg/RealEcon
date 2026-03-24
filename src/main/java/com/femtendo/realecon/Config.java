package com.femtendo.realecon;

import net.minecraftforge.common.ForgeConfigSpec;
import java.util.Arrays;
import java.util.List;

public class Config {
    public static final ForgeConfigSpec SERVER_SPEC;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CURRENCY_ITEMS;

    public static final ForgeConfigSpec.ConfigValue<String> ANCHOR_CURRENCY;
    public static final ForgeConfigSpec.ConfigValue<Integer> MARKET_UPDATE_TRADES;
    public static final ForgeConfigSpec.EnumValue<MarketMathMode> MARKET_MATH_MODE;
    public static final ForgeConfigSpec.ConfigValue<Double> WMA_WEIGHT;

    public enum MarketMathMode {
        WMA,
        AVERAGE
    }

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("=========================================",
                        " REALECON CURRENCY CONFIGURATION",
                        "=========================================",
                        "IMPORTANT NOTE ON MATH & TRADING:",
                        "All currency values are strictly WHOLE NUMBERS (Integers).",
                        "There are NO decimals. 1 unit = 1 minimum physical value.",
                        "When setting up trades or shops, use this 1:1 integer ratio.",
                        "",
                        "SYNTAX INSTRUCTIONS:",
                        "1. Specific Items: 'modid:item_name,value'",
                        "2. Tags (Broad matching): '#modid:tag_name,value'",
                        "3. Negative values are allowed to create 'debt' items")
                .push("Currency");

        CURRENCY_ITEMS = builder.defineListAllowEmpty(List.of("currency_list"),
                () -> Arrays.asList(
                        "minecraft:gold_nugget,1",
                        "minecraft:gold_ingot,9",
                        "minecraft:gold_block,81"
                ),
                obj -> obj instanceof String && ((String) obj).contains(","));

        builder.pop();

        builder.push("GlobalMarket");

        ANCHOR_CURRENCY = builder
                .comment("The item used as the $1.00 baseline for all market calculations.")
                .define("anchorCurrency", "minecraft:gold_nugget");

        // NEW: Fast update cycle
        MARKET_UPDATE_TRADES = builder
                .comment("How many trades must occur before the server updates the global market prices.")
                .defineInRange("marketUpdateTrades", 10, 1, 100000);

        MARKET_MATH_MODE = builder
                .comment("WMA (Weighted Moving Average) adjusts prices faster based on recent trades. AVERAGE counts all history equally.")
                .defineEnum("mathMode", MarketMathMode.WMA);

        WMA_WEIGHT = builder
                .comment("If using WMA, how much weight does the NEW trade have vs the OLD history? (0.2 = 20% New, 80% Old)")
                .defineInRange("wmaWeight", 0.2, 0.01, 1.0);

        builder.pop();

        SERVER_SPEC = builder.build();
    }
}