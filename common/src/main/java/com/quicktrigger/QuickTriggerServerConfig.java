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

    public static void init(ConfigDirProvider provider){
        CONFIG_PATH=provider.getConfigDir().resolve("quicktrigger-server.json");
    }


    public int maxHomes = 1;

    // one per slot from #2 up, slot 1 is never locked
    public String[] lockMessages= {
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


        if(!Files.exists(CONFIG_PATH)) {
            save();//first run, write the defaults so admins have something to edit
            return;
        }
        try {
            QuickTriggerServerConfig loaded = GSON.fromJson(Files.readString(CONFIG_PATH), QuickTriggerServerConfig.class);


            if (loaded == null) return;

            this.maxHomes = Math.max(1, Math.min(9, loaded.maxHomes)); //9 = hotbar-ish row, no room for more
            String[]msgs=loaded.lockMessages;
            if (msgs != null) {
                for (int i = 0; i < 8; i++) {
                    lockMessages[i]= (i < msgs.length && msgs[i]!= null)? msgs[i] : "";
                }
            }

        } catch(IOException e) {
            // broken file, just run on defaults
        }
    }

    public void save() {
        if(CONFIG_PATH == null) return;
        try{
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException e) {
            // not worth crashing the server over
        }
    }
}
