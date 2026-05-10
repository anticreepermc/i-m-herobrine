package com.imherobrine.network;

import com.imherobrine.ImHerobrineMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public final class HerobrineNetworking {
    private static final int PROTOCOL = 1;
    public static final SimpleChannel CHANNEL = ChannelBuilder
            .named(new ResourceLocation(ImHerobrineMod.MODID, "main"))
            .networkProtocolVersion(PROTOCOL)
            .simpleChannel();

    private static int id = 0;

    public static void register() {
        CHANNEL.messageBuilder(C2SHerobrineActionPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SHerobrineActionPacket::encode)
                .decoder(C2SHerobrineActionPacket::decode)
                .consumerMainThread(C2SHerobrineActionPacket::handle)
                .add();
    }

    public static void sendToServer(C2SHerobrineActionPacket packet) {
        CHANNEL.send(packet, PacketDistributor.SERVER.noArg());
    }

    private HerobrineNetworking() {
    }
}
