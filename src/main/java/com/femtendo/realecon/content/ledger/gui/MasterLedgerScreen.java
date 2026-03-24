package com.femtendo.realecon.content.ledger.gui;

import com.femtendo.realecon.client.ClientMarketData;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MasterLedgerScreen extends Screen {

    private static final ResourceLocation BOOK_TEXTURE = new ResourceLocation("realecon", "textures/gui/master_ledger.png");

    private final CompoundTag playerProfile;
    private final CompoundTag yellowPages;
    private int currentPage = 0;
    private final int MAX_PAGES = 2;
    private int scrollOffset = 0;

    private final int imageWidth = 256;
    private final int imageHeight = 175;

    public MasterLedgerScreen(CompoundTag playerProfile, CompoundTag yellowPages) {
        super(Component.translatable("item.realecon.master_ledger"));
        this.playerProfile = playerProfile;
        this.yellowPages = yellowPages;
    }

    public static void open(CompoundTag profile, CompoundTag yellowPages) {
        Minecraft.getInstance().setScreen(new MasterLedgerScreen(profile, yellowPages));
    }

    @Override
    protected void init() {
        super.init();
        int guiLeft = (this.width - imageWidth) / 2;
        int guiTop = (this.height - imageHeight) / 2;

        this.addRenderableWidget(Button.builder(Component.literal("<"), button -> {
            if (currentPage > 0) {
                currentPage--;
                scrollOffset = 0;
            }
        }).bounds(guiLeft + 15, guiTop + 145, 20, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal(">"), button -> {
            if (currentPage < MAX_PAGES) {
                currentPage++;
                scrollOffset = 0;
            }
        }).bounds(guiLeft + 220, guiTop + 145, 20, 20).build());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta > 0 && scrollOffset > 0) {
            scrollOffset--;
        } else if (delta < 0) {
            scrollOffset++;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int guiLeft = (this.width - imageWidth) / 2;
        int guiTop = (this.height - imageHeight) / 2;

        graphics.blit(BOOK_TEXTURE, guiLeft, guiTop, 0, 0, imageWidth, imageHeight, 256, 256);

        int leftX = guiLeft + 20;
        int rightX = guiLeft + 140;

        String title = switch (currentPage) {
            case 0 -> "§0§lMy Empire Stats";
            case 1 -> "§0§lGlobal Shops";
            case 2 -> "§0§lGlobal Market Index";
            default -> "Ledger";
        };
        graphics.drawString(this.font, title, leftX, guiTop + 15, 0, false);
        graphics.drawString(this.font, (currentPage + 1) + "/" + (MAX_PAGES + 1), rightX + 60, guiTop + 150, 0x666666, false);

        if (currentPage == 0) renderPage1(graphics, leftX, rightX, guiTop);
        else if (currentPage == 1) renderPage2(graphics, leftX, rightX, guiTop);
        else if (currentPage == 2) renderPage3(graphics, leftX, rightX, guiTop);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderPage1(GuiGraphics graphics, int leftX, int rightX, int y) {
        UUID myUUID = Minecraft.getInstance().player.getUUID();
        List<StackedTrade> myTrades = new ArrayList<>();

        for (String key : yellowPages.getAllKeys()) {
            CompoundTag node = yellowPages.getCompound(key);
            if (node.hasUUID("OwnerId") && node.getUUID("OwnerId").equals(myUUID)) {
                ListTag trades = node.getList("Trades", Tag.TAG_COMPOUND);
                myTrades.addAll(getGroupedTrades(trades));
            }
        }

        long netWorth = playerProfile.getLong("NetWorth");
        double stockValue = playerProfile.getDouble("ShopStockValue");

        double netGain = 0;
        ListTag history = playerProfile.getList("History", Tag.TAG_COMPOUND);
        for(int i = 0; i < history.size(); i++) {
            ItemStack bounty = ItemStack.of(history.getCompound(i).getCompound("Bounty"));
            String bKey = ForgeRegistries.ITEMS.getKey(bounty.getItem()).toString();
            netGain += ClientMarketData.PRICES.getOrDefault(bKey, 0.0) * bounty.getCount();
        }

        // Cleaned up Financial Dash
        graphics.drawString(this.font, "§8Net Worth:", leftX, y + 40, 0, false);
        graphics.drawString(this.font, "§2$" + netWorth, leftX, y + 50, 0, false);

        graphics.drawString(this.font, "§8Trade Revenue:", leftX, y + 75, 0, false);
        graphics.drawString(this.font, String.format("§2$%.2f", netGain), leftX, y + 85, 0, false);

        graphics.drawString(this.font, "§8Shop Stock Value:", leftX, y + 110, 0, false);
        graphics.drawString(this.font, String.format("§6$%.2f", stockValue), leftX, y + 120, 0, false);

        // Open Orders List
        graphics.drawString(this.font, "§0Your Open Orders:", rightX, y + 15, 0, false);

        int maxScroll = Math.max(0, myTrades.size() - 6);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;

        int drawY = y + 30;
        for (int i = 0; i < 6; i++) {
            int index = scrollOffset + i;
            if (index >= myTrades.size()) break;

            StackedTrade trade = myTrades.get(index);
            graphics.renderItem(trade.bounty, rightX, drawY);
            graphics.renderItemDecorations(this.font, trade.bounty, rightX, drawY);
            graphics.drawString(this.font, "->", rightX + 20, drawY + 4, 0x444444, false);
            graphics.renderItem(trade.reward, rightX + 35, drawY);
            graphics.renderItemDecorations(this.font, trade.reward, rightX + 35, drawY);

            if (trade.count > 1) {
                graphics.drawString(this.font, "x" + trade.count, rightX + 55, drawY + 4, 0x005500, false);
            }
            drawY += 20;
        }

        if (myTrades.isEmpty()) graphics.drawString(this.font, "§8No boxes listed.", rightX, y + 35, 0, false);
    }

    private void renderPage2(GuiGraphics graphics, int leftX, int rightX, int y) {
        UUID myUUID = Minecraft.getInstance().player.getUUID();
        List<CompoundTag> otherNodes = new ArrayList<>();
        for (String key : yellowPages.getAllKeys()) {
            CompoundTag node = yellowPages.getCompound(key);
            if (node.hasUUID("OwnerId") && !node.getUUID("OwnerId").equals(myUUID)) otherNodes.add(node);
        }
        otherNodes.sort((a, b) -> a.getString("OwnerName").compareToIgnoreCase(b.getString("OwnerName")));

        int maxScroll = Math.max(0, otherNodes.size() - 4);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;

        for (int i = 0; i < 4; i++) {
            int index = scrollOffset + i;
            if (index >= otherNodes.size()) break;

            CompoundTag node = otherNodes.get(index);
            int curX = (i % 2 == 0) ? leftX : rightX;
            int curY = y + 35 + ((i / 2) * 50);

            net.minecraft.core.BlockPos pos = net.minecraft.core.BlockPos.of(node.getLong("Pos"));
            graphics.drawString(this.font, "§6" + node.getString("OwnerName"), curX, curY, 0, false);
            graphics.drawString(this.font, "§8" + pos.getX() + " " + pos.getY() + " " + pos.getZ(), curX, curY + 10, 0, false);

            ListTag trades = node.getList("Trades", Tag.TAG_COMPOUND);
            List<StackedTrade> grouped = getGroupedTrades(trades);

            if (!grouped.isEmpty()) {
                StackedTrade topTrade = grouped.get(0);
                graphics.renderItem(topTrade.bounty, curX, curY + 20);
                graphics.renderItem(topTrade.reward, curX + 18, curY + 20);
                if (topTrade.count > 1) {
                    graphics.drawString(this.font, "x" + topTrade.count, curX + 38, curY + 24, 0x005500, false);
                }
                if (grouped.size() > 1) {
                    graphics.drawString(this.font, "§8+ " + (grouped.size() - 1) + " more", curX, curY + 38, 0, false);
                }
            } else {
                graphics.drawString(this.font, "§cSold Out", curX, curY + 22, 0, false);
            }
        }
        if (otherNodes.isEmpty()) graphics.drawString(this.font, "§8Market is empty.", leftX, y + 40, 0, false);
    }

    private void renderPage3(GuiGraphics graphics, int leftX, int rightX, int y) {
        List<Map.Entry<String, Double>> sortedPrices = new ArrayList<>(ClientMarketData.PRICES.entrySet());
        sortedPrices.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        int maxScroll = Math.max(0, sortedPrices.size() - 10);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;

        for (int i = 0; i < 10; i++) {
            int index = scrollOffset + i;
            if (index >= sortedPrices.size()) break;

            Map.Entry<String, Double> entry = sortedPrices.get(index);
            int curX = (i < 5) ? leftX : rightX;
            int curY = y + 35 + ((i % 5) * 22);

            ResourceLocation itemLoc = new ResourceLocation(entry.getKey());
            ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(itemLoc));

            graphics.renderItem(stack, curX, curY);

            String name = stack.getHoverName().getString();
            if (name.length() > 10) name = name.substring(0, 9) + ".";
            graphics.drawString(this.font, "§0" + name, curX + 20, curY, 0, false);

            String priceStr = String.format("§2$%.2f", entry.getValue());
            graphics.drawString(this.font, priceStr, curX + 20, curY + 10, 0, false);
        }

        if (sortedPrices.isEmpty()) {
            graphics.drawString(this.font, "§8No market data available.", leftX, y + 40, 0, false);
        }

        int tradesSince = ClientMarketData.tradesSinceUpdate;
        int maxTrades = ClientMarketData.tradesUntilUpdate;
        String timerText = "Next Report: " + tradesSince + " / " + maxTrades + " trades";

        int centerX = this.width / 2;
        int drawX = centerX - (this.font.width(timerText) / 2);

        graphics.drawString(this.font, "§8" + timerText, drawX, y + 150, 0, false);
    }

    @Override
    public boolean isPauseScreen() { return false; }

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
                    merged = true; break;
                }
            }
            if (!merged) grouped.add(new StackedTrade(bounty, reward, 1));
        }
        return grouped;
    }

    private static class StackedTrade {
        public final ItemStack bounty; public final ItemStack reward; public int count;
        public StackedTrade(ItemStack b, ItemStack r, int c) { this.bounty = b; this.reward = r; this.count = c; }
    }
}