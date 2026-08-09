package com.imherobrine.neoforge;

import com.imherobrine.common.HerobrineAction;
import com.imherobrine.common.HerobrineGameLogic;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

final class NeoForgeNetworking {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("imherobrine", "action");

    private NeoForgeNetworking() {
    }

    static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(ActionPayload.TYPE, ActionPayload.CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player && player.hasPermissions(2)) {
                HerobrineGameLogic.handleServerAction(player, payload.action());
            }
        });
    }

    record ActionPayload(HerobrineAction action) implements CustomPacketPayload {
        static final Type<ActionPayload> TYPE = new Type<>(ID);
        static final StreamCodec<RegistryFriendlyByteBuf, ActionPayload> CODEC = StreamCodec.of(
                (buffer, payload) -> buffer.writeVarInt(payload.action().ordinal()),
                buffer -> new ActionPayload(HerobrineAction.values()[buffer.readVarInt()])
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
