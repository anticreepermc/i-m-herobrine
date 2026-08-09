package com.imherobrine.common;

import com.imherobrine.common.HerobrineAction;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class HerobrineGameLogic {
    private static final ResourceLocation HEALTH_BOOST_ID = ResourceLocation.fromNamespaceAndPath("imherobrine", "extra_hp");
    private static final String NBT_FLY = "imherobrine_survival_fly";
    private static final int BUFF_DURATION_INFINITE = -1;
    private static final int BUFF_AMPLIFIER_STRONG = 9;
    private static final int LEAF_CLEAR_RADIUS = 30;
    private static final int LEAF_CLEAR_RADIUS_SQ = LEAF_CLEAR_RADIUS * LEAF_CLEAR_RADIUS;
    private static final Map<UUID, Boolean> SURVIVAL_FLY = new HashMap<>();

    private HerobrineGameLogic() {
    }

    public static void handleServerAction(ServerPlayer player, HerobrineAction action) {
        switch (action) {
            case LIGHTNING -> spawnLightning(player);
            case APPLY_BUFFS -> applyHerobrineBuffs(player);
            case CLEAR_BUFFS -> clearHerobrineBuffs(player);
            case TOGGLE_FLY -> toggleSurvivalFly(player);
            case CLEAR_NEARBY_LEAVES -> clearNearbyLeaves(player);
        }
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
        int r = LEAF_CLEAR_RADIUS;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dy * dy + dz * dz > LEAF_CLEAR_RADIUS_SQ) {
                        continue;
                    }
                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (state.is(BlockTags.LEAVES)) {
                        level.destroyBlock(pos, false);
                    }
                }
            }
        }
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
        var bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt != null) {
            bolt.setPos(pos.x, pos.y, pos.z);
            level.addFreshEntity(bolt);
        }
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

