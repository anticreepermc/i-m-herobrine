//? fabric {
package com.imherobrine.fabric;

import com.imherobrine.common.HerobrineAction;
import com.imherobrine.client.HerobrineMenuScreen;
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
    private static final KeyMapping TREE_TORCHES = key("tree_torches", GLFW.GLFW_KEY_T);
    private static final KeyMapping TELEPORT_TO_AIM = key("teleport_to_aim", GLFW.GLFW_KEY_P);
    private static final KeyMapping MENU = key("menu", GLFW.GLFW_KEY_M);

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(LIGHTNING);
        KeyBindingHelper.registerKeyBinding(BUFFS);
        KeyBindingHelper.registerKeyBinding(CLEAR);
        KeyBindingHelper.registerKeyBinding(FLY);
        KeyBindingHelper.registerKeyBinding(LEAVES);
        KeyBindingHelper.registerKeyBinding(TREE_TORCHES);
        KeyBindingHelper.registerKeyBinding(TELEPORT_TO_AIM);
        KeyBindingHelper.registerKeyBinding(MENU);
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
        send(TREE_TORCHES, HerobrineAction.PLACE_TREE_TORCHES);
        send(TELEPORT_TO_AIM, HerobrineAction.TELEPORT_TO_AIM);
        if (MENU.consumeClick()) {
            minecraft.setScreen(new HerobrineMenuScreen(action -> ClientPlayNetworking.send(new FabricNetworking.ActionPayload(action))));
        }
    }

    private static void send(KeyMapping key, HerobrineAction action) {
        if (key.consumeClick()) {
            ClientPlayNetworking.send(new FabricNetworking.ActionPayload(action));
        }
    }
}

//?}
