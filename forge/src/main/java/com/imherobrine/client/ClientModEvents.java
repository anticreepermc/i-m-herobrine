package com.imherobrine.client;

import com.imherobrine.ImHerobrineMod;
import com.imherobrine.network.C2SHerobrineActionPacket;
import com.imherobrine.network.C2SHerobrineActionPacket.Action;
import com.imherobrine.network.HerobrineNetworking;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ImHerobrineMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(HerobrineKeyBindings.LIGHTNING);
        event.register(HerobrineKeyBindings.BUFFS);
        event.register(HerobrineKeyBindings.CLEAR);
        event.register(HerobrineKeyBindings.TOGGLE_FLY);
        event.register(HerobrineKeyBindings.CLEAR_LEAVES);
    }
}

@Mod.EventBusSubscriber(modid = ImHerobrineMod.MODID, value = Dist.CLIENT)
final class ClientTickHandler {
    private ClientTickHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) {
            return;
        }
        if (HerobrineKeyBindings.LIGHTNING.consumeClick()) {
            HerobrineNetworking.sendToServer(new C2SHerobrineActionPacket(Action.LIGHTNING));
        }
        if (HerobrineKeyBindings.BUFFS.consumeClick()) {
            HerobrineNetworking.sendToServer(new C2SHerobrineActionPacket(Action.APPLY_BUFFS));
        }
        if (HerobrineKeyBindings.CLEAR.consumeClick()) {
            HerobrineNetworking.sendToServer(new C2SHerobrineActionPacket(Action.CLEAR_BUFFS));
        }
        if (HerobrineKeyBindings.TOGGLE_FLY.consumeClick()) {
            HerobrineNetworking.sendToServer(new C2SHerobrineActionPacket(Action.TOGGLE_FLY));
        }
        if (HerobrineKeyBindings.CLEAR_LEAVES.consumeClick()) {
            HerobrineNetworking.sendToServer(new C2SHerobrineActionPacket(Action.CLEAR_NEARBY_LEAVES));
        }
    }
}
