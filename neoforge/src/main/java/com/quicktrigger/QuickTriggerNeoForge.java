package com.quicktrigger;

import com.quicktrigger.QuickTriggerPayloads.HomeLimitPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;


import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod("quicktrigger")
public class QuickTriggerNeoForge {

    public QuickTriggerNeoForge(IEventBus modBus, ModContainer container){
        //server config loads on both sides, singleplayer needs it too
        QuickTriggerServerConfig.init(FMLPaths.CONFIGDIR::get);

        QuickTriggerServerConfig.INSTANCE.load();

        modBus.addListener(this::onRegisterPayloads);
        NeoForge.EVENT_BUS.register(new ServerEventHandler());

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            QuickTriggerConfig.init(FMLPaths.CONFIGDIR::get);


            QuickTriggerConfig.INSTANCE.load();
            NeoForge.EVENT_BUS.register(new ClientEventHandler());
        }
    }

    private void onRegisterPayloads(RegisterPayloadHandlersEvent event){
        event.registrar("quicktrigger").playToClient(HomeLimitPayload.TYPE, HomeLimitPayload.CODEC,(payload,ctx) ->{


            // dist check so a dedicated server never touches ClientEventHandler
            if (FMLEnvironment.getDist()== Dist.CLIENT){
                ClientEventHandler.handlePayload(payload);
            }
        });
    }
}
