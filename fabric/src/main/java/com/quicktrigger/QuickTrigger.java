package com.quicktrigger;

import com.google.gson.Gson;
import com.quicktrigger.QuickTriggerPayloads.HomeLimitPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;


import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;

import net.minecraft.world.scores.Scoreboard;

public class QuickTrigger implements ModInitializer{

    private static final Gson GSON =new Gson();

    @Override
    public void onInitialize() {
        QuickTriggerServerConfig.init(FabricLoader.getInstance()::getConfigDir);


        QuickTriggerServerConfig.INSTANCE.load();

        PayloadTypeRegistry.clientboundPlay().register(HomeLimitPayload.TYPE,HomeLimitPayload.CODEC);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server)-> sendLimit(handler.player, server));
    }

    private static void sendLimit(ServerPlayer player, MinecraftServer server) {
        // vanilla client or client without the mod, nothing to do
        if (!ServerPlayNetworking.canSend(player, HomeLimitPayload.TYPE))return;



        QuickTriggerServerConfig cfg = QuickTriggerServerConfig.INSTANCE;
        Scoreboard board =server.getScoreboard();
        Objective objective = board.getObjective("homes.limit");
        if(objective == null)return; //datapack not installed

        ReadOnlyScoreInfo score= board.getPlayerScoreInfo(ScoreHolder.forNameOnly(player.getScoreboardName()), objective);
        // no score yet = new player, they get the one free slot
        int limit= score != null ? Math.min(score.value(),cfg.maxHomes): 1;

        ServerPlayNetworking.send(player, new HomeLimitPayload(limit, cfg.maxHomes, GSON.toJson(cfg.lockMessages)));
    }

}
