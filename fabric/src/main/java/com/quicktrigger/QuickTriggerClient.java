package com.quicktrigger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quicktrigger.QuickTriggerPayloads.HomeLimitPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;


import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;

import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;


import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;


import org.slf4j.LoggerFactory;

import java.util.List;

public class QuickTriggerClient implements ClientModInitializer {

    private static final Gson GSON=new Gson();
    private static final Logger LOGGER = LoggerFactory.getLogger("quicktrigger");

    private static volatile String[]activeNames =new String[9];
    private static volatile int playerLimit= 1;
    private static volatile String[] lockMessages = new String[9];


    // ip for multiplayer, "local:<world>" in solo so each save gets its own names
    private static String resolveServerKey(ClientPacketListener handler, Minecraft client) {
        //handler.getServerData, NOT client.getCurrentServer, that one is still null at JOIN
        ServerData data = handler.getServerData();
        if (data != null && data.ip != null && !data.ip.isBlank()) {
            LOGGER.info("[QuickTrigger] Server key: {}",data.ip);
            return data.ip;
        }
        try {
            IntegratedServer sp = client.getSingleplayerServer();
            if (sp != null) {
                String name=sp.getWorldData().getLevelName();
                if(name != null && !name.isBlank()){
                    LOGGER.info("[QuickTrigger] Solo key: local:{}",name);
                    return "local:" + name;
                }
            }
        }catch(Exception ignored){}//shouldnt happen, but not worth breaking the join over, fall thru
        LOGGER.warn("[QuickTrigger] Could not resolve server key, using 'local'");
        return "local";
    }

    private static Component homeTooltip(int slot) {


        String[] names = activeNames;
        if(names != null && slot < names.length) {
            String custom = names[slot];
            if(custom != null && !custom.isBlank()) return Component.literal(custom);
        }
        return Component.literal("Home #" + (slot + 1));
    }

    // close the inventory then fire the trigger
    private static Runnable command(Minecraft client,String cmd){
        return()->{
            client.setScreenAndShow(null);
            if (client.getConnection() != null)
                client.getConnection().sendCommand(cmd);
        };
    }

    @Override
    public void onInitializeClient(){


        QuickTriggerConfig.init(FabricLoader.getInstance()::getConfigDir);
        QuickTriggerConfig.INSTANCE.load();

        ClientPlayNetworking.registerGlobalReceiver(HomeLimitPayload.TYPE, (payload, ctx) -> {
            ClientState.serverHasMod =true;
            playerLimit=payload.playerLimit();
            ClientState.maxHomes =payload.maxHomes();
            //LOGGER.info("limit {} / max {}", playerLimit, ClientState.maxHomes);

            List<String> msgs = GSON.fromJson(payload.lockMessagesJson(), new TypeToken<List<String>>() {}.getType());
            String[] parsed=new String[8];
            for(int i = 0; i < 8;i++){
                parsed[i] = (msgs != null && i < msgs.size() && msgs.get(i) != null) ? msgs.get(i) : "";
            }
            lockMessages =parsed;
        });

        ClientPlayConnectionEvents.JOIN.register((handler,sender, client)-> {
            ClientState.currentServerKey = resolveServerKey(handler,client);
            activeNames = QuickTriggerConfig.INSTANCE.getNamesForServer(ClientState.currentServerKey);
        });

        //reset everything, next server might not have the mod
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ClientState.serverHasMod=false;

            playerLimit=1;
            ClientState.maxHomes = 1;
            lockMessages= new String[8];
            ClientState.currentServerKey = null;
            activeNames=new String[9];
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof InventoryScreen)) return;


            // 176x166 = vanilla inventory texture, buttons go in a row just above it
            int bgX =(scaledWidth - 176)/ 2;
            int bgY = (scaledHeight - 166) / 2;

            Screens.getWidgets(screen).add(new ItemButton(bgX + 7, bgY - 20,
                new ItemStack(Items.COMPASS), Component.literal("Spawn"), command(client, "trigger spawn")));

            //home 1 works everywhere, even without the mod on the server
            Screens.getWidgets(screen).add(new ItemButton(bgX + 26,bgY - 20,
                QuickTriggerConfig.INSTANCE.getItemStack(0), homeTooltip(0),command(client, "trigger home")));

            if(!ClientState.serverHasMod)return;

            int limit = playerLimit;
            int total = ClientState.maxHomes;
            String[]msgs = lockMessages;

            for (int i=1;i < total; i++){
                boolean unlocked=i < limit;

                ItemStack icon = unlocked
                    ? QuickTriggerConfig.INSTANCE.getItemStack(i)
                    : new ItemStack(Items.BED.pick(DyeColor.GRAY));



                Component tooltip;
                if (unlocked){
                    tooltip = homeTooltip(i);
                }else{
                    // lockMessages[0] is slot #2
                    int m =i - 1;
                    String msg = (m < msgs.length && msgs[m] != null && !msgs[m].isEmpty()) ? msgs[m] : null;
                    tooltip = msg != null ? Component.literal(msg) : null;
                }

                Runnable action = unlocked ? command(client, "trigger home set " + (i + 1)) : null;

                Screens.getWidgets(screen).add(new ItemButton(bgX + 26 + (i * 19), bgY - 20, icon, tooltip, action));


            }

        });
    }

    private static class ItemButton extends AbstractWidget {
        private final ItemStack icon;
        private final Component tooltip;


        private final Runnable action; // null = locked slot, click does nothing

        ItemButton(int x, int y,ItemStack icon, Component tooltip, Runnable action){
            super(x,y, 18, 18, Component.empty());
            this.icon = icon;
            this.tooltip =tooltip;
            this.action = action;
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean isDoubleClick) {

            if (action != null) action.run();
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX,int mouseY,float delta){
            int x = getX(), y = getY();
            graphics.fill(x,y, x + 18, y + 18, isHovered()? 0x80555555 : 0x50252525);
            //1px border, looks like a vanilla slot
            graphics.fill(x,y,x + 18,y + 1,0x55FFFFFF);
            graphics.fill(x,y + 17,x + 18,y + 18, 0x55FFFFFF);
            graphics.fill(x,      y,      x + 1,  y + 18, 0x55FFFFFF);
            graphics.fill(x + 17, y,      x + 18, y + 18, 0x55FFFFFF);


            graphics.item(icon, x + 1, y + 1);

            if (isHovered()&& tooltip != null){
                graphics.setTooltipForNextFrame(tooltip, mouseX, mouseY);
            }
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput output){}
    }
}
