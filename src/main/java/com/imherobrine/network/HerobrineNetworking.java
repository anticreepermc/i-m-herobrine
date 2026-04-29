package com.imherobrine.network;

import com.imherobrine.ImHerobrineMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class HerobrineNetworking {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ImHerobrineMod.MODID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private static int id = 0;

    public static void register() {
        CHANNEL.messageBuilder(C2SHerobrineActionPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SHerobrineActionPacket::encode)
                .decoder(C2SHerobrineActionPacket::decode)
                .consumerMainThread(C2SHerobrineActionPacket::handle)
                .add();
    }

    private HerobrineNetworking() {
    }
}
