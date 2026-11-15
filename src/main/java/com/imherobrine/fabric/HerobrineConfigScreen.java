//? fabric {
package com.imherobrine.fabric;

import com.imherobrine.common.HerobrineClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class HerobrineConfigScreen extends Screen {
    private final Screen parent;

    HerobrineConfigScreen(Screen parent) {
        super(Component.literal("I'm Herobrine settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(previewLabel(), button -> {
            HerobrineClientConfig.togglePreview();
            button.setMessage(previewLabel());
        }).bounds(width / 2 - 100, height / 2 - 10, 200, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(width / 2 - 100, height / 2 + 24, 200, 20).build());
    }

    private static Component previewLabel() {
        return Component.literal("Structure preview: " + (HerobrineClientConfig.previewEnabled() ? "ON" : "OFF"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 50, 0xFFFFFF);
        graphics.drawCenteredString(font, "Server permissions and feature limits are configured", width / 2, height / 2 - 32, 0xC0C0C0);
        graphics.drawCenteredString(font, "in config/imherobrine-server.properties on the server.", width / 2, height / 2 - 20, 0xC0C0C0);
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
//?}
