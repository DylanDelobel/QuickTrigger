package com.quicktrigger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class QuickTriggerClient implements ClientModInitializer {

    private static final Gson GSON = new Gson();
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("quicktrigger");

    private static volatile String[] activeNames = new String[9];
    private static volatile int playerLimit = 1;
    private static volatile String[] lockMessages = new String[9];

    private static String resolveServerKey(net.minecraft.client.multiplayer.ClientPacketListener handler,
                                           net.minecraft.client.Minecraft client) {
        net.minecraft.client.multiplayer.ServerData data = handler.getServerData();
        if (data != null && data.ip != null && !data.ip.isBlank()) {
            LOGGER.info("[QuickTrigger] Server key: {}", data.ip);
            return data.ip;
        }
        try {
            if (client.getSingleplayerServer() != null) {
                String name = client.getSingleplayerServer().getWorldData().getLevelName();
                if (name != null && !name.isBlank()) {
                    LOGGER.info("[QuickTrigger] Solo key: local:{}", name);
                    return "local:" + name;
                }
            }
        } catch (Exception ignored) {}
        LOGGER.warn("[QuickTrigger] Could not resolve server key, using 'local'");
        return "local";
    }

    private static Component homeTooltip(int index) {
        String[] names = activeNames;
        if (names != null && index < names.length) {
            String custom = names[index];
            if (custom != null && !custom.isBlank()) return Component.literal(custom);
        }
        return Component.literal("Home #" + (index + 1));
    }

    @Override
    public void onInitializeClient() {
        QuickTriggerConfig.init(FabricLoader.getInstance()::getConfigDir);
        QuickTriggerConfig.INSTANCE.load();

        ClientPlayNetworking.registerGlobalReceiver(
            QuickTriggerPayloads.HomeLimitPayload.TYPE,
            (payload, ctx) -> {
                ClientState.serverHasMod = true;
                playerLimit = payload.playerLimit();
                ClientState.maxHomes = payload.maxHomes();
                List<String> msgs = GSON.fromJson(
                    payload.lockMessagesJson(),
                    new TypeToken<List<String>>() {}.getType()
                );
                String[] arr = new String[8];
                for (int i = 0; i < 8; i++) {
                    arr[i] = (msgs != null && i < msgs.size() && msgs.get(i) != null) ? msgs.get(i) : "";
                }
                lockMessages = arr;
            }
        );

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ClientState.currentServerKey = resolveServerKey(handler, client);
            activeNames = QuickTriggerConfig.INSTANCE.getNamesForServer(ClientState.currentServerKey);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ClientState.serverHasMod = false;
            playerLimit = 1;
            ClientState.maxHomes = 1;
            lockMessages = new String[8];
            ClientState.currentServerKey = null;
            activeNames = new String[9];
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof InventoryScreen)) return;

            int bgX = (scaledWidth - 176) / 2;
            int bgY = (scaledHeight - 166) / 2;

            Screens.getWidgets(screen).add(new ItemButton(
                bgX + 7, bgY - 20,
                new ItemStack(Items.COMPASS),
                Component.literal("Spawn"),
                () -> {
                    client.setScreen(null);
                    if (client.getConnection() != null)
                        client.getConnection().sendCommand("trigger spawn");
                }
            ));

            Screens.getWidgets(screen).add(new ItemButton(
                bgX + 26, bgY - 20,
                QuickTriggerConfig.INSTANCE.getItemStack(0),
                homeTooltip(0),
                () -> {
                    client.setScreen(null);
                    if (client.getConnection() != null)
                        client.getConnection().sendCommand("trigger home");
                }
            ));

            if (!ClientState.serverHasMod) return;

            int limit = playerLimit;
            int total = ClientState.maxHomes;
            String[] msgs = lockMessages;

            for (int i = 1; i < total; i++) {
                final int index = i;
                boolean available = index < limit;

                ItemStack icon = available
                    ? QuickTriggerConfig.INSTANCE.getItemStack(index)
                    : new ItemStack(Items.GRAY_BED);

                Component tooltip;
                if (available) {
                    tooltip = homeTooltip(index);
                } else {
                    int msgIdx = index - 1;
                    String msg = (msgIdx < msgs.length && msgs[msgIdx] != null && !msgs[msgIdx].isEmpty())
                        ? msgs[msgIdx] : null;
                    tooltip = msg != null ? Component.literal(msg) : null;
                }

                Runnable action = available ? () -> {
                    client.setScreen(null);
                    if (client.getConnection() != null)
                        client.getConnection().sendCommand("trigger home set " + (index + 1));
                } : null;

                Screens.getWidgets(screen).add(new ItemButton(
                    bgX + 26 + (index * 19), bgY - 20,
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
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
            int x = getX(), y = getY();
            graphics.fill(x, y, x + 18, y + 18, isHovered() ? 0x80555555 : 0x50252525);
            graphics.fill(x,      y,      x + 18, y + 1,  0x55FFFFFF);
            graphics.fill(x,      y + 17, x + 18, y + 18, 0x55FFFFFF);
            graphics.fill(x,      y,      x + 1,  y + 18, 0x55FFFFFF);
            graphics.fill(x + 17, y,      x + 18, y + 18, 0x55FFFFFF);
            graphics.item(icon, x + 1, y + 1);

            if (isHovered() && tooltip != null) {
                graphics.setTooltipForNextFrame(tooltip, mouseX, mouseY);
            }
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput output) {}
    }
}
