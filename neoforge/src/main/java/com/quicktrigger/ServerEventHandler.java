package com.quicktrigger;

import com.google.gson.Gson;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;

public class ServerEventHandler {

    private static final Gson GSON = new Gson();

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        trySend(player);
    }

    private static void trySend(ServerPlayer player) {
        QuickTriggerServerConfig cfg = QuickTriggerServerConfig.INSTANCE;

        Scoreboard scoreboard = player.level().getServer().getScoreboard();
        Objective objective = scoreboard.getObjective("homes.limit");
        if (objective == null) return;

        ScoreHolder holder = ScoreHolder.forNameOnly(player.getScoreboardName());
        ReadOnlyScoreInfo scoreInfo = scoreboard.getPlayerScoreInfo(holder, objective);
        int limit = scoreInfo != null ? Math.min(scoreInfo.value(), cfg.maxHomes) : 1;
        String lockMessagesJson = GSON.toJson(cfg.lockMessages);

        PacketDistributor.sendToPlayer(player, new QuickTriggerPayloads.HomeLimitPayload(
            limit,
            cfg.maxHomes,
            lockMessagesJson
        ));
    }
}
