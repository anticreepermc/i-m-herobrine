//? forge {
package com.imherobrine.client;

import com.imherobrine.ImHerobrineMod;
import com.imherobrine.network.C2SHerobrineActionPacket;
import com.imherobrine.common.HerobrineAction;
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
        event.register(HerobrineKeyBindings.TREE_TORCHES);
        event.register(HerobrineKeyBindings.TELEPORT_TO_AIM);
        event.register(HerobrineKeyBindings.MENU);
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
            HerobrineNetworking.sendToServer(new C2SHerobrineActionPacket(HerobrineAction.LIGHTNING));
        }
        if (HerobrineKeyBindings.BUFFS.consumeClick()) {
            HerobrineNetworking.sendToServer(new C2SHerobrineActionPacket(HerobrineAction.APPLY_BUFFS));
        }
        if (HerobrineKeyBindings.CLEAR.consumeClick()) {
            HerobrineNetworking.sendToServer(new C2SHerobrineActionPacket(HerobrineAction.CLEAR_BUFFS));
        }
        if (HerobrineKeyBindings.TOGGLE_FLY.consumeClick()) {
            HerobrineNetworking.sendToServer(new C2SHerobrineActionPacket(HerobrineAction.TOGGLE_FLY));
        }
        if (HerobrineKeyBindings.CLEAR_LEAVES.consumeClick()) {
            HerobrineNetworking.sendToServer(new C2SHerobrineActionPacket(HerobrineAction.CLEAR_NEARBY_LEAVES));
        }
        if (HerobrineKeyBindings.TREE_TORCHES.consumeClick()) {
            HerobrineNetworking.sendToServer(new C2SHerobrineActionPacket(HerobrineAction.PLACE_TREE_TORCHES));
        }
        if (HerobrineKeyBindings.TELEPORT_TO_AIM.consumeClick()) {
            HerobrineNetworking.sendToServer(new C2SHerobrineActionPacket(HerobrineAction.TELEPORT_TO_AIM));
        }
        if (HerobrineKeyBindings.MENU.consumeClick()) {
            mc.setScreen(new HerobrineMenuScreen(action -> HerobrineNetworking.sendToServer(new C2SHerobrineActionPacket(action))));
        }
    }
}

//?}
