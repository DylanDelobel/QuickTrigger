package com.quicktrigger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;

public class QuickTrigger implements ModInitializer {

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(
            QuickTriggerPayloads.HomeLimitPayload.TYPE,
            QuickTriggerPayloads.HomeLimitPayload.CODEC
        );

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
            trySend(handler.player, server)
        );
    }

    private static void trySend(ServerPlayer player, MinecraftServer server) {
        if (!ServerPlayNetworking.canSend(player, QuickTriggerPayloads.HomeLimitPayload.TYPE)) return;

        Scoreboard scoreboard = server.getScoreboard();
        Objective objective = scoreboard.getObjective("homes.limit");
        if (objective == null) return;

        ScoreHolder holder = ScoreHolder.forNameOnly(player.getScoreboardName());
        ReadOnlyScoreInfo scoreInfo = scoreboard.getPlayerScoreInfo(holder, objective);
        if (scoreInfo == null) return;

        ServerPlayNetworking.send(player, new QuickTriggerPayloads.HomeLimitPayload(scoreInfo.value()));
    }
}
