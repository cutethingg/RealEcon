package com.femtendo.realecon.command;

import com.femtendo.realecon.capability.PlayerWealth; // Import the Implementation
import com.femtendo.realecon.capability.PlayerWealthProvider;
import com.femtendo.realecon.content.wagebox.WageboxBlockEntity;
import com.femtendo.realecon.content.wagebox.WageboxStorageMenu;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;

public class DebugCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("econadmin")
                .requires(source -> source.hasPermission(2))

                // NEW: The Root Execution Block (The Help Menu)
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    source.sendSuccess(() -> Component.literal("§6--- RealEcon Command Help ---"), false);
                    source.sendSuccess(() -> Component.literal("§e/bal §7- Calculates your total net worth and prints it to chat."), false);
                    source.sendSuccess(() -> Component.literal("§e/baltop §7- Scans online players and prints a Top 10 Wealth Leaderboard."), false);
                    source.sendSuccess(() -> Component.literal("§e/econadmin own_all §7- Grants admin access to bypass security and open all Wageboxes."), false);
                    source.sendSuccess(() -> Component.literal("§e/econadmin own_none §7- Revokes access to all Wageboxes (including your own) for testing."), false);
                    source.sendSuccess(() -> Component.literal("§e/econadmin reset §7- Restores standard Wagebox access permissions."), false);
                    source.sendSuccess(() -> Component.literal("§e/econadmin inspect §7- Silently opens the storage of the Wagebox you are looking at."), false);
                    source.sendSuccess(() -> Component.literal("§e/econadmin force_market_update §7- Bypasses the trade limit to instantly update the Global Market."), false);
                    return 1;
                })

                .then(Commands.literal("own_all").executes(context -> {
                    return setMode(context.getSource().getPlayerOrException(), PlayerWealth.AdminMode.OWN_ALL);
                }))

                .then(Commands.literal("own_none").executes(context -> {
                    return setMode(context.getSource().getPlayerOrException(), PlayerWealth.AdminMode.OWN_NONE);
                }))

                .then(Commands.literal("reset").executes(context -> {
                    return setMode(context.getSource().getPlayerOrException(), PlayerWealth.AdminMode.NORMAL);
                }))

                .then(Commands.literal("inspect").executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    HitResult hit = player.pick(5.0D, 0.0F, false);
                    if (hit.getType() == HitResult.Type.BLOCK) {
                        BlockEntity be = player.level().getBlockEntity(((BlockHitResult)hit).getBlockPos());
                        if (be instanceof WageboxBlockEntity wagebox) {
                            NetworkHooks.openScreen(player, new SimpleMenuProvider(
                                    (id, inv, p) -> new WageboxStorageMenu(id, inv, wagebox),
                                    Component.literal("§cAdmin Inspect: §f" + wagebox.getBlockPos().toShortString())
                            ), wagebox.getBlockPos());
                            return 1;
                        }
                    }
                    context.getSource().sendFailure(Component.literal("Not looking at a Wagebox."));
                    return 0;
                }))
                // Subcommand: Force Market Update
                .then(Commands.literal("force_market_update").executes(context -> {
                    com.femtendo.realecon.logic.market.GlobalMarketManager.get(context.getSource().getServer())
                            .checkAndRunEpoch(context.getSource().getServer(), true);
                    context.getSource().sendSuccess(() -> Component.literal("§aForced Market Epoch calculation!"), true);
                    return 1;
                }))
        );
    }

    // FIXED: Corrected parameter type to PlayerWealth.AdminMode
    private static int setMode(ServerPlayer player, PlayerWealth.AdminMode mode) {
        player.getCapability(PlayerWealthProvider.PLAYER_WEALTH).ifPresent(c -> c.setDebugMode(mode));
        player.sendSystemMessage(Component.literal("§dEcon Admin mode set to: §l" + mode.name()));
        return 1;
    }
}