package com.quicktrigger;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public final class QuickTriggerPayloads {

    public record HomeLimitPayload(int limit) implements CustomPacketPayload {

        public static final Type<HomeLimitPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("quicktrigger", "data"));

        public static final StreamCodec<FriendlyByteBuf, HomeLimitPayload> CODEC =
            StreamCodec.of(
                (buf, p) -> buf.writeVarInt(p.limit()),
                buf -> new HomeLimitPayload(buf.readVarInt())
            );

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    private QuickTriggerPayloads() {}
}
