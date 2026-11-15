package com.imherobrine.client;

import com.imherobrine.common.HerobrineAction;
import com.imherobrine.common.HerobrineClientConfig;
import com.imherobrine.common.HerobrineStructureBuilder;
import com.imherobrine.common.HerobrineStructureType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
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
            }).bounds(center - 154, top + 34, 92, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Next ›"), button -> {
                selectedIndex = (selectedIndex + 1) % types.length;
                rebuildWidgets();
            }).bounds(center + 62, top + 34, 92, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Select"), button -> send(types[selectedIndex].selectionAction()))
                    .bounds(center - 49, top + 34, 98, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Rotate"), button -> send(HerobrineAction.ROTATE_STRUCTURE))
                    .bounds(center - 154, top + 151, 98, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Build here"), button -> {
                send(types[selectedIndex].selectionAction());
                send(HerobrineAction.BUILD_SELECTED_STRUCTURE);
            }).bounds(center - 49, top + 151, 98, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> onClose())
                    .bounds(center + 56, top + 151, 98, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Import help"), button -> {
                if (minecraft != null && minecraft.player != null) minecraft.player.displayClientMessage(
                        Component.literal("Put <name>.json in config/imherobrine/structures, then run /imherobrine import <name>"), false);
            }).bounds(center - 154, top + 177, 150, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Reset rotation"), button -> send(HerobrineAction.RESET_STRUCTURE_ROTATION))
                    .bounds(center + 4, top + 177, 150, 20).build());
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
            graphics.drawCenteredString(font, type.title(), center, top + 70, 0xFFE080);
            graphics.drawCenteredString(font, "Preview · rotate before building", center, top + 91, 0xB0B0B0);
            if (HerobrineClientConfig.previewEnabled()) {
                drawPreview(graphics, center, top + 107, type);
            } else {
                graphics.drawCenteredString(font, "Preview disabled in client settings", center, top + 125, 0xFFAAAAAA);
            }
        } else {
            graphics.drawCenteredString(font, "Choose a lightning pattern, then use H to strike", center, top + 145, 0xB0B0B0);
        }
    }

    private void drawPreview(GuiGraphics graphics, int center, int y, HerobrineStructureType type) {
        int panelLeft = center - 100;
        graphics.fill(panelLeft, y - 4, center + 100, y + 44, 0xC0101010);
        graphics.fill(panelLeft, y - 4, center + 100, y - 3, 0xFF707070);
        graphics.fill(panelLeft, y + 43, center + 100, y + 44, 0xFF707070);
        if (type == HerobrineStructureType.WORD_HEROBRINE || type == HerobrineStructureType.WORD_STOP
                || type.name().startsWith("LETTER_")) {
            String word = switch (type) {
                case WORD_HEROBRINE -> "HEROBRINE";
                case WORD_STOP -> "STOP";
                default -> type.name().substring("LETTER_".length());
            };
            int block = word.length() > 5 ? 3 : 5;
            int wordWidth = word.length() * 6 * block - block;
            int startX = center - wordWidth / 2;
            for (int index = 0; index < word.length(); index++) {
                String[] glyph = HerobrineStructureBuilder.glyph(word.charAt(index));
                if (glyph == null) continue;
                for (int row = 0; row < glyph.length; row++) {
                    for (int col = 0; col < glyph[row].length(); col++) {
                        if (glyph[row].charAt(col) == '1') {
                            int x = startX + index * 6 * block + col * block;
                            int py = y + 2 + row * 5;
                            graphics.fill(x, py, x + block - 1, py + 3, 0xFFFFD45A);
                        }
                    }
                }
            }
            return;
        }
        if (type.name().contains("PYRAMID")) {
            int width = switch (type) {
                case SAND_PYRAMID_SMALL, COBBLE_PYRAMID_SMALL -> 5;
                case SAND_PYRAMID_MEDIUM, COBBLE_PYRAMID_MEDIUM -> 9;
                default -> 13;
            };
            int cell = 3;
            int left = center - width * cell / 2;
            int top = y + 1;
            for (int layer = 0; layer < (width + 1) / 2; layer++) {
                int half = width / 2 - layer;
                for (int dx = -half; dx <= half; dx++) for (int dz = -half; dz <= half; dz++) {
                    graphics.fill(left + (width / 2 + dx) * cell, top + (width / 2 + dz) * cell,
                            left + (width / 2 + dx + 1) * cell - 1, top + (width / 2 + dz + 1) * cell - 1,
                            type.name().startsWith("SAND") ? 0xFFE3C15A : 0xFF858585);
                }
            }
            return;
        }
        if (type == HerobrineStructureType.MINE_TUNNEL) {
            graphics.drawCenteredString(font, "Tunnel cross-section · inner torches", center, y - 9, 0xB0B0B0);
            int cell = 8;
            int left = center - 4 * cell;
            int top = y + 10;
            for (int row = 0; row < 4; row++) {
                for (int col = 0; col < 9; col++) {
                    boolean wall = row == 0 || row == 3 || col == 0 || col == 8;
                    int color = wall ? 0xFF777777 : row == 2 ? 0xFF6B4930 : 0xFF242424;
                    graphics.fill(left + col * cell, top + row * cell, left + (col + 1) * cell - 1, top + (row + 1) * cell - 1, color);
                }
            }
            graphics.fill(left + 2 * cell, top + cell, left + 2 * cell + cell - 2, top + 2 * cell - 2, 0xFFFF3333);
            graphics.fill(left + 6 * cell, top + cell, left + 6 * cell + cell - 2, top + 2 * cell - 2, 0xFFFF3333);
            return;
        }
        String pattern = switch (type) {
            case SAND_PYRAMID_SMALL, SAND_PYRAMID_MEDIUM, SAND_PYRAMID_LARGE,
                    COBBLE_PYRAMID_SMALL, COBBLE_PYRAMID_MEDIUM, COBBLE_PYRAMID_LARGE -> "    █    /   ███   /  █████  / ███████ /█████████";
            case MINE_TUNNEL -> "";
            case CREEPY_SIGNS -> "[I SEE YOU]  [DO NOT TURN]  [HE IS HERE]";
            case HEROBRINE_TOTEM -> "  F  /  █  /███";
            case WORD_STOP -> "██████  █████  ███████  █████";
            default -> "█  █ /████ /█  █ /█  █";
        };
        int lineY = y;
        for (String line : pattern.split("/")) {
            graphics.drawCenteredString(font, line, center, lineY, 0xC0C0C0);
            lineY += 9;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
