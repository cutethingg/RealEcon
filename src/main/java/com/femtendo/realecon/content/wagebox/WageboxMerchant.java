package com.femtendo.realecon.content.wagebox;

import com.femtendo.realecon.logic.market.GlobalMarketManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jetbrains.annotations.Nullable;

public class WageboxMerchant implements Merchant {
    private final WageboxBlockEntity wagebox;
    private Player tradingPlayer;
    private MerchantOffers offers;

    public WageboxMerchant(WageboxBlockEntity wagebox, Player player) {
        this.wagebox = wagebox;
        this.tradingPlayer = player;

        // Register the player to receive live UI updates
        if (player != null) {
            wagebox.registerMerchant(this);
        }
    }

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        this.tradingPlayer = player;
        // Unregister the player when they close the GUI
        if (player == null) {
            wagebox.unregisterMerchant(this);
        }
    }

    @Nullable
    @Override
    public Player getTradingPlayer() {
        return this.tradingPlayer;
    }

    @Override
    public MerchantOffers getOffers() {
        if (this.offers == null) {
            this.offers = new MerchantOffers();
            for (WageTrade trade : wagebox.getTrades()) {
                int available = wagebox.calculateAvailableUses(trade);
                MerchantOffer offer = new MerchantOffer(
                        trade.getBounty(),
                        ItemStack.EMPTY,
                        trade.getReward(),
                        0, available, 0, 0, 0);

                this.offers.add(offer);
            }
        }
        return this.offers;
    }

    // --- NEW: THE LIVE REFRESH TRIGGER ---
    public void refreshOffers() {
        this.offers = null; // Destroy the old cache
        getOffers(); // Force a live recalculation of the physical inventory

        if (this.tradingPlayer instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            // Ensure the player is still safely looking at the menu before beaming the packet
            if (serverPlayer.containerMenu instanceof net.minecraft.world.inventory.MerchantMenu) {
                serverPlayer.sendMerchantOffers(
                        serverPlayer.containerMenu.containerId,
                        this.offers,
                        0, 0, false, false
                );
            }
        }
    }
    // -------------------------------------

    @Override
    public void overrideOffers(MerchantOffers offers) {}

    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
        wagebox.processTrade(offer, this.tradingPlayer);

        if (this.tradingPlayer != null && !this.tradingPlayer.level().isClientSide()) {
            MinecraftServer server = this.tradingPlayer.getServer();
            if (server != null) {
                GlobalMarketManager.get(server).recordTrade(offer.getBaseCostA(), offer.getResult());
                GlobalMarketManager.get(server).checkAndRunEpoch(server, false);
            }
        }
    }

    @Override
    public void notifyTradeUpdated(ItemStack stack) {}

    @Override
    public int getVillagerXp() { return 0; }

    @Override
    public void overrideXp(int xp) {}

    @Override
    public boolean showProgressBar() { return false; }

    @Nullable
    @Override
    public SoundEvent getNotifyTradeSound() {
        return null;
    }

    @Override
    public boolean isClientSide() { return false; }
}