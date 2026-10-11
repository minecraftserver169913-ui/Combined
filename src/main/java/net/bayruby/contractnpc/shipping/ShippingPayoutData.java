package net.bayruby.contractnpc.shipping;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public class ShippingPayoutData extends SavedData {
    public static final String DATA_NAME = "contractnpc_shipping_payout";

    public static final Factory<ShippingPayoutData> FACTORY =
            new Factory<>(
                    ShippingPayoutData::new,
                    ShippingPayoutData::load,
                    DataFixTypes.LEVEL
            );

    private long lastProcessedDay = Long.MIN_VALUE;

    public static ShippingPayoutData load(
            CompoundTag tag,
            HolderLookup.Provider provider
    ) {
        ShippingPayoutData data = new ShippingPayoutData();

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
