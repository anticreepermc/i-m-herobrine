package com.imherobrine.world;

import com.imherobrine.ImHerobrineMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class HerobrineWorldData extends SavedData {
    private static final String FILE_ID = ImHerobrineMod.MODID + "_world";
    private static final String KEY_TOTEM = "totem_placed";
    private static final Codec<HerobrineWorldData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf(KEY_TOTEM, false).forGetter(HerobrineWorldData::isTotemPlaced)
    ).apply(instance, HerobrineWorldData::new));
    private static final SavedDataType<HerobrineWorldData> TYPE = new SavedDataType<>(
            FILE_ID,
            HerobrineWorldData::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    private boolean totemPlaced;

    public static HerobrineWorldData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public HerobrineWorldData() {
    }

    private HerobrineWorldData(boolean totemPlaced) {
        this.totemPlaced = totemPlaced;
    }

    public boolean isTotemPlaced() {
        return totemPlaced;
    }

    public void setTotemPlaced() {
        totemPlaced = true;
        setDirty();
    }
}
