package com.quicktrigger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class QuickTriggerConfig {

    public static final QuickTriggerConfig INSTANCE = new QuickTriggerConfig();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("quicktrigger.json");

    public enum BedColor {
        WHITE(Items.WHITE_BED),
        ORANGE(Items.ORANGE_BED),
        MAGENTA(Items.MAGENTA_BED),
        LIGHT_BLUE(Items.LIGHT_BLUE_BED),
        YELLOW(Items.YELLOW_BED),
        LIME(Items.LIME_BED),
        PINK(Items.PINK_BED),
        GRAY(Items.GRAY_BED),
        LIGHT_GRAY(Items.LIGHT_GRAY_BED),
        CYAN(Items.CYAN_BED),
        PURPLE(Items.PURPLE_BED),
        BLUE(Items.BLUE_BED),
        BROWN(Items.BROWN_BED),
        GREEN(Items.GREEN_BED),
        RED(Items.RED_BED),
        BLACK(Items.BLACK_BED);

        public final Item item;

        BedColor(Item item) {
            this.item = item;
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

    // Stored as enum names in JSON — 9 slots max
    public String[] bedColors = {"BLUE", "GREEN", "ORANGE", "PURPLE", "RED", "CYAN", "YELLOW", "LIME", "WHITE"};

    public BedColor getColor(int homeIndex) {
        if (homeIndex < 0 || homeIndex >= bedColors.length) return BedColor.BLUE;
        return BedColor.fromName(bedColors[homeIndex]);
    }

    public void setColor(int homeIndex, BedColor color) {
        if (homeIndex >= 0 && homeIndex < bedColors.length) {
            bedColors[homeIndex] = color.name();
        }
    }

    public ItemStack getItemStack(int homeIndex) {
        return new ItemStack(getColor(homeIndex).item);
    }

    public void load() {
        if (!Files.exists(CONFIG_PATH)) return;
        try {
            String json = Files.readString(CONFIG_PATH);
            QuickTriggerConfig loaded = GSON.fromJson(json, QuickTriggerConfig.class);
            if (loaded != null && loaded.bedColors != null && loaded.bedColors.length == 4) {
                this.bedColors = loaded.bedColors;
            }
        } catch (IOException e) {
            // Garde les valeurs par défaut si le fichier est illisible
        }
    }

    public void save() {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException e) {
            // Échec silencieux
        }
    }
}
