package com.quicktrigger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class QuickTriggerServerConfig {

    public static final QuickTriggerServerConfig INSTANCE = new QuickTriggerServerConfig();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path CONFIG_PATH;

    public static void init(ConfigDirProvider provider) {
        CONFIG_PATH = provider.getConfigDir().resolve("quicktrigger-server.json");
    }

    public int maxHomes = 1;

    public String[] lockMessages = {
        "Unlock slot #2",
        "Unlock slot #3",
        "Unlock slot #4",
        "Unlock slot #5",
        "Unlock slot #6",
        "Unlock slot #7",
        "Unlock slot #8",
        "Unlock slot #9"
    };

    public void load() {
        if (CONFIG_PATH == null) return;
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }
        try {
            String json = Files.readString(CONFIG_PATH);
            QuickTriggerServerConfig loaded = GSON.fromJson(json, QuickTriggerServerConfig.class);
            if (loaded == null) return;
            this.maxHomes = Math.max(1, Math.min(9, loaded.maxHomes));
            if (loaded.lockMessages != null) {
                for (int i = 0; i < 8; i++) {
                    this.lockMessages[i] = (i < loaded.lockMessages.length && loaded.lockMessages[i] != null)
                        ? loaded.lockMessages[i]
                        : "";
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
