package com.quicktrigger;

import com.google.gson.Gson;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod("quicktrigger")
public class QuickTriggerNeoForge {

    static final Gson GSON = new Gson();

    public QuickTriggerNeoForge(IEventBus modEventBus, ModContainer container) {
        // Server-side config
        QuickTriggerServerConfig.init(() -> FMLPaths.CONFIGDIR.get());
        QuickTriggerServerConfig.INSTANCE.load();

        modEventBus.addListener(this::onRegisterPayloads);

        NeoForge.EVENT_BUS.register(new ServerEventHandler());

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            QuickTriggerConfig.init(() -> FMLPaths.CONFIGDIR.get());
            QuickTriggerConfig.INSTANCE.load();
            NeoForge.EVENT_BUS.register(new ClientEventHandler());
        }
    }

    private void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("quicktrigger");
        registrar.playToClient(
            QuickTriggerPayloads.HomeLimitPayload.TYPE,
            QuickTriggerPayloads.HomeLimitPayload.CODEC,
            (payload, ctx) -> {
                if (FMLEnvironment.getDist() == Dist.CLIENT) {
                    ClientEventHandler.handlePayload(payload);
                }
            }
        );
    }
}
