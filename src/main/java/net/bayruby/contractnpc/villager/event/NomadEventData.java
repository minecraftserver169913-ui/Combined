package net.bayruby.contractnpc.villager.event;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public class NomadEventData extends SavedData {
    public static final String DATA_NAME = "villager_nomad_event";

    public static final Factory<NomadEventData> FACTORY =
            new Factory<>(
                    NomadEventData::new,
                    NomadEventData::load,
                    DataFixTypes.LEVEL
            );

    private long lastProcessedDay = Long.MIN_VALUE;

    public static NomadEventData load(
            CompoundTag tag,
            HolderLookup.Provider provider
    ) {
        NomadEventData data = new NomadEventData();

        if (tag.contains("LastProcessedDay")) {
            data.lastProcessedDay = tag.getLong("LastProcessedDay");
        }

        return data;
    }

    public boolean markProcessed(long day) {
        if (lastProcessedDay == day) {
            return false;
        }

        lastProcessedDay = day;
        setDirty();
        return true;
    }

    @Override
    public CompoundTag save(
            CompoundTag tag,
            HolderLookup.Provider provider
    ) {
        tag.putLong("LastProcessedDay", lastProcessedDay);
        return tag;
    }
}