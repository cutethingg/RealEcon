package com.femtendo.realecon.content.ledger.gui;

import com.femtendo.realecon.network.PacketHandler;
import com.femtendo.realecon.network.RemoveTradePacket;
import com.femtendo.realecon.network.SaveLedgerTradePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TradeLedgerScreen extends AbstractContainerScreen<TradeLedgerMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("realecon", "textures/gui/ledger.png");

    private int scrollOffset = 0;
    private static final int VISIBLE_ENTRIES = 6;

    public TradeLedgerScreen(TradeLedgerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 256;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        // FIXED: Bumped the button to the right of the fire icon (approx X=118)
        this.addRenderableWidget(Button.builder(Component.literal("+ Add"), (btn) -> {
            ItemStack bounty = menu.inputSlots.getStackInSlot(0);
            ItemStack reward = menu.inputSlots.getStackInSlot(1);
            if (!bounty.isEmpty() && !reward.isEmpty()) {
                PacketHandler.sendToServer(new SaveLedgerTradePacket(bounty, reward));
            }
        }).pos(leftPos + 118, topPos + 50).size(45, 20).build());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderActiveTrades(graphics, mouseX, mouseY);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        ItemStack ledger = menu.getLedgerStack();
        if (!ledger.isEmpty() && ledger.hasTag() && ledger.getTag().contains("SavedTrades")) {
            ListTag rawTrades = ledger.getTag().getList("SavedTrades", Tag.TAG_COMPOUND);
            List<StackedTrade> groupedTrades = getGroupedTrades(rawTrades);

            int maxScroll = Math.max(0, groupedTrades.size() - VISIBLE_ENTRIES);

            if (delta > 0 && scrollOffset > 0) {
                scrollOffset--;
            } else if (delta < 0 && scrollOffset < maxScroll) {
                scrollOffset++;
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void renderActiveTrades(GuiGraphics graphics, int mouseX, int mouseY) {
        ItemStack ledger = menu.getLedgerStack();
        if (ledger.isEmpty() || !ledger.hasTag()) return;

        ListTag rawTrades = ledger.getTag().getList("SavedTrades", Tag.TAG_COMPOUND);
        List<StackedTrade> groupedTrades = getGroupedTrades(rawTrades);

        // FIXED: Pushed the entire list to X=172 so it completely clears the player inventory slots
        int listX = leftPos + 172;
        int listY = topPos + 15;

        graphics.drawString(this.font, "§nActive Trades", listX, listY - 10, 0x444444, false);

        for (int i = 0; i < VISIBLE_ENTRIES; i++) {
            int tradeIndex = scrollOffset + i;
            if (tradeIndex >= groupedTrades.size()) break;

            StackedTrade trade = groupedTrades.get(tradeIndex);
            int entryY = listY + (i * 22);

            graphics.renderItem(trade.bounty, listX, entryY);
            graphics.renderItemDecorations(this.font, trade.bounty, listX, entryY);

            // Scaled the spacing down slightly so everything fits snugly between X=172 and X=256
            graphics.drawString(this.font, "->", listX + 18, entryY + 4, 0x666666, false);
            graphics.renderItem(trade.reward, listX + 32, entryY);
            graphics.renderItemDecorations(this.font, trade.reward, listX + 32, entryY);

            if (trade.count > 1) {
                graphics.drawString(this.font, "x" + trade.count, listX + 50, entryY + 4, 0x006600, false);
            }

            int xButtonX = listX + 65;
            int xButtonY = entryY + 4;
            boolean isHovering = mouseX >= xButtonX && mouseX <= xButtonX + 10 && mouseY >= xButtonY && mouseY <= xButtonY + 10;
            graphics.drawString(this.font, isHovering ? "§c§lX" : "§4X", xButtonX, xButtonY, 0xFFFFFF);
        }

        if (groupedTrades.size() > VISIBLE_ENTRIES) {
            int maxPages = Math.max(1, groupedTrades.size() - VISIBLE_ENTRIES + 1);
            graphics.drawString(this.font, (scrollOffset + 1) + "/" + maxPages, listX + 30, listY + (VISIBLE_ENTRIES * 22), 0x888888, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        ItemStack ledger = menu.getLedgerStack();
        if (!ledger.isEmpty() && ledger.hasTag()) {
            ListTag rawTrades = ledger.getTag().getList("SavedTrades", Tag.TAG_COMPOUND);
            List<StackedTrade> groupedTrades = getGroupedTrades(rawTrades);

            // Must match the listX updated above!
            int listX = leftPos + 172;
            int listY = topPos + 15;

            for (int i = 0; i < VISIBLE_ENTRIES; i++) {
                int tradeIndex = scrollOffset + i;
                if (tradeIndex >= groupedTrades.size()) break;

                StackedTrade trade = groupedTrades.get(tradeIndex);
                int xButtonX = listX + 65;
                int xButtonY = listY + (i * 22) + 4;

                if (mouseX >= xButtonX && mouseX <= xButtonX + 12 && mouseY >= xButtonY && mouseY <= xButtonY + 10) {
                    PacketHandler.sendToServer(new RemoveTradePacket(trade.firstRawIndex));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private List<StackedTrade> getGroupedTrades(ListTag rawTrades) {
        List<StackedTrade> grouped = new ArrayList<>();
        for (int i = 0; i < rawTrades.size(); i++) {
            CompoundTag tag = rawTrades.getCompound(i);
            ItemStack bounty = ItemStack.of(tag.getCompound("Bounty"));
            ItemStack reward = ItemStack.of(tag.getCompound("Reward"));

            boolean merged = false;
            for (StackedTrade st : grouped) {
                if (ItemStack.isSameItemSameTags(st.bounty, bounty) && st.bounty.getCount() == bounty.getCount() &&
                        ItemStack.isSameItemSameTags(st.reward, reward) && st.reward.getCount() == reward.getCount()) {
                    st.count++;
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                grouped.add(new StackedTrade(bounty, reward, 1, i));
            }
        }
        return grouped;
    }

    private static class StackedTrade {
        public final ItemStack bounty;
        public final ItemStack reward;
        public int count;
        public final int firstRawIndex;

        public StackedTrade(ItemStack bounty, ItemStack reward, int count, int firstRawIndex) {
            this.bounty = bounty;
            this.reward = reward;
            this.count = count;
            this.firstRawIndex = firstRawIndex;
        }
    }
}