package com.imherobrine.fabric;

import com.imherobrine.common.HerobrineAction;
import com.imherobrine.common.HerobrineGameLogic;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

final class FabricNetworking {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("imherobrine", "action");

    private FabricNetworking() {
    }

    static void register() {
        PayloadTypeRegistry.playC2S().register(ActionPayload.TYPE, ActionPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ActionPayload.TYPE, (payload, context) -> {
            if (context.player().hasPermissions(2)) {
                HerobrineGameLogic.handleServerAction(context.player(), payload.action());
            }
        });
    }

    record ActionPayload(HerobrineAction action) implements CustomPacketPayload {
        static final Type<ActionPayload> TYPE = new Type<>(ID);
        static final StreamCodec<RegistryFriendlyByteBuf, ActionPayload> CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, value -> value.action().ordinal(),
                        value -> new ActionPayload(HerobrineAction.values()[value]), ActionPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
