package com.imherobrine.fabric;

import com.imherobrine.common.HerobrineAction;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public final class ImHerobrineFabricClient implements ClientModInitializer {
    private static final String CATEGORY = "key.categories.imherobrine";
    private static final KeyMapping LIGHTNING = key("lightning", GLFW.GLFW_KEY_H);
    private static final KeyMapping BUFFS = key("buffs", GLFW.GLFW_KEY_G);
    private static final KeyMapping CLEAR = key("clear", GLFW.GLFW_KEY_V);
    private static final KeyMapping FLY = key("toggle_fly", GLFW.GLFW_KEY_F);
    private static final KeyMapping LEAVES = key("clear_leaves", GLFW.GLFW_KEY_C);

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(LIGHTNING);
        KeyBindingHelper.registerKeyBinding(BUFFS);
        KeyBindingHelper.registerKeyBinding(CLEAR);
        KeyBindingHelper.registerKeyBinding(FLY);
        KeyBindingHelper.registerKeyBinding(LEAVES);
        ClientTickEvents.END_CLIENT_TICK.register(ImHerobrineFabricClient::tick);
    }

    private static KeyMapping key(String name, int code) {
        return new KeyMapping("key.imherobrine." + name, InputConstants.Type.KEYSYM, code, CATEGORY);
    }

    private static void tick(Minecraft minecraft) {
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
            ClientPlayNetworking.send(new FabricNetworking.ActionPayload(action));
        }
    }
}
