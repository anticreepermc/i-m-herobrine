package com.imherobrine.network;

import com.imherobrine.HerobrineGameEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record C2SHerobrineActionPacket(Action action) {

    public enum Action {
        LIGHTNING,
        APPLY_BUFFS,
        CLEAR_BUFFS,
        TOGGLE_FLY,
        CLEAR_NEARBY_LEAVES
    }

    public static void encode(C2SHerobrineActionPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.action);
    }

    public static C2SHerobrineActionPacket decode(FriendlyByteBuf buf) {
        return new C2SHerobrineActionPacket(buf.readEnum(Action.class));
    }

    public static void handle(C2SHerobrineActionPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                HerobrineGameEvents.handleServerAction(player, msg.action());
            }
        });
        ctx.setPacketHandled(true);
    }
}
