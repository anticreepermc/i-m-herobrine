package com.imherobrine.client;

import com.imherobrine.common.HerobrineAction;
import com.imherobrine.common.HerobrineClientConfig;
import com.imherobrine.common.HerobrineStructureBuilder;
import com.imherobrine.common.HerobrineStructureType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public final class HerobrineMenuScreen extends Screen {
    private final Consumer<HerobrineAction> sender;
    private int selectedIndex;
    private int page;

    public HerobrineMenuScreen(Consumer<HerobrineAction> sender) {
        super(Component.literal("I'm Herobrine"));
        this.sender = sender;
    }

    @Override
    protected void init() {
        clearWidgets();
        int center = width / 2;
        int top = height / 2 - 92;
        addRenderableWidget(Button.builder(Component.literal("Lightning"), button -> {
            page = 0;
            rebuildWidgets();
        }).bounds(center - 154, top, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Structures"), button -> {
            page = 1;
            rebuildWidgets();
        }).bounds(center + 4, top, 150, 20).build());

        if (page == 0) {
            addAction(center, top + 30, "Single bolt", HerobrineAction.SET_LIGHTNING_SINGLE);
            addAction(center, top + 53, "Burst", HerobrineAction.SET_LIGHTNING_BURST);
            addAction(center, top + 76, "Chain", HerobrineAction.SET_LIGHTNING_CHAIN);
            addAction(center, top + 99, "Ring", HerobrineAction.SET_LIGHTNING_RING);
            addAction(center, top + 122, "Storm", HerobrineAction.SET_LIGHTNING_STORM);
            addAction(center, top + 145, "All mobs, no players", HerobrineAction.SET_LIGHTNING_NON_PLAYERS);
            addAction(center, top + 168, "All players", HerobrineAction.SET_LIGHTNING_ALL_PLAYERS);
            addRenderableWidget(Button.builder(Component.literal("Strike with selected mode"), button -> send(HerobrineAction.LIGHTNING))
                    .bounds(center - 100, top + 192, 200, 20).build());
        } else {
            HerobrineStructureType[] types = HerobrineStructureType.values();
            selectedIndex = Math.floorMod(selectedIndex, types.length);
            addRenderableWidget(Button.builder(Component.literal("‹ Previous"), button -> {
                selectedIndex = Math.floorMod(selectedIndex - 1, types.length);
                rebuildWidgets();
            }).bounds(center - 154, top + 29, 92, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Next ›"), button -> {
                selectedIndex = (selectedIndex + 1) % types.length;
                rebuildWidgets();
            }).bounds(center + 62, top + 29, 92, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Select"), button -> send(types[selectedIndex].selectionAction()))
                    .bounds(center - 49, top + 29, 98, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Rotate"), button -> send(HerobrineAction.ROTATE_STRUCTURE))
                    .bounds(center - 154, top + 158, 98, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Build here"), button -> {
                send(types[selectedIndex].selectionAction());
                send(HerobrineAction.BUILD_SELECTED_STRUCTURE);
            }).bounds(center - 49, top + 158, 98, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> onClose())
                    .bounds(center + 56, top + 158, 98, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Import help"), button -> {
                if (minecraft != null && minecraft.player != null) minecraft.player.displayClientMessage(
                        Component.literal("Put <name>.json in config/imherobrine/structures, then run /imherobrine import <name>"), false);
            }).bounds(center - 154, top + 181, 150, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Reset rotation"), button -> send(HerobrineAction.RESET_STRUCTURE_ROTATION))
                    .bounds(center + 4, top + 181, 150, 20).build());
        }
    }

    private void addAction(int center, int y, String label, HerobrineAction action) {
        addRenderableWidget(Button.builder(Component.literal(label), button -> send(action))
                .bounds(center - 100, y, 200, 20).build());
    }

    private void send(HerobrineAction action) {
        sender.accept(action);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int center = width / 2;
        int top = height / 2 - 92;
        graphics.drawCenteredString(font, title, center, top - 20, 0xFFFFFF);
        if (page == 1) {
            HerobrineStructureType type = HerobrineStructureType.values()[Math.floorMod(selectedIndex, HerobrineStructureType.values().length)];
            graphics.drawCenteredString(font, type.title(), center, top + 43, 0xFFE080);
            if (HerobrineClientConfig.previewEnabled()) {
                drawPreview(graphics, center, top + 52, type);
            } else {
                graphics.drawCenteredString(font, "Preview disabled in client settings", center, top + 97, 0xFFAAAAAA);
            }
        } else {
            graphics.drawCenteredString(font, "Choose a lightning pattern, then use H to strike", center, top + 145, 0xB0B0B0);
        }
    }

    private void drawPreview(GuiGraphics graphics, int center, int top, HerobrineStructureType type) {
        int left = center - 132;
        int right = center + 132;
        int bottom = top + 88;
        graphics.fill(left, top, right, bottom, 0xD010141B);
        graphics.fill(left, top, right, top + 1, 0xFF68798D);
        graphics.fill(left, bottom - 1, right, bottom, 0xFF68798D);
        graphics.fill(left, top, left + 1, bottom, 0xFF68798D);
        graphics.fill(right - 1, top, right, bottom, 0xFF68798D);

        var blocks = HerobrineStructureBuilder.previewBlocks(type);
        if (blocks.isEmpty() || minecraft == null) return;
        int minX = blocks.stream().mapToInt(HerobrineStructureBuilder.PreviewBlock::x).min().orElse(0);
        int maxX = blocks.stream().mapToInt(HerobrineStructureBuilder.PreviewBlock::x).max().orElse(0);
        int minY = blocks.stream().mapToInt(HerobrineStructureBuilder.PreviewBlock::y).min().orElse(0);
        int maxY = blocks.stream().mapToInt(HerobrineStructureBuilder.PreviewBlock::y).max().orElse(0);
        int minZ = blocks.stream().mapToInt(HerobrineStructureBuilder.PreviewBlock::z).min().orElse(0);
        int maxZ = blocks.stream().mapToInt(HerobrineStructureBuilder.PreviewBlock::z).max().orElse(0);
        float spanX = maxX - minX + 1;
        float spanY = maxY - minY + 1;
        float spanZ = maxZ - minZ + 1;
        float projectedWidth = spanX * 0.79F + spanZ * 0.62F;
        float projectedHeight = spanY * 0.88F + spanX * 0.18F + spanZ * 0.29F;
        float scale = Math.min(9.0F, Math.min(210.0F / projectedWidth, 72.0F / projectedHeight));
        float modelCenterX = (minX + maxX + 1) / 2.0F;
        float modelCenterY = (minY + maxY + 1) / 2.0F;
        float modelCenterZ = (minZ + maxZ + 1) / 2.0F;

        PoseStack pose = new PoseStack();
        pose.pushPose();
        pose.translate(center, top + 45, 180);
        pose.scale(scale, -scale, scale);
        pose.mulPose(Axis.XP.rotationDegrees(28.0F));
        pose.mulPose(Axis.YP.rotationDegrees(-38.0F));
        pose.translate(-modelCenterX, -modelCenterY, -modelCenterZ);
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        for (var block : blocks) {
            pose.pushPose();
            pose.translate(block.x(), block.y(), block.z());
            minecraft.getBlockRenderer().renderSingleBlock(block.state(), pose, buffers, 0x00F000F0, OverlayTexture.NO_OVERLAY);
            pose.popPose();
        }
        buffers.endBatch();
        pose.popPose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
