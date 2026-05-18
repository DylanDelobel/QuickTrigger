package com.quicktrigger;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public final class QuickTriggerPayloads {

    public record HomeLimitPayload(
        int playerLimit,
        int maxHomes,
        String lockMessagesJson
    ) implements CustomPacketPayload {

        public static final Type<HomeLimitPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("quicktrigger", "data"));

        public static final StreamCodec<FriendlyByteBuf, HomeLimitPayload> CODEC =
            StreamCodec.of(
                (buf, p) -> {
                    buf.writeVarInt(p.playerLimit());
                    buf.writeVarInt(p.maxHomes());
                    buf.writeUtf(p.lockMessagesJson());
                },
                buf -> new HomeLimitPayload(buf.readVarInt(), buf.readVarInt(), buf.readUtf())
            );

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    private QuickTriggerPayloads() {}
}
