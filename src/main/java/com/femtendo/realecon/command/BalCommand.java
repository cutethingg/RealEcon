package com.femtendo.realecon.command;

import com.femtendo.realecon.MessageConfig;
import com.femtendo.realecon.capability.PlayerWealthProvider;
import com.femtendo.realecon.logic.WealthScanner;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class BalCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bal")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();

                    // Force wealth scan
                    WealthScanner.updatePlayerWealth(player, true);

                    player.getCapability(PlayerWealthProvider.PLAYER_WEALTH).ifPresent(wealth -> {
                        long total = wealth.getNetWorth();

                        String symbol = MessageConfig.CURRENCY_SYMBOL.get();
                        String rawMessage = MessageConfig.BAL_COMMAND_REPLY.get();

                        String finalMessage = rawMessage.replace("%symbol%", symbol).replace("%total%", String.valueOf(total));
                        player.sendSystemMessage(Component.literal(MessageConfig.format(finalMessage)));
                    });

                    return 1;
                })
        );
    }
}