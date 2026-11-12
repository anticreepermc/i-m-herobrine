package com.imherobrine.common;

import com.imherobrine.common.HerobrineAction;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneWallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class HerobrineGameLogic {
    private static final ResourceLocation HEALTH_BOOST_ID = ResourceLocation.fromNamespaceAndPath("imherobrine", "extra_hp");
    private static final String NBT_FLY = "imherobrine_survival_fly";
    private static final int BUFF_DURATION_INFINITE = -1;
    private static final int BUFF_AMPLIFIER_STRONG = 9;
    private static final double TELEPORT_RANGE = 70.0D;
    private static final Map<UUID, Boolean> SURVIVAL_FLY = new HashMap<>();
    private static final Map<UUID, HerobrineLightningMode> LIGHTNING_MODES = new HashMap<>();
    private static final Map<UUID, HerobrineStructureType> STRUCTURES = new HashMap<>();
    private static final Map<UUID, Integer> STRUCTURE_ROTATIONS = new HashMap<>();

    private HerobrineGameLogic() {
    }

    public static void handleServerAction(ServerPlayer player, HerobrineAction action) {
        if (!HerobrinePermissionConfig.canUse(player, action)) {
            return;
        }
        switch (action) {
            case LIGHTNING -> spawnLightning(player);
            case APPLY_BUFFS -> applyHerobrineBuffs(player);
            case CLEAR_BUFFS -> clearHerobrineBuffs(player);
            case TOGGLE_FLY -> toggleSurvivalFly(player);
            case CLEAR_NEARBY_LEAVES -> clearNearbyLeaves(player);
            case PLACE_TREE_TORCHES -> placeTreeTorches(player);
            case TELEPORT_TO_AIM -> teleportToAim(player);
            case SET_LIGHTNING_SINGLE -> LIGHTNING_MODES.put(player.getUUID(), HerobrineLightningMode.SINGLE);
            case SET_LIGHTNING_BURST -> LIGHTNING_MODES.put(player.getUUID(), HerobrineLightningMode.BURST);
            case SET_LIGHTNING_CHAIN -> LIGHTNING_MODES.put(player.getUUID(), HerobrineLightningMode.CHAIN);
            case SET_LIGHTNING_RING -> LIGHTNING_MODES.put(player.getUUID(), HerobrineLightningMode.RING);
            case SET_LIGHTNING_STORM -> LIGHTNING_MODES.put(player.getUUID(), HerobrineLightningMode.STORM);
            case SET_LIGHTNING_NON_PLAYERS -> LIGHTNING_MODES.put(player.getUUID(), HerobrineLightningMode.NON_PLAYERS);
            case SET_LIGHTNING_ALL_PLAYERS -> LIGHTNING_MODES.put(player.getUUID(), HerobrineLightningMode.ALL_PLAYERS);
            case BUILD_STRUCTURE -> { }
            case SELECT_SAND_PYRAMID_SMALL -> selectStructure(player, HerobrineStructureType.SAND_PYRAMID_SMALL);
            case SELECT_SAND_PYRAMID_MEDIUM -> selectStructure(player, HerobrineStructureType.SAND_PYRAMID_MEDIUM);
            case SELECT_SAND_PYRAMID_LARGE -> selectStructure(player, HerobrineStructureType.SAND_PYRAMID_LARGE);
            case SELECT_COBBLE_PYRAMID_SMALL -> selectStructure(player, HerobrineStructureType.COBBLE_PYRAMID_SMALL);
            case SELECT_COBBLE_PYRAMID_MEDIUM -> selectStructure(player, HerobrineStructureType.COBBLE_PYRAMID_MEDIUM);
            case SELECT_COBBLE_PYRAMID_LARGE -> selectStructure(player, HerobrineStructureType.COBBLE_PYRAMID_LARGE);
            case SELECT_WORD_HEROBRINE -> selectStructure(player, HerobrineStructureType.WORD_HEROBRINE);
            case SELECT_WORD_STOP -> selectStructure(player, HerobrineStructureType.WORD_STOP);
            case SELECT_LETTER_H -> selectStructure(player, HerobrineStructureType.LETTER_H);
            case SELECT_LETTER_E -> selectStructure(player, HerobrineStructureType.LETTER_E);
            case SELECT_LETTER_R -> selectStructure(player, HerobrineStructureType.LETTER_R);
            case SELECT_LETTER_O -> selectStructure(player, HerobrineStructureType.LETTER_O);
            case SELECT_LETTER_B -> selectStructure(player, HerobrineStructureType.LETTER_B);
            case SELECT_LETTER_I -> selectStructure(player, HerobrineStructureType.LETTER_I);
            case SELECT_LETTER_N -> selectStructure(player, HerobrineStructureType.LETTER_N);
            case SELECT_MINE_TUNNEL -> selectStructure(player, HerobrineStructureType.MINE_TUNNEL);
            case SELECT_CREEPY_SIGNS -> selectStructure(player, HerobrineStructureType.CREEPY_SIGNS);
            case SELECT_HEROBRINE_TOTEM -> selectStructure(player, HerobrineStructureType.HEROBRINE_TOTEM);
            case ROTATE_STRUCTURE -> STRUCTURE_ROTATIONS.merge(player.getUUID(), 1, (oldValue, one) -> (oldValue + 1) % 4);
            case RESET_STRUCTURE_ROTATION -> STRUCTURE_ROTATIONS.remove(player.getUUID());
            case BUILD_SELECTED_STRUCTURE -> buildSelectedStructure(player);
        }
    }

    public static void onPlayerTick(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator() || !isSurvivalFlyEnabled(player)) {
            return;
        }
        var abilities = player.getAbilities();
        if (!abilities.mayfly) {
            abilities.mayfly = true;
            player.onUpdateAbilities();
        }
    }

    public static void onPlayerChangedDimension(ServerPlayer player) {
        syncAbilities(player);
    }

    public static void onPlayerRespawn(ServerPlayer player) {
        syncAbilities(player);
    }

    public static void placeTotemIfNeeded(ServerLevel overworld) {
        HerobrineWorldData data = HerobrineWorldData.get(overworld);
        if (data.isTotemPlaced()) {
            return;
        }
        BlockPos spawn = overworld.getSharedSpawnPos();
        BlockPos surface = overworld.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, spawn);
        placeTotem(overworld, surface);
        data.setTotemPlaced();
    }

    private static void placeTotem(ServerLevel level, BlockPos ground) {
        if (level.dimension() != Level.OVERWORLD) {
            return;
        }
        for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                BlockPos pos = ground.offset(dx, 0, dz);
                BlockState state = dx == 0 && dz == 0
                        ? Blocks.MOSSY_COBBLESTONE.defaultBlockState()
                        : Blocks.GOLD_BLOCK.defaultBlockState();
                level.setBlock(pos, state, 3);
            }
        }
        BlockPos aboveCenter = ground.above();
        level.setBlock(aboveCenter, Blocks.NETHERRACK.defaultBlockState(), 3);
        BlockState torch = Blocks.REDSTONE_TORCH.defaultBlockState();
        level.setBlock(aboveCenter.north(), torch, 3);
        level.setBlock(aboveCenter.south(), torch, 3);
        level.setBlock(aboveCenter.west(), torch, 3);
        level.setBlock(aboveCenter.east(), torch, 3);
        level.setBlock(aboveCenter.above(), Blocks.FIRE.defaultBlockState(), 3);
    }

    private static void clearNearbyLeaves(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        BlockPos center = player.blockPosition();
        int r = HerobrinePermissionConfig.leafRadius();
        int radiusSq = r * r;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dy * dy + dz * dz > radiusSq) {
                        continue;
                    }
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (!level.hasChunkAt(pos)) {
                        continue;
                    }
                    BlockState state = level.getBlockState(pos);
                    if (state.is(BlockTags.LEAVES)) {
                        level.destroyBlock(pos, false);
                    }
                }
            }
        }
    }

    private static void placeTreeTorches(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        BlockPos center = player.blockPosition();
        int radius = HerobrinePermissionConfig.treeTorchRadius();
        int radiusSq = radius * radius;
        Set<BlockPos> roots = new HashSet<>();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz > radiusSq) {
                        continue;
                    }
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (!level.hasChunkAt(pos) || !level.getBlockState(pos).is(BlockTags.LOGS)) {
                        continue;
                    }
                    BlockPos root = pos;
                    for (int depth = 0; depth < 64; depth++) {
                        BlockPos below = root.below();
                        if (!level.hasChunkAt(below) || !level.getBlockState(below).is(BlockTags.LOGS)) {
                            break;
                        }
                        root = below;
                    }
                    if (root.distSqr(center) <= radiusSq) {
                        roots.add(root);
                    }
                }
            }
        }

        Set<BlockPos> treeRoots = new HashSet<>();
        for (BlockPos root : roots) {
            if (hasNearbyLeaves(level, root)) {
                treeRoots.add(root);
            }
        }

        Set<BlockPos> remaining = new HashSet<>(treeRoots);
        while (!remaining.isEmpty()) {
            BlockPos first = remaining.iterator().next();
            List<BlockPos> group = collectAdjacentRoots(first, remaining);
            placeTorchesAroundRootGroup(level, center, radiusSq, group);
        }
    }

    private static boolean hasNearbyLeaves(ServerLevel level, BlockPos root) {
        for (int dx = -5; dx <= 5; dx++) {
            for (int dy = 0; dy <= 8; dy++) {
                for (int dz = -5; dz <= 5; dz++) {
                    BlockPos pos = root.offset(dx, dy, dz);
                    if (level.hasChunkAt(pos) && level.getBlockState(pos).is(BlockTags.LEAVES)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static List<BlockPos> collectAdjacentRoots(BlockPos first, Set<BlockPos> remaining) {
        List<BlockPos> group = new ArrayList<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(first);
        remaining.remove(first);
        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            group.add(current);
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos neighbor = current.relative(direction);
                if (remaining.remove(neighbor)) {
                    queue.addLast(neighbor);
                }
            }
        }
        return group;
    }

    private static void placeTorchesAroundRootGroup(ServerLevel level, BlockPos center, int radiusSq, List<BlockPos> roots) {
        double averageX = roots.stream().mapToInt(BlockPos::getX).average().orElse(0);
        double averageZ = roots.stream().mapToInt(BlockPos::getZ).average().orElse(0);
        for (Direction side : Direction.Plane.HORIZONTAL) {
            Comparator<BlockPos> edgeComparator = switch (side) {
                case NORTH -> Comparator.<BlockPos>comparingInt(BlockPos::getZ).thenComparingDouble(pos -> Math.abs(pos.getX() - averageX));
                case SOUTH -> Comparator.<BlockPos>comparingInt(BlockPos::getZ).reversed().thenComparingDouble(pos -> Math.abs(pos.getX() - averageX));
                case WEST -> Comparator.<BlockPos>comparingInt(BlockPos::getX).thenComparingDouble(pos -> Math.abs(pos.getZ() - averageZ));
                case EAST -> Comparator.<BlockPos>comparingInt(BlockPos::getX).reversed().thenComparingDouble(pos -> Math.abs(pos.getZ() - averageZ));
                default -> throw new IllegalStateException("Unexpected direction: " + side);
            };
            BlockPos torchPos = roots.stream().min(edgeComparator).orElseThrow().relative(side);
            if (torchPos.distSqr(center) > radiusSq || !level.hasChunkAt(torchPos) || !level.getBlockState(torchPos).isAir()) {
                continue;
            }
            var torchState = Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.FACING, side);
            if (torchState.canSurvive(level, torchPos)) {
                level.setBlock(torchPos, torchState, 3);
            }
        }
    }

    private static void teleportToAim(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        HitResult hit = player.pick(TELEPORT_RANGE, 0.0F, false);
        BlockPos target;
        if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
            target = blockHit.getBlockPos().relative(blockHit.getDirection());
        } else if (hit instanceof EntityHitResult entityHit && hit.getType() == HitResult.Type.ENTITY) {
            target = entityHit.getEntity().blockPosition();
        } else {
            player.displayClientMessage(Component.literal("Aim at a block or entity within 70 blocks."), true);
            return;
        }

        Vec3 destination = findSafeTeleportPosition(level, player, target);
        if (destination == null) {
            player.displayClientMessage(Component.literal("No safe teleport spot near the target."), true);
            return;
        }
        player.connection.teleport(destination.x, destination.y, destination.z, player.getYRot(), player.getXRot());
    }

    private static Vec3 findSafeTeleportPosition(ServerLevel level, ServerPlayer player, BlockPos target) {
        for (int dy = 0; dy <= 3; dy++) {
            for (int distance = 0; distance <= 3; distance++) {
                for (int dx = -distance; dx <= distance; dx++) {
                    for (int dz = -distance; dz <= distance; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != distance) {
                            continue;
                        }
                        BlockPos feet = target.offset(dx, dy, dz);
                        BlockPos head = feet.above();
                        BlockPos floor = feet.below();
                        if (!level.hasChunkAt(floor) || !level.hasChunkAt(head)) {
                            continue;
                        }
                        BlockState floorState = level.getBlockState(floor);
                        BlockState feetState = level.getBlockState(feet);
                        BlockState headState = level.getBlockState(head);
                        if (!floorState.isFaceSturdy(level, floor, Direction.UP)
                                || !feetState.getCollisionShape(level, feet).isEmpty()
                                || !headState.getCollisionShape(level, head).isEmpty()
                                || !feetState.getFluidState().isEmpty()
                                || !headState.getFluidState().isEmpty()) {
                            continue;
                        }
                        Vec3 destination = Vec3.atBottomCenterOf(feet);
                        if (destination.distanceToSqr(player.position()) > TELEPORT_RANGE * TELEPORT_RANGE) {
                            continue;
                        }
                        AABB destinationBox = player.getBoundingBox().move(destination.subtract(player.position()));
                        if (level.noCollision(player, destinationBox)) {
                            return destination;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static void spawnLightning(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        HitResult hit = player.pick(128.0D, 0.0F, false);
        Vec3 pos;
        if (hit.getType() == HitResult.Type.BLOCK) {
            pos = ((BlockHitResult) hit).getBlockPos().getCenter();
        } else if (hit.getType() == HitResult.Type.ENTITY) {
            pos = ((EntityHitResult) hit).getEntity().position();
        } else {
            pos = player.getEyePosition().add(player.getLookAngle().scale(32.0D));
        }
        switch (LIGHTNING_MODES.getOrDefault(player.getUUID(), HerobrineLightningMode.SINGLE)) {
            case SINGLE -> addLightning(player, pos);
            case BURST -> {
                Vec3 right = new Vec3(-player.getLookAngle().z, 0, player.getLookAngle().x).normalize();
                addLightning(player, pos.add(right.scale(-6)));
                addLightning(player, pos);
                addLightning(player, pos.add(right.scale(6)));
            }
            case CHAIN -> {
                addLightning(player, pos);
                level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                        new AABB(pos, pos).inflate(16), entity -> entity != player && !entity.isSpectator()).stream()
                        .sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(pos)))
                        .limit(4).forEach(entity -> addLightning(player, entity.position()));
            }
            case RING -> {
                for (int i = 0; i < 8; i++) {
                    double angle = Math.PI * 2 * i / 8;
                    addLightning(player, pos.add(Math.cos(angle) * 8, 0, Math.sin(angle) * 8));
                }
            }
            case STORM -> {
                for (int i = 0; i < 8; i++) {
                    double angle = Math.random() * Math.PI * 2;
                    double radius = 5 + Math.random() * 14;
                    addLightning(player, pos.add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius));
                }
            }
            case NON_PLAYERS -> level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                    new AABB(pos, pos).inflate(32), entity -> !(entity instanceof net.minecraft.world.entity.player.Player)
                            && entity.isAlive() && !entity.isSpectator())
                    .forEach(entity -> addLightning(player, entity.position()));
            case ALL_PLAYERS -> level.players().stream()
                    .filter(target -> target != player && target.distanceToSqr(pos.x, pos.y, pos.z) <= 32.0D * 32.0D && !target.isSpectator())
                    .forEach(target -> addLightning(player, target.position()));
        }
    }

    private static void addLightning(ServerPlayer caster, Vec3 pos) {
        ServerLevel level = (ServerLevel) caster.level();
        Vec3 casterPos = caster.position();
        Vec3 away = pos.subtract(casterPos);
        if (away.lengthSqr() < 25.0D) {
            Vec3 direction = away.lengthSqr() < 1.0E-6D
                    ? new Vec3(-caster.getLookAngle().x, 0, -caster.getLookAngle().z).normalize()
                    : away.normalize();
            pos = casterPos.add(direction.scale(5.0D));
        }
        var bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt != null) {
            bolt.setPos(pos.x, pos.y, pos.z);
            level.addFreshEntity(bolt);
        }
    }

    private static void selectStructure(ServerPlayer player, HerobrineStructureType type) {
        STRUCTURES.put(player.getUUID(), type);
    }

    private static void buildSelectedStructure(ServerPlayer player) {
        HerobrineStructureType type = STRUCTURES.get(player.getUUID());
        if (type == null) {
            player.displayClientMessage(Component.literal("Select a structure in the menu first."), true);
            return;
        }
        HerobrineStructureBuilder.build(player, type, STRUCTURE_ROTATIONS.getOrDefault(player.getUUID(), 0));
    }

    private static MobEffectInstance hiddenInfinite(Holder<MobEffect> effect, int amplifier) {
        return new MobEffectInstance(effect, BUFF_DURATION_INFINITE, amplifier, false, false, false);
    }

    private static void applyHerobrineBuffs(ServerPlayer player) {
        player.addEffect(hiddenInfinite(MobEffects.STRENGTH, BUFF_AMPLIFIER_STRONG));
        player.addEffect(hiddenInfinite(MobEffects.RESISTANCE, BUFF_AMPLIFIER_STRONG));
        player.addEffect(hiddenInfinite(MobEffects.FIRE_RESISTANCE, 0));
        player.addEffect(hiddenInfinite(MobEffects.HERO_OF_THE_VILLAGE, BUFF_AMPLIFIER_STRONG));

        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.removeModifier(HEALTH_BOOST_ID);
            maxHealth.addTransientModifier(new AttributeModifier(HEALTH_BOOST_ID, 10.0D, AttributeModifier.Operation.ADD_VALUE));
        }
        float missing = player.getMaxHealth() - player.getHealth();
        if (missing > 0) {
            player.heal(Math.min(missing, 10.0F));
        }
    }

    private static void clearHerobrineBuffs(ServerPlayer player) {
        player.removeAllEffects();
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.removeModifier(HEALTH_BOOST_ID);
        }
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void toggleSurvivalFly(ServerPlayer player) {
        boolean next = !isSurvivalFlyEnabled(player);
        SURVIVAL_FLY.put(player.getUUID(), next);
        syncAbilities(player);
    }

    private static boolean isSurvivalFlyEnabled(ServerPlayer player) {
        return SURVIVAL_FLY.getOrDefault(player.getUUID(), false);
    }

    private static void syncAbilities(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) {
            player.onUpdateAbilities();
            return;
        }
        var abilities = player.getAbilities();
        if (isSurvivalFlyEnabled(player)) {
            abilities.mayfly = true;
        } else {
            abilities.mayfly = false;
            abilities.flying = false;
        }
        player.onUpdateAbilities();
    }
}

