//? forge {
package com.imherobrine.network;

import com.imherobrine.HerobrineGameEvents;
import com.imherobrine.common.HerobrineAction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public record C2SHerobrineActionPacket(HerobrineAction action) {

    public static void encode(C2SHerobrineActionPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.action);
    }

    public static C2SHerobrineActionPacket decode(FriendlyByteBuf buf) {
        return new C2SHerobrineActionPacket(buf.readEnum(HerobrineAction.class));
    }

    public static void handle(C2SHerobrineActionPacket msg, CustomPayloadEvent.Context ctx) {
        ServerPlayer player = ctx.getSender();
        if (player != null) {
            HerobrineGameEvents.handleServerAction(player, msg.action());
        }
        ctx.setPacketHandled(true);
    }
}

//?}
