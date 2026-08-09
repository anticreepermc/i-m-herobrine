package com.imherobrine.network;

import com.imherobrine.HerobrineGameEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

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

    public static void handle(C2SHerobrineActionPacket msg, CustomPayloadEvent.Context ctx) {
        ServerPlayer player = ctx.getSender();
        if (player != null && player.hasPermissions(2)) {
            HerobrineGameEvents.handleServerAction(player, msg.action());
        }
        ctx.setPacketHandled(true);
    }
}
