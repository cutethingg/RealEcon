package com.femtendo.realecon.content.wagebox;

import com.femtendo.realecon.capability.PlayerWealth;
import com.femtendo.realecon.capability.PlayerWealthProvider;
import com.femtendo.realecon.content.ledger.TradeLedgerItem;
import com.femtendo.realecon.init.ModBlockEntities;
import com.femtendo.realecon.logic.EmpireManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class WageboxBlock extends BaseEntityBlock {

    public WageboxBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        if (!level.isClientSide() && placer instanceof Player player) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof WageboxBlockEntity wagebox) {
                wagebox.setOwner(player.getUUID());
            }
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @NotNull
    @Override
    public InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (player.isSecondaryUseActive() && !player.getItemInHand(hand).isEmpty()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);

            if (be instanceof WageboxBlockEntity wagebox) {
                // 1. GET ADMIN STATUS
                PlayerWealth.AdminMode mode = serverPlayer.getCapability(PlayerWealthProvider.PLAYER_WEALTH)
                        .map(PlayerWealth::getDebugMode).orElse(PlayerWealth.AdminMode.NORMAL);

                boolean isOwner = serverPlayer.getUUID().equals(wagebox.getOwnerId());

                // 2. THE OVERRIDE LOGIC
                boolean canAccessAsOwner = (mode == PlayerWealth.AdminMode.OWN_ALL) || (mode == PlayerWealth.AdminMode.NORMAL && isOwner);
                if (mode == PlayerWealth.AdminMode.OWN_NONE) canAccessAsOwner = false;

                // 3. EXECUTE INTERACTION
                if (canAccessAsOwner) {
                    ItemStack handItem = serverPlayer.getItemInHand(hand);

                    // A. OWNER PROGRAMMING
                    if (handItem.getItem() instanceof TradeLedgerItem) {
                        net.minecraft.nbt.CompoundTag tag = handItem.getTag();
                        if (tag != null && tag.contains("SavedTrades")) {
                            net.minecraft.nbt.ListTag tradesList = tag.getList("SavedTrades", net.minecraft.nbt.Tag.TAG_COMPOUND);
                            wagebox.getTrades().clear();
                            for(int i = 0; i < tradesList.size(); i++) {
                                wagebox.addTrade(WageTrade.deserializeNBT(tradesList.getCompound(i)));
                            }
                            EmpireManager.get((ServerLevel) level).registerWagebox(pos, serverPlayer.getUUID(), serverPlayer.getName().getString(), wagebox.getTrades());
                            serverPlayer.sendSystemMessage(Component.literal("§aTrades Uploaded!"));
                        }
                    }
                    // B. OWNER STORAGE
                    else {
                        NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                                (id, inv, p) -> new WageboxStorageMenu(id, inv, wagebox),
                                Component.literal("Wagebox Inventory")
                        ), pos);
                    }
                }
                // C. PASSERBY TRADING
                else {
                    WageboxMerchant merchant = new WageboxMerchant(wagebox, serverPlayer);
                    NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                            (id, inv, p) -> new WageboxMenu(id, inv, merchant, wagebox),
                            Component.literal("Trading Post")
                    ), pos);

                    serverPlayer.sendMerchantOffers(serverPlayer.containerMenu.containerId, merchant.getOffers(), 1, 0, false, false);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new WageboxBlockEntity(ModBlockEntities.WAGEBOX_BE.get(), pos, state);
    }

    @NotNull
    @Override
    public RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide() && level instanceof ServerLevel serverLevel) {
            EmpireManager.get(serverLevel).unregisterWagebox(pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}