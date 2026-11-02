package com.example.middleworld.client;

import com.example.middleworld.MiddleWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

/** Adds the Reputation tab under the survival inventory. */
@EventBusSubscriber(modid = MiddleWorld.MODID, value = Dist.CLIENT)
public class ClientEvents {
    private static final int TAB_WIDTH = 96;
    private static final int TAB_HEIGHT = 20;
    private static final int INVENTORY_HEIGHT = 166;

    private static Button reputationTab;

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof InventoryScreen screen) {
            reputationTab = Button.builder(
                            Component.translatable("gui.middleworld.reputation_tab"),
                            button -> Minecraft.getInstance().setScreen(new ReputationScreen()))
                    .bounds(screen.getGuiLeft(), screen.getGuiTop() + INVENTORY_HEIGHT + 2, TAB_WIDTH, TAB_HEIGHT)
                    .build();
            event.addListener(reputationTab);
        }
    }

    /** Draws the tab on top of the inventory (harmless if the screen already drew it). */
    @SubscribeEvent
    public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        if (reputationTab != null && event.getScreen() instanceof InventoryScreen) {
            reputationTab.render(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTick());
        }
    }

    /** The recipe book shifts the inventory sideways, so keep the tab glued to it. */
    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Pre event) {
        if (reputationTab != null && event.getScreen() instanceof InventoryScreen screen) {
            reputationTab.setX(screen.getGuiLeft());
            reputationTab.setY(screen.getGuiTop() + INVENTORY_HEIGHT + 2);
        }
    }
}
