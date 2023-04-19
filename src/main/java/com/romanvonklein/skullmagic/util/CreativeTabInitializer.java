package com.romanvonklein.skullmagic.util;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

public class CreativeTabInitializer {
    public static void init() {
        for (ItemGroup tab : CreativeTabLists.itemCreativeTabs.keySet()) {
            for (Item item : CreativeTabLists.itemCreativeTabs.get(tab)) {
                ItemGroupEvents.modifyEntriesEvent(tab).register(entries -> entries.add(item));
            }
        }
    }
}
