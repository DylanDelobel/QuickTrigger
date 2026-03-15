package com.quicktrigger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class QuickTriggerServerConfig {

    public static final QuickTriggerServerConfig INSTANCE = new QuickTriggerServerConfig();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("quicktrigger-server.json");

    /** Nombre total de slots Home disponibles sur ce serveur (1–9). */
    public int maxHomes = 1;

    /**
     * Message affiché sur chaque lit gris verrouillé.
     * Indice 0 = slot 2, indice 1 = slot 3, … indice 7 = slot 9.
     * (Le slot 1 est toujours débloqué, il n'a pas de message de verrouillage.)
     */
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
        if (!Files.exists(CONFIG_PATH)) {
            save(); // Génère le fichier par défaut
            return;
        }
        try {
            String json = Files.readString(CONFIG_PATH);
            QuickTriggerServerConfig loaded = GSON.fromJson(json, QuickTriggerServerConfig.class);
            if (loaded == null) return;
            this.maxHomes = Math.max(1, Math.min(9, loaded.maxHomes));
            if (loaded.lockMessages != null) {
                // Copie jusqu'à 8 messages (slots 2–9), complète avec "" si le tableau est plus court
                for (int i = 0; i < 8; i++) {
                    this.lockMessages[i] = (i < loaded.lockMessages.length && loaded.lockMessages[i] != null)
                        ? loaded.lockMessages[i]
                        : "";
                }
            }
        } catch (IOException e) {
            // Garde les valeurs par défaut
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
