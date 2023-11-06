package com.romanvonklein.skullmagic.util;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;

public class CreativeTabInitializer {
    public static void init() {
        for (ItemGroup tab : CreativeTabLists.itemCreativeTabs.keySet()) {
            for (Item item : CreativeTabLists.itemCreativeTabs.get(tab)) {
                ItemGroupEvents
                        .modifyEntriesEvent(Registries.ITEM_GROUP.getKey(tab).get()).register(content -> {
                            content.add(item);
                        });
            }
        }
    }
}
