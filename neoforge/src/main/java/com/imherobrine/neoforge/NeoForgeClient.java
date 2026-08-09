package com.imherobrine.neoforge;

import com.imherobrine.common.HerobrineAction;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "imherobrine", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class NeoForgeClient {
    private static final String CATEGORY = "key.categories.imherobrine";
    private static final KeyMapping LIGHTNING = key("lightning", GLFW.GLFW_KEY_H);
    private static final KeyMapping BUFFS = key("buffs", GLFW.GLFW_KEY_G);
    private static final KeyMapping CLEAR = key("clear", GLFW.GLFW_KEY_V);
    private static final KeyMapping FLY = key("toggle_fly", GLFW.GLFW_KEY_F);
    private static final KeyMapping LEAVES = key("clear_leaves", GLFW.GLFW_KEY_C);

    private NeoForgeClient() {
    }

    private static KeyMapping key(String name, int code) {
        return new KeyMapping("key.imherobrine." + name, InputConstants.Type.KEYSYM, code, CATEGORY);
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(LIGHTNING);
        event.register(BUFFS);
        event.register(CLEAR);
        event.register(FLY);
        event.register(LEAVES);
    }

    @Mod.EventBusSubscriber(modid = "imherobrine", value = Dist.CLIENT)
    public static final class TickHandler {
        private TickHandler() {
        }

        @SubscribeEvent
        public static void tick(ClientTickEvent.Post event) {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            if (player == null || minecraft.screen != null) {
                return;
            }
            send(LIGHTNING, HerobrineAction.LIGHTNING);
            send(BUFFS, HerobrineAction.APPLY_BUFFS);
            send(CLEAR, HerobrineAction.CLEAR_BUFFS);
            send(FLY, HerobrineAction.TOGGLE_FLY);
            send(LEAVES, HerobrineAction.CLEAR_NEARBY_LEAVES);
        }

        private static void send(KeyMapping key, HerobrineAction action) {
            if (key.consumeClick()) {
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                        new NeoForgeNetworking.ActionPayload(action));
            }
        }
    }
}
