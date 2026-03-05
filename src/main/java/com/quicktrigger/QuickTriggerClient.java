package com.quicktrigger;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;


public class QuickTriggerClient implements ClientModInitializer {

    private static final ItemStack[] BED_ICONS = {
        new ItemStack(Items.BLUE_BED),
        new ItemStack(Items.GREEN_BED),
        new ItemStack(Items.ORANGE_BED),
        new ItemStack(Items.PURPLE_BED)
    };

    private static final Component[] HOME_TOOLTIPS = {
        Component.literal("Home #1"),
        Component.literal("Home #2"),
        Component.literal("Home #3"),
        Component.literal("Home #4")
    };

    private static final Component[] LOCK_TOOLTIPS = {
        null,
        Component.literal("Obtenez le rôle Mineur sur Discord"),
        Component.literal("Obtenez le rôle Architecte sur Discord"),
        Component.literal("Obtenez le rôle Dragon sur Discord")
    };

    private static volatile int homesLimit = 1;

    @Override
    public void onInitializeClient() {

        ClientPlayNetworking.registerGlobalReceiver(
            QuickTriggerPayloads.HomeLimitPayload.TYPE,
            (payload, ctx) -> homesLimit = Math.min(payload.limit(), 4)
        );

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> homesLimit = 1);

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof InventoryScreen)) return;

            int bgX = (scaledWidth - 176) / 2;
            int bgY = (scaledHeight - 166) / 2;

            // Bouton Spawn
            Screens.getButtons(screen).add(new ItemButton(
                bgX + 7, bgY - 20,
                new ItemStack(Items.COMPASS),
                Component.literal("Spawn"),
                () -> {
                    client.setScreen(null);
                    if (client.getConnection() != null)
                        client.getConnection().sendCommand("trigger spawn");
                }
            ));

            // 4 boutons Home — toujours affichés
            int limit = homesLimit;
            for (int i = 0; i < 4; i++) {
                final int index = i;
                boolean available = index < limit;

                ItemStack icon = available ? BED_ICONS[index] : new ItemStack(Items.GRAY_BED);
                Component tooltip = available ? HOME_TOOLTIPS[index] : LOCK_TOOLTIPS[index];
                Runnable action = available ? () -> {
                    client.setScreen(null);
                    if (client.getConnection() != null) {
                        String cmd = index == 0 ? "trigger home" : "trigger home set " + (index + 1);
                        client.getConnection().sendCommand(cmd);
                    }
                } : null;

                Screens.getButtons(screen).add(new ItemButton(
                    bgX + 25 + (index * 19), bgY - 20,
                    icon, tooltip, action
                ));
            }
        });
    }

    private static class ItemButton extends AbstractWidget {
        private final ItemStack icon;
        private final Component tooltip;
        private final Runnable action;

        ItemButton(int x, int y, ItemStack icon, Component tooltip, Runnable action) {
            super(x, y, 18, 18, Component.empty());
            this.icon = icon;
            this.tooltip = tooltip;
            this.action = action;
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
            if (action != null) action.run();
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            int x = getX(), y = getY();
            graphics.fill(x, y, x + 18, y + 18, isHovered() ? 0x80555555 : 0x50252525);
            graphics.fill(x,      y,      x + 18, y + 1,  0x55FFFFFF);
            graphics.fill(x,      y + 17, x + 18, y + 18, 0x55FFFFFF);
            graphics.fill(x,      y,      x + 1,  y + 18, 0x55FFFFFF);
            graphics.fill(x + 17, y,      x + 18, y + 18, 0x55FFFFFF);
            graphics.renderItem(icon, x + 1, y + 1);

            if (isHovered() && tooltip != null) {
                graphics.setTooltipForNextFrame(tooltip, mouseX, mouseY);
            }
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput output) {}
    }
}
