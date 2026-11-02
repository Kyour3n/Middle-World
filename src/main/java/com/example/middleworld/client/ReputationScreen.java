package com.example.middleworld.client;

import com.example.middleworld.ModItems;
import com.example.middleworld.reputation.ClientReputation;
import com.example.middleworld.reputation.Faction;
import com.example.middleworld.reputation.Rank;
import com.example.middleworld.reputation.ReputationData;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** The reputation screen opened from the tab under the inventory. */
public class ReputationScreen extends Screen {
    private static final int PW = 330;
    private static final int PH = 206;
    private static final int ROW_W = 128;
    private static final int ROW_H = 26;
    private static final int ROW_STEP = 28;

    private static final int GOLD = 0xFF6B5535;
    private static final int TEXT = 0xFFE8D5A8;
    private static final int MUTED = 0xFF9A8A6C;

    private int selected = 0;

    public ReputationScreen() {
        super(Component.translatable("gui.middleworld.reputation_title"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static ItemStack icon(Faction f) {
        return switch (f) {
            case ELVES -> new ItemStack(Items.BOW);
            case GOBLINS -> new ItemStack(ModItems.SPAWN_EGGS.get(0).get());
            case DWARVES -> new ItemStack(Items.IRON_PICKAXE);
            case VILLAGERS -> new ItemStack(Items.EMERALD);
            case PILLAGERS -> new ItemStack(Items.CROSSBOW);
            case PIGLINS -> new ItemStack(Items.GOLD_INGOT);
        };
    }

    private int left() {
        return (this.width - PW) / 2;
    }

    private int top() {
        return (this.height - PH) / 2;
    }

    private int rowY(int index) {
        return top() + 30 + index * ROW_STEP;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick); // dimmed / blurred world background

        int left = left();
        int top = top();
        ReputationData data = ClientReputation.get();

        // Frame, body and header
        g.fill(left - 3, top - 3, left + PW + 3, top + PH + 3, 0xFF0B0805);
        g.fill(left - 2, top - 2, left + PW + 2, top + PH + 2, GOLD);
        g.fill(left, top, left + PW, top + PH, 0xFF1C1712);
        g.fill(left, top, left + PW, top + 22, 0xFF2A2118);
        g.fill(left, top + 22, left + PW, top + 23, GOLD);
        g.drawCenteredString(this.font, this.title, left + PW / 2, top + 7, TEXT);
        g.fill(left + 140, top + 30, left + 141, top + PH - 8, 0xFF3A2F22);

        // Faction list
        Faction[] factions = Faction.values();
        for (int i = 0; i < factions.length; i++) {
            drawRow(g, factions[i], data, i, left + 8, rowY(i), mouseX, mouseY);
        }

        drawDetails(g, factions[selected], data, left + 148, top + 30, PW - 156);

        // Hint under the panel
        Component hint = Component.translatable("gui.middleworld.hint", this.minecraft.options.keyInventory.getTranslatedKeyMessage());
        g.drawCenteredString(this.font, hint, this.width / 2, top + PH + 8, MUTED);
    }

    private void drawRow(GuiGraphics g, Faction f, ReputationData data, int index, int x, int y, int mx, int my) {
        boolean hovered = mx >= x && mx < x + ROW_W && my >= y && my < y + ROW_H;
        int bg = index == selected ? 0xFF3A2F22 : hovered ? 0xFF2E251B : 0xFF241D15;
        g.fill(x, y, x + ROW_W, y + ROW_H, bg);
        if (index == selected) {
            g.fill(x, y, x + 2, y + ROW_H, 0xFF000000 | f.color);
        }

        g.renderItem(icon(f), x + 6, y + 2);
        g.drawString(this.font, f.displayName(), x + 28, y + 3, 0xFF000000 | f.color, false);

        int value = data.get(f);
        Rank rank = Rank.of(value);
        g.drawString(this.font, rank.displayName(), x + 28, y + 13, 0xFF000000 | rank.color, false);

        // Mini bar along the bottom edge of the row
        int bx = x + 4;
        int bw = ROW_W - 8;
        int by = y + ROW_H - 3;
        g.fill(bx, by, bx + bw, by + 2, 0xFF0E0B08);
        int center = bx + bw / 2;
        int end = center + Math.round(value / 1000.0F * (bw / 2.0F));
        g.fill(Math.min(center, end), by, Math.max(center, end), by + 2, value >= 0 ? 0xFF000000 | f.color : 0xFFC04040);
    }

    private void drawDetails(GuiGraphics g, Faction f, ReputationData data, int x, int y, int w) {
        int value = data.get(f);
        Rank rank = Rank.of(value);

        // Header: icon, name, rank
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(1.5F, 1.5F, 1.0F);
        g.renderItem(icon(f), 0, 0);
        g.pose().popPose();

        g.pose().pushPose();
        g.pose().translate(x + 30, y + 1, 0);
        g.pose().scale(1.4F, 1.4F, 1.0F);
        g.drawString(this.font, f.displayName(), 0, 0, 0xFF000000 | f.color, true);
        g.pose().popPose();
        g.drawString(this.font, rank.displayName(), x + 30, y + 16, 0xFF000000 | rank.color, true);

        String number = (value > 0 ? "+" : "") + value;
        g.drawString(this.font, number, x + w - this.font.width(number), y + 16, TEXT, true);

        // Reputation bar, centered on zero, with a tick at every rank boundary
        int barY = y + 36;
        int barH = 10;
        g.fill(x - 1, barY - 1, x + w + 1, barY + barH + 1, 0xFF000000);
        g.fill(x, barY, x + w, barY + barH, 0xFF15110D);
        int center = x + w / 2;
        float half = w / 2.0F;
        int end = center + Math.round(value / 1000.0F * half);
        if (value >= 0) {
            g.fill(center, barY, end, barY + barH, 0xFF000000 | f.color);
            g.fill(center, barY, end, barY + 2, 0x55FFFFFF);
        } else {
            g.fill(end, barY, center, barY + barH, 0xFFC04040);
            g.fill(end, barY, center, barY + 2, 0x55FFFFFF);
        }
        for (int threshold : Rank.THRESHOLDS) {
            int tx = center + Math.round(threshold / 1000.0F * half);
            g.fill(tx, barY - 2, tx + 1, barY + barH + 2, GOLD);
        }
        g.fill(center, barY - 3, center + 1, barY + barH + 3, 0xFFFFFFFF);

        // Progress to next rank
        int[] starts = {-399, -99, 100, 400, 700};
        Component progress;
        if (rank.ordinal() < starts.length) {
            Rank next = Rank.values()[rank.ordinal() + 1];
            progress = Component.translatable("gui.middleworld.next_rank", starts[rank.ordinal()] - value,
                    next.displayName());
        } else {
            progress = Component.translatable("gui.middleworld.max_rank");
        }
        g.drawString(this.font, progress, x, barY + barH + 5, MUTED, false);

        // Rivals
        List<Faction> rivals = f.rivals();
        Component rivalNames = Component.empty();
        for (int i = 0; i < rivals.size(); i++) {
            if (i > 0) {
                rivalNames = rivalNames.copy().append(", ");
            }
            rivalNames = rivalNames.copy().append(rivals.get(i).displayName());
        }
        g.drawString(this.font, Component.translatable("gui.middleworld.rivals", rivalNames), x, barY + barH + 16, MUTED, false);

        // Effects of the current rank
        int ey = barY + barH + 32;
        g.drawString(this.font, Component.translatable("gui.middleworld.effects"), x, ey, TEXT, false);
        g.fill(x, ey + 10, x + w, ey + 11, 0xFF3A2F22);
        g.drawWordWrap(this.font, Component.translatable("effect.middleworld." + f.id + "." + rank.key()),
                x, ey + 15, w, 0xFFC9B88E);

        // Recent history for this faction
        int hy = ey + 52;
        g.drawString(this.font, Component.translatable("gui.middleworld.history"), x, hy, TEXT, false);
        g.fill(x, hy + 10, x + w, hy + 11, 0xFF3A2F22);

        List<ReputationData.HistoryEntry> history = data.history();
        long now = this.minecraft.level != null ? this.minecraft.level.getGameTime() : 0L;
        int line = 0;
        for (int i = history.size() - 1; i >= 0 && line < 5; i--) {
            ReputationData.HistoryEntry e = history.get(i);
            if (e.faction() != f.ordinal()) {
                continue;
            }
            int ly = hy + 15 + line * 10;
            boolean gain = e.delta() > 0;
            String delta = (gain ? "+" : "") + e.delta();
            g.drawString(this.font, delta, x, ly, gain ? 0xFF7CD65A : 0xFFE05A5A, false);

            Component when = timeAgo(now - e.time());
            int whenW = this.font.width(when);
            String reason = this.font.plainSubstrByWidth(Component.translatable(e.reason()).getString(), w - 34 - whenW - 6);
            g.drawString(this.font, reason, x + 30, ly, 0xFFC9B88E, false);
            g.drawString(this.font, when, x + w - whenW, ly, MUTED, false);
            line++;
        }
        if (line == 0) {
            g.drawString(this.font, Component.translatable("gui.middleworld.no_history"), x, hy + 15, MUTED, false);
        }
    }

    private static Component timeAgo(long ticks) {
        long minutes = Math.max(0L, ticks) / 1200L;
        if (minutes < 1L) {
            return Component.translatable("gui.middleworld.ago_now");
        }
        if (minutes < 60L) {
            return Component.translatable("gui.middleworld.ago_min", minutes);
        }
        return Component.translatable("gui.middleworld.ago_hour", minutes / 60L);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int x = left() + 8;
            for (int i = 0; i < Faction.values().length; i++) {
                int y = rowY(i);
                if (mouseX >= x && mouseX < x + ROW_W && mouseY >= y && mouseY < y + ROW_H) {
                    selected = i;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Inventory key goes back to the inventory instead of closing everything.
        if (this.minecraft != null && this.minecraft.player != null
                && this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            this.minecraft.setScreen(new InventoryScreen(this.minecraft.player));
            return true;
        }
        // Up/down arrows move through the faction list.
        int count = Faction.values().length;
        if (keyCode == 264) { // down
            selected = (selected + 1) % count;
            return true;
        }
        if (keyCode == 265) { // up
            selected = (selected + count - 1) % count;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
