package com.femtendo.realecon;

import net.minecraftforge.common.ForgeConfigSpec;

public class MessageConfig {
    public static final ForgeConfigSpec SERVER_SPEC;
    public static final ForgeConfigSpec.ConfigValue<String> CURRENCY_SYMBOL;
    public static final ForgeConfigSpec.ConfigValue<String> BAL_COMMAND_REPLY;

    // Baltop Configs
    public static final ForgeConfigSpec.ConfigValue<String> BALTOP_HEADER;
    public static final ForgeConfigSpec.ConfigValue<String> BALTOP_ROW;
    public static final ForgeConfigSpec.ConfigValue<String> BALTOP_FOOTER;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("=========================================",
                        " REALECON MESSAGES & COLORS",
                        "=========================================",
                        "Use the '&' symbol for formatting codes:",
                        "&0 = Black       &8 = Dark Gray",
                        "&1 = Dark Blue   &9 = Blue",
                        "&2 = Dark Green  &a = Green",
                        "&3 = Dark Aqua   &b = Aqua",
                        "&4 = Dark Red    &c = Red",
                        "&5 = Dark Purple &d = Light Purple",
                        "&6 = Gold        &e = Yellow",
                        "&7 = Gray        &f = White",
                        "&l = Bold        &o = Italic",
                        "&n = Underline   &m = Strikethrough",
                        "&r = Reset",
                        "=========================================")
                .push("Formatting");

        CURRENCY_SYMBOL = builder
                .comment("The symbol used to represent the currency (e.g., $, £, G, ¢)")
                .define("currencySymbol", "$");

        BAL_COMMAND_REPLY = builder
                .comment("The message sent when a player types /bal.",
                        "Use %symbol% for the currency symbol and %total% for the player's net worth.")
                .define("balCommandReply", "&eYour current Net Worth is: &6%symbol%%total%");

        builder.pop().push("Baltop");

        BALTOP_HEADER = builder
                .comment("The top line of the leaderboard.")
                .define("baltopHeader", "&8&m--------&r &e&lWealth Leaderboard&r &8&m--------");

        BALTOP_ROW = builder
                .comment("The format for each player in the leaderboard.",
                        "Placeholders: %rank%, %player%, %symbol%, %balance%")
                .define("baltopRow", "&7%rank%. &f%player% &8- &a%symbol%%balance%");

        BALTOP_FOOTER = builder
                .comment("The bottom line of the leaderboard.")
                .define("baltopFooter", "&8&m----------------------------------");

        builder.pop();
        SERVER_SPEC = builder.build();
    }

    public static String format(String text) {
        return text.replace("&", "§");
    }
}