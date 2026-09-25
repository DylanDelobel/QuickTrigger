package com.quicktrigger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quicktrigger.QuickTriggerPayloads.HomeLimitPayload;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;



import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;

import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;


import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;



@OnlyIn(Dist.CLIENT)
public class ClientEventHandler {

    private static final Gson GSON=new Gson();
    private static final Logger LOGGER= LoggerFactory.getLogger("quicktrigger");

    static volatile String[] activeNames = new String[9];
    static volatile int playerLimit=1;
    static volatile String[] lockMessages= new String[9];

    static void handlePayload(HomeLimitPayload payload) {
        ClientState.serverHasMod = true;

        playerLimit = payload.playerLimit();
        ClientState.maxHomes = payload.maxHomes();
        // LOGGER.info("limit {} / max {}", playerLimit, ClientState.maxHomes);

        List<String> msgs =GSON.fromJson(payload.lockMessagesJson(),new TypeToken<List<String>>(){}.getType());
        String[] parsed = new String[8];
        for (int i = 0; i < 8; i++) {
            parsed[i]=(msgs != null && i < msgs.size()&& msgs.get(i)!= null)? msgs.get(i): "";
        }
        lockMessages= parsed;
    }

    @SubscribeEvent
    public void onLogIn(ClientPlayerNetworkEvent.LoggingIn event){
        ClientState.currentServerKey =resolveServerKey(Minecraft.getInstance());
        activeNames=QuickTriggerConfig.INSTANCE.getNamesForServer(ClientState.currentServerKey);
    }

    //ip for multiplayer, "local:<world>" in solo. fabric version also logs a warn on fallback, this one doesnt
    private static String resolveServerKey(Minecraft client) {
        ServerData data = client.getCurrentServer();
        if(data != null && data.ip != null && !data.ip.isBlank()){
            LOGGER.info("[QuickTrigger] Server key: {}", data.ip);
            return data.ip;
        }
        try{


            IntegratedServer sp = client.getSingleplayerServer();
            if (sp == null)return "local";
            String name=sp.getWorldData().getLevelName();
            if (name == null || name.isBlank()) return "local";
            LOGGER.info("[QuickTrigger] Solo key: local:{}", name);
            return "local:" + name;
        } catch (Exception e) {
            return "local";
        }
    }

    //wipe per-server state so the next server starts clean
    @SubscribeEvent
    public void onLogOut(ClientPlayerNetworkEvent.LoggingOut event){
        ClientState.serverHasMod =false;
        playerLimit=1;
        ClientState.maxHomes = 1;


        lockMessages = new String[8];
        ClientState.currentServerKey =null;
        activeNames = new String[9];
    }

    @SubscribeEvent
    public void onScreenInit(ScreenEvent.Init.Post event){
        if (!(event.getScreen()instanceof InventoryScreen))return;

        Minecraft client =Minecraft.getInstance();
        int scaledWidth  = client.getWindow().getGuiScaledWidth();
        int scaledHeight = client.getWindow().getGuiScaledHeight();

        // 176x166 is the vanilla inventory bg
        int bgX = (scaledWidth - 176) / 2;
        int bgY=(scaledHeight - 166)/ 2;

        event.addListener(new ItemButton(bgX + 7,bgY - 20,
            new ItemStack(Items.COMPASS),Component.literal("Spawn"), command(client, "trigger spawn")));

        event.addListener(new ItemButton(bgX + 26, bgY - 20,
            QuickTriggerConfig.INSTANCE.getItemStack(0),homeTooltip(0), command(client,"trigger home")));

        if(!ClientState.serverHasMod) return; // vanilla server, just spawn + home 1

        int limit = playerLimit;
        int total = ClientState.maxHomes;
        String[] msgs = lockMessages;

        for (int i =1;i < total; i++){
            boolean unlocked = i < limit;


            ItemStack icon =unlocked
                ? QuickTriggerConfig.INSTANCE.getItemStack(i)
                : new ItemStack(Items.BED.pick(DyeColor.GRAY)); // grey bed = locked

            Component tooltip;
            if(unlocked){
                tooltip = homeTooltip(i);
            } else {
                int m= i - 1; // msgs start at slot #2
                String msg =(m < msgs.length && msgs[m]!= null && !msgs[m].isEmpty())? msgs[m] : null;
                tooltip=msg != null ? Component.literal(msg): null;

            }

            Runnable action = unlocked ? command(client, "trigger home set " +(i + 1)) : null;

            event.addListener(new ItemButton(bgX + 26 + (i * 19),bgY - 20,icon, tooltip,action));
        }
    }

    private static Runnable command(Minecraft client, String cmd){
        return () ->{
            client.setScreenAndShow(null);
            if(client.getConnection() != null)
                client.getConnection().sendCommand(cmd);
        };
    }



    private static Component homeTooltip(int slot) {
        String[] names = activeNames;
        if (names != null && slot < names.length) {
            String custom = names[slot];
            if (custom != null && !custom.isBlank()) return Component.literal(custom);
        }
        return Component.literal("Home #" + (slot + 1));
    }

    //copy of the fabric one, didnt want a common widget class just for this
    private static class ItemButton extends AbstractWidget {
        private final ItemStack icon;


        private final Component tooltip;

        private final Runnable action;

        ItemButton(int x, int y, ItemStack icon,Component tooltip, Runnable action){
            super(x, y, 18, 18, Component.empty());
            this.icon=icon;
            this.tooltip = tooltip;


            this.action = action;
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
            if(action != null) action.run();
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
            int x = getX(),y=getY();
            graphics.fill(x,y,x + 18,y + 18,isHovered()? 0x80555555 : 0x50252525);

            graphics.fill(x, y, x + 18,y + 1,0x55FFFFFF); //top
            graphics.fill(x,      y + 17, x + 18, y + 18, 0x55FFFFFF); //bottom
            graphics.fill(x,      y,      x + 1,  y + 18, 0x55FFFFFF);
            graphics.fill(x + 17, y,      x + 18, y + 18, 0x55FFFFFF);
            graphics.item(icon, x + 1,y + 1);

            if(isHovered() && tooltip != null) {
                graphics.setTooltipForNextFrame(tooltip,mouseX, mouseY);
            }
        }



        @Override
        public void updateWidgetNarration(NarrationElementOutput output){}
    }
}
