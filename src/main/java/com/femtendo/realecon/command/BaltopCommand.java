package com.femtendo.realecon.command;

import com.femtendo.realecon.capability.PlayerWealth;
import com.femtendo.realecon.capability.PlayerWealthProvider;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class BaltopCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("baltop")
                .executes(context -> {
                    context.getSource().sendSuccess(() -> Component.literal("§6--- Top Wealth (Online) ---"), false);

                    // Gather all currently online players
                    List<ServerPlayer> players = new ArrayList<>(context.getSource().getServer().getPlayerList().getPlayers());

                    // Sort players by Net Worth (Highest to Lowest)
                    players.sort((p1, p2) -> {
                        long w1 = p1.getCapability(PlayerWealthProvider.PLAYER_WEALTH).map(PlayerWealth::getNetWorth).orElse(0L);
                        long w2 = p2.getCapability(PlayerWealthProvider.PLAYER_WEALTH).map(PlayerWealth::getNetWorth).orElse(0L);
                        return Long.compare(w2, w1);
                    });

                    // Display top 10
                    int rank = 1;
                    for (int i = 0; i < Math.min(10, players.size()); i++) {
                        ServerPlayer p = players.get(i);
                        long wealth = p.getCapability(PlayerWealthProvider.PLAYER_WEALTH).map(PlayerWealth::getNetWorth).orElse(0L);

                        String rankDisplay = "§e" + rank + ". §a" + p.getName().getString() + " §7- §2$" + wealth;
                        context.getSource().sendSuccess(() -> Component.literal(rankDisplay), false);
                        rank++;
                    }

                    if (players.isEmpty()) {
                        context.getSource().sendSuccess(() -> Component.literal("§8No players online to rank."), false);
                    }

                    return 1;
                })
        );
    }
}