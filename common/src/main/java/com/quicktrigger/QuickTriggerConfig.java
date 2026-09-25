package com.quicktrigger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.IOException;
import java.nio.file.Files;


import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


// client side config, bed colors are global but names are per server
public class QuickTriggerConfig{

    public static final QuickTriggerConfig INSTANCE =new QuickTriggerConfig();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path CONFIG_PATH;

    public static void init(ConfigDirProvider provider) {


        CONFIG_PATH = provider.getConfigDir().resolve("quicktrigger.json");
    }

    public enum BedColor {
        WHITE(DyeColor.WHITE),
        ORANGE(DyeColor.ORANGE),
        MAGENTA(DyeColor.MAGENTA),
        LIGHT_BLUE(DyeColor.LIGHT_BLUE),
        YELLOW(DyeColor.YELLOW),
        LIME(DyeColor.LIME),
        PINK(DyeColor.PINK),
        GRAY(DyeColor.GRAY),
        LIGHT_GRAY(DyeColor.LIGHT_GRAY),
        CYAN(DyeColor.CYAN),
        PURPLE(DyeColor.PURPLE),
        BLUE(DyeColor.BLUE),
        BROWN(DyeColor.BROWN),
        GREEN(DyeColor.GREEN),
        RED(DyeColor.RED),
        BLACK(DyeColor.BLACK);

        public final Item item;

        BedColor(DyeColor dye) {


            this.item = Items.BED.pick(dye);
        }

        //case insensitive, anything unknown falls back to blue
        public static BedColor fromName(String name) {
            for (BedColor c : values()){
                if (c.name().equalsIgnoreCase(name)) return c;
            }
            return BLUE;
        }


        @Override
        public String toString(){
            return name();
        }
    }

    public static final int MAX_NAME_LENGTH =24;

    //saved as strings so a renamed enum doesnt nuke peoples config
    public String[]bedColors={"BLUE", "GREEN", "ORANGE","PURPLE","RED","CYAN","YELLOW","LIME","WHITE"};

    public Map<String, String[]> serverBedNames = new HashMap<>(); // server ip / "local:<world>" -> 9 names

    public BedColor getColor(int slot) {
        if (slot < 0 || slot >= bedColors.length) return BedColor.BLUE;
        return BedColor.fromName(bedColors[slot]);
    }

    public void setColor(int slot, BedColor color) {
        if(slot >= 0 && slot < bedColors.length){
            bedColors[slot] = color.name();
        }
    }

    // returns the live array, callers write into it directly
    public String[] getNamesForServer(String serverKey) {
        return serverBedNames.computeIfAbsent(serverKey, k -> new String[]{"", "", "","", "","", "", "",""});
    }

    public ItemStack getItemStack(int slot) {


        return new ItemStack(getColor(slot).item);
    }

    public void load(){
        if (CONFIG_PATH == null || !Files.exists(CONFIG_PATH)) return;
        try{
            QuickTriggerConfig loaded =GSON.fromJson(Files.readString(CONFIG_PATH),QuickTriggerConfig.class);
            if (loaded == null)return;

            // copy over slot by slot so an old/short file still keeps the defaults for the rest
            if (loaded.bedColors != null) {
                for (int i =0;i < bedColors.length;i++){
                    if(i < loaded.bedColors.length && loaded.bedColors[i]!= null){
                        bedColors[i] = loaded.bedColors[i];
                    }
                }
            }



            if(loaded.serverBedNames != null){
                for(Map.Entry<String, String[]> e : loaded.serverBedNames.entrySet()){
                    String[] names=e.getValue();
                    if (e.getKey() == null || names == null) continue;

                    String[] fixed = new String[9];
                    Arrays.fill(fixed,"");
                    for(int i=0; i < 9 && i < names.length;i++){
                        if(names[i] == null)continue;
                        String n =names[i].strip();
                        fixed[i] = n.length() > MAX_NAME_LENGTH ? n.substring(0, MAX_NAME_LENGTH) : n; // someone hand edited the json
                    }
                    serverBedNames.put(e.getKey(),fixed);
                }
            }
        }catch (IOException e) {
            //keep defaults
        }
    }

    public void save() {
        if (CONFIG_PATH == null) return;

        try{
            Files.writeString(CONFIG_PATH,GSON.toJson(this));
        } catch (IOException e) {
            // meh, worst case they lose a color change
        }
    }
}
