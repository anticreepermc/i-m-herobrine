package com.imherobrine.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.imherobrine.ImHerobrineMod;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

public final class HerobrineKeyBindings {
    public static final String CATEGORY = "key.categories." + ImHerobrineMod.MODID;

    public static final KeyMapping LIGHTNING = new KeyMapping(
            "key." + ImHerobrineMod.MODID + ".lightning",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            CATEGORY
    );

    public static final KeyMapping BUFFS = new KeyMapping(
            "key." + ImHerobrineMod.MODID + ".buffs",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            CATEGORY
    );

    public static final KeyMapping CLEAR = new KeyMapping(
            "key." + ImHerobrineMod.MODID + ".clear",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            CATEGORY
    );

    public static final KeyMapping TOGGLE_FLY = new KeyMapping(
            "key." + ImHerobrineMod.MODID + ".toggle_fly",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F,
            CATEGORY
    );

    public static final KeyMapping CLEAR_LEAVES = new KeyMapping(
            "key." + ImHerobrineMod.MODID + ".clear_leaves",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            CATEGORY
    );

    private HerobrineKeyBindings() {
    }
}
