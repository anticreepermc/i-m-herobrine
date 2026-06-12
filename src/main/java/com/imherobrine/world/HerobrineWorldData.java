package com.imherobrine.world;

import com.imherobrine.ImHerobrineMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public class HerobrineWorldData extends SavedData {
    private static final String FILE_ID = ImHerobrineMod.MODID + "_world";
    private static final String KEY_TOTEM = "totem_placed";

    private boolean totemPlaced;

    public static HerobrineWorldData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(factory(), FILE_ID);
    }

    private static SavedData.Factory<HerobrineWorldData> factory() {
        return new SavedData.Factory<>(HerobrineWorldData::new, HerobrineWorldData::load, DataFixTypes.LEVEL);
    }

    public static HerobrineWorldData load(CompoundTag tag, HolderLookup.Provider registries) {
        HerobrineWorldData data = new HerobrineWorldData();
        data.totemPlaced = tag.getBoolean(KEY_TOTEM);
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean(KEY_TOTEM, totemPlaced);
        return tag;
    }

    public boolean isTotemPlaced() {
        return totemPlaced;
    }

    public void setTotemPlaced() {
        totemPlaced = true;
        setDirty();
    }
}
