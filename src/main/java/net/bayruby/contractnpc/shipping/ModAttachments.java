package net.bayruby.contractnpc.shipping;

import net.bayruby.contractnpc.ContractNpc;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(
                    net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.ATTACHMENT_TYPES,
                    ContractNpc.MODID
            );

    public static final DeferredHolder<
            AttachmentType<?>,
            AttachmentType<ShippingInventory>
            > SHIPPING_INVENTORY = ATTACHMENTS.register(
            "shipping_inventory",
            () -> AttachmentType.builder(ShippingInventory::new)
                    .serialize(new IAttachmentSerializer<CompoundTag, ShippingInventory>() {
                        @Override
                        public ShippingInventory read(
                                net.neoforged.neoforge.attachment.IAttachmentHolder holder,
                                CompoundTag tag,
                                net.minecraft.core.HolderLookup.Provider provider
                        ) {
                            ShippingInventory inventory = new ShippingInventory();
                            inventory.load(tag, provider);
                            return inventory;
                        }

                        @Override
                        public CompoundTag write(
                                ShippingInventory inventory,
                                net.minecraft.core.HolderLookup.Provider provider
                        ) {
                            return inventory.save(provider);
                        }
                    })
                    .copyOnDeath()
                    .build()
    );
}