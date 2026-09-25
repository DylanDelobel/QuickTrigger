package com.quicktrigger;

import com.google.gson.Gson;
import com.quicktrigger.QuickTriggerPayloads.HomeLimitPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;


import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;

public class ServerEventHandler {

    private static final Gson GSON =new Gson();


    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if(!(event.getEntity()instanceof ServerPlayer player)) return;
        sendLimit(player);
    }

    //same as the fabric side, keep the two in sync
    private static void sendLimit(ServerPlayer player) {


        QuickTriggerServerConfig cfg =QuickTriggerServerConfig.INSTANCE;

        Scoreboard board = player.level().getServer().getScoreboard();
        Objective objective = board.getObjective("homes.limit");
        if (objective == null)return; // no datapack, no limits

        ReadOnlyScoreInfo score = board.getPlayerScoreInfo(ScoreHolder.forNameOnly(player.getScoreboardName()),objective);
        int limit=score != null ? Math.min(score.value(), cfg.maxHomes): 1;



        // todo does this need a canSend check like the fabric side?
        PacketDistributor.sendToPlayer(player, new HomeLimitPayload(limit, cfg.maxHomes,GSON.toJson(cfg.lockMessages)));
    }
}
