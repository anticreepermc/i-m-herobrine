package com.imherobrine.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class HerobrineStructureBuilder {
    private static final int MAX_BLOCKS = 12000;

    private HerobrineStructureBuilder() {
    }

    public static void build(ServerPlayer player, HerobrineStructureType type, int rotation) {
        ServerLevel level = (ServerLevel) player.level();
        HitResult hit = player.pick(70, 0, false);
        BlockPos origin = hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK
                ? blockHit.getBlockPos().relative(blockHit.getDirection())
                : player.blockPosition().relative(player.getDirection(), 5);
        if (!level.hasChunkAt(origin) || origin.distSqr(player.blockPosition()) > 70 * 70) {
            player.displayClientMessage(Component.literal("Structure target is out of range."), true);
            return;
        }
        int before = 0;
        switch (type) {
            case SAND_PYRAMID_SMALL -> before = pyramid(level, origin, Blocks.SAND, 5);
            case SAND_PYRAMID_MEDIUM -> before = pyramid(level, origin, Blocks.SAND, 9);
            case SAND_PYRAMID_LARGE -> before = pyramid(level, origin, Blocks.SAND, 13);
            case COBBLE_PYRAMID_SMALL -> before = pyramid(level, origin, Blocks.COBBLESTONE, 5);
            case COBBLE_PYRAMID_MEDIUM -> before = pyramid(level, origin, Blocks.COBBLESTONE, 9);
            case COBBLE_PYRAMID_LARGE -> before = pyramid(level, origin, Blocks.COBBLESTONE, 13);
            case WORD_STOP -> before = pixelText(level, origin, "STOP", rotation);
            case WORD_HEROBRINE -> before = pixelText(level, origin, "HEROBRINE", rotation);
            case LETTER_H, LETTER_E, LETTER_R, LETTER_O, LETTER_B, LETTER_I, LETTER_N -> before = pixelText(level, origin, type.name().substring(7), rotation);
            case MINE_TUNNEL -> before = tunnel(level, origin, rotation);
            case CREEPY_SIGNS -> before = signs(level, origin, rotation);
            case HEROBRINE_TOTEM -> before = totem(level, origin);
        }
        player.displayClientMessage(Component.literal("Built " + type.title() + " (" + before + " blocks)."), true);
    }

    public static int importStructure(ServerPlayer player, String name) {
        if (!name.matches("[a-zA-Z0-9_-]{1,48}")) {
            player.displayClientMessage(Component.literal("Invalid structure name."), false);
            return 0;
        }
        Path folder = player.getServer().getServerDirectory().resolve("config").resolve("imherobrine").resolve("structures").normalize();
        Path file = folder.resolve(name + ".json").normalize();
        if (!file.startsWith(folder)) return 0;
        try {
            if (!Files.isRegularFile(file) || Files.size(file) > 1_000_000L) {
                player.displayClientMessage(Component.literal("Structure file is missing or exceeds 1 MB."), false);
                return 0;
            }
            JsonObject root;
            try (var reader = Files.newBufferedReader(file)) {
                root = JsonParser.parseReader(reader).getAsJsonObject();
            }
            JsonArray size = root.getAsJsonArray("size");
            JsonArray blocks = root.getAsJsonArray("blocks");
            if (size == null || size.size() != 3 || blocks == null || blocks.size() > 4096) return 0;
            int sx = size.get(0).getAsInt(), sy = size.get(1).getAsInt(), sz = size.get(2).getAsInt();
            if (sx < 1 || sy < 1 || sz < 1 || sx > 32 || sy > 32 || sz > 32 || sx * sy * sz > MAX_BLOCKS) return 0;
            HitResult hit = player.pick(70, 0, false);
            BlockPos origin = hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK
                    ? blockHit.getBlockPos().relative(blockHit.getDirection()) : player.blockPosition().relative(player.getDirection(), 5);
            ServerLevel level = (ServerLevel) player.level();
            int placed = 0;
            for (var element : blocks) {
                JsonObject entry = element.getAsJsonObject();
                int x = entry.get("x").getAsInt(), y = entry.get("y").getAsInt(), z = entry.get("z").getAsInt();
                if (x < 0 || x >= sx || y < 0 || y >= sy || z < 0 || z >= sz) continue;
                ResourceLocation id = ResourceLocation.tryParse(entry.get("block").getAsString());
                if (id == null) continue;
                var block = BuiltInRegistries.BLOCK.getOptional(id);
                if (block.isEmpty() || block.get() == Blocks.AIR || block.get() == Blocks.BEDROCK || block.get() == Blocks.COMMAND_BLOCK) continue;
                if (put(level, origin.offset(x, y, z), block.get().defaultBlockState())) placed++;
            }
            player.displayClientMessage(Component.literal("Imported " + placed + " blocks from " + name + "."), false);
            return placed;
        } catch (Exception exception) {
            player.displayClientMessage(Component.literal("Could not read structure JSON: " + exception.getMessage()), false);
            return 0;
        }
    }

    private static int pyramid(ServerLevel level, BlockPos origin, Block block, int width) {
        int count = 0;
        for (int layer = 0; layer < (width + 1) / 2; layer++) {
            int half = width / 2 - layer;
            for (int x = -half; x <= half; x++) {
                for (int z = -half; z <= half; z++) {
                    if (put(level, origin.offset(x, layer, z), block.defaultBlockState())) count++;
                }
            }
        }
        return count;
    }

    private static int pixelText(ServerLevel level, BlockPos origin, String text, int rotation) {
        BlockState block = Blocks.GLOWSTONE.defaultBlockState();
        int count = 0;
        int offset = 0;
        for (char ch : text.toUpperCase().toCharArray()) {
            String[] rows = glyph(ch);
            if (rows == null) { offset += 6; continue; }
            for (int y = 0; y < rows.length; y++) {
                for (int x = 0; x < rows[y].length(); x++) {
                    if (rows[y].charAt(x) != '1') continue;
                    BlockPos pos = rotate(origin, offset + x, 6 - y, rotation);
                    if (put(level, pos, block)) count++;
                }
            }
            offset += 6;
            if (offset > 100) break;
        }
        return count;
    }

    public static String[] glyph(char character) {
        return switch (Character.toUpperCase(character)) {
            case 'H' -> new String[]{"10001", "10001", "10001", "11111", "10001", "10001", "10001"};
            case 'E' -> new String[]{"11111", "10000", "10000", "11110", "10000", "10000", "11111"};
            case 'R' -> new String[]{"11110", "10001", "10001", "11110", "10100", "10010", "10001"};
            case 'O' -> new String[]{"01110", "10001", "10001", "10001", "10001", "10001", "01110"};
            case 'B' -> new String[]{"11110", "10001", "10001", "11110", "10001", "10001", "11110"};
            case 'I' -> new String[]{"11111", "00100", "00100", "00100", "00100", "00100", "11111"};
            case 'N' -> new String[]{"10001", "11001", "11001", "10101", "10011", "10011", "10001"};
            case 'S' -> new String[]{"01111", "10000", "10000", "01110", "00001", "00001", "11110"};
            case 'T' -> new String[]{"11111", "00100", "00100", "00100", "00100", "00100", "00100"};
            case 'P' -> new String[]{"11110", "10001", "10001", "11110", "10000", "10000", "10000"};
            default -> null;
        };
    }

    private static int tunnel(ServerLevel level, BlockPos origin, int rotation) {
        int count = 0;
        for (int length = 0; length < 16; length++) {
            for (int y = -1; y <= 2; y++) {
                for (int side = -1; side <= 2; side++) {
                    BlockPos pos = rotate(origin, length, y, side, rotation);
                    boolean frame = y == -1 || y == 2 || side == -1 || side == 2;
                    if (frame && put(level, pos, Blocks.COBBLESTONE.defaultBlockState())) count++;
                    else if (!frame && put(level, pos, Blocks.AIR.defaultBlockState())) count++;
                }
            }
            BlockPos rail = rotate(origin, length, 0, 0, rotation);
            if (level.getBlockState(rail).isAir() && put(level, rail, Blocks.REDSTONE_WIRE.defaultBlockState())) count++;
            if (length % 4 == 0) {
                BlockPos torchPos = rotate(origin, length, 0, -2, rotation);
                Direction outward = switch (Math.floorMod(rotation, 4)) {
                    case 1 -> Direction.EAST;
                    case 2 -> Direction.SOUTH;
                    case 3 -> Direction.WEST;
                    default -> Direction.NORTH;
                };
                BlockState torch = Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(net.minecraft.world.level.block.RedstoneWallTorchBlock.FACING, outward);
                if (torch.canSurvive(level, torchPos) && put(level, torchPos, torch)) count++;
            }
        }
        return count;
    }

    private static int signs(ServerLevel level, BlockPos origin, int rotation) {
        List<String> lines = List.of("I SEE YOU", "DO NOT TURN", "HE IS HERE");
        int count = 0;
        for (int i = 0; i < lines.size(); i++) {
            BlockPos pos = rotate(origin, i * 2, 0, 0, rotation);
            if (!level.hasChunkAt(pos)) continue;
            BlockState state = Blocks.OAK_SIGN.defaultBlockState();
            if (!put(level, pos, state)) continue;
            count++;
            if (level.getBlockEntity(pos) instanceof SignBlockEntity sign) {
                var text = sign.getFrontText();
                for (int line = 0; line < 4; line++) text = text.setMessage(line, Component.literal(line == 1 ? lines.get(i) : ""));
                sign.setText(text, true);
            }
        }
        return count;
    }

    private static int totem(ServerLevel level, BlockPos origin) {
        int count = 0;
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
            if (put(level, origin.offset(x, 0, z), x == 0 && z == 0 ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : Blocks.GOLD_BLOCK.defaultBlockState())) count++;
        }
        BlockPos top = origin.above();
        if (put(level, top, Blocks.NETHERRACK.defaultBlockState())) count++;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos torch = top.relative(side);
            BlockState torchState = Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(net.minecraft.world.level.block.RedstoneWallTorchBlock.FACING, side);
            if (put(level, torch, torchState)) count++;
        }
        if (put(level, top.above(), Blocks.FIRE.defaultBlockState())) count++;
        return count;
    }

    private static BlockPos rotate(BlockPos origin, int forward, int up, int side, int rotation) {
        return switch (Math.floorMod(rotation, 4)) {
            case 1 -> origin.offset(-side, up, forward);
            case 2 -> origin.offset(-forward, up, -side);
            case 3 -> origin.offset(side, up, -forward);
            default -> origin.offset(forward, up, side);
        };
    }

    private static BlockPos rotate(BlockPos origin, int forward, int up, int rotation) {
        return rotate(origin, forward, up, 0, rotation);
    }

    private static boolean put(ServerLevel level, BlockPos pos, BlockState state) {
        if (!level.hasChunkAt(pos) || pos.getY() <= level.getMinY() || pos.getY() >= level.getMaxY() || !state.canSurvive(level, pos)) return false;
        level.setBlock(pos, state, 3);
        return true;
    }
}
