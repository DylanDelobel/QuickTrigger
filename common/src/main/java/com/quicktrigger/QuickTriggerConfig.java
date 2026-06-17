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

public class QuickTriggerConfig {

    public static final QuickTriggerConfig INSTANCE = new QuickTriggerConfig();

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

        BedColor(DyeColor dyeColor) {
            this.item = Items.BED.pick(dyeColor);
        }

        public static BedColor fromName(String name) {
            for (BedColor c : values()) {
                if (c.name().equalsIgnoreCase(name)) return c;
            }
            return BLUE;
        }

        @Override
        public String toString() {
            return name();
        }
    }

    public static final int MAX_NAME_LENGTH = 24;

    public String[] bedColors = {"BLUE", "GREEN", "ORANGE", "PURPLE", "RED", "CYAN", "YELLOW", "LIME", "WHITE"};

    public Map<String, String[]> serverBedNames = new HashMap<>();

    public BedColor getColor(int homeIndex) {
        if (homeIndex < 0 || homeIndex >= bedColors.length) return BedColor.BLUE;
        return BedColor.fromName(bedColors[homeIndex]);
    }

    public void setColor(int homeIndex, BedColor color) {
        if (homeIndex >= 0 && homeIndex < bedColors.length) {
            bedColors[homeIndex] = color.name();
        }
    }

    public String[] getNamesForServer(String serverKey) {
        return serverBedNames.computeIfAbsent(serverKey, k -> new String[]{"", "", "", "", "", "", "", "", ""});
    }

    public ItemStack getItemStack(int homeIndex) {
        return new ItemStack(getColor(homeIndex).item);
    }

    public void load() {
        if (CONFIG_PATH == null || !Files.exists(CONFIG_PATH)) return;
        try {
            String json = Files.readString(CONFIG_PATH);
            QuickTriggerConfig loaded = GSON.fromJson(json, QuickTriggerConfig.class);
            if (loaded != null) {
                if (loaded.bedColors != null) {
                    for (int i = 0; i < this.bedColors.length; i++) {
                        if (i < loaded.bedColors.length && loaded.bedColors[i] != null) {
                            this.bedColors[i] = loaded.bedColors[i];
                        }
                    }
                }
                if (loaded.serverBedNames != null) {
                    for (Map.Entry<String, String[]> entry : loaded.serverBedNames.entrySet()) {
                        if (entry.getKey() == null || entry.getValue() == null) continue;
                        String[] normalized = new String[9];
                        Arrays.fill(normalized, "");
                        for (int i = 0; i < 9 && i < entry.getValue().length; i++) {
                            if (entry.getValue()[i] != null) {
                                String n = entry.getValue()[i].strip();
                                normalized[i] = n.length() > MAX_NAME_LENGTH ? n.substring(0, MAX_NAME_LENGTH) : n;
                            }
                        }
                        this.serverBedNames.put(entry.getKey(), normalized);
                    }
                }
            }
        } catch (IOException e) {
            // keep defaults
        }
    }

    public void save() {
        if (CONFIG_PATH == null) return;
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException e) {
            // silent failure
        }
    }
}
