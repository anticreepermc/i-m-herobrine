package com.imherobrine.world;

import com.imherobrine.ImHerobrineMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class HerobrineWorldData extends SavedData {
    private static final String FILE_ID = ImHerobrineMod.MODID + "_world";
    private static final String KEY_TOTEM = "totem_placed";

    private boolean totemPlaced;

    public static HerobrineWorldData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(HerobrineWorldData::load, HerobrineWorldData::new, FILE_ID);
    }

    public static HerobrineWorldData load(CompoundTag tag) {
        HerobrineWorldData data = new HerobrineWorldData();
        data.totemPlaced = tag.getBoolean(KEY_TOTEM);
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
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
