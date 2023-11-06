package com.romanvonklein.skullmagic.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreativeTabLists {
    public static Map<ItemGroup, ArrayList<Item>> itemCreativeTabs = new HashMap<>();
    public static List<ItemGroup> decorationsTabList = new ArrayList<>();
    public static List<ItemGroup> materialsTabList = new ArrayList<>();
    public static List<ItemGroup> toolsTabList = new ArrayList<>();
    public static List<ItemGroup> foodTabList = new ArrayList<>();
    public static List<ItemGroup> combatTabList = new ArrayList<>();
    public static List<ItemGroup> buildingBlocksTabList = new ArrayList<>();
    public static List<ItemGroup> functionalTabList = new ArrayList<>();
    private static boolean initialized = false;

    private static void init() {
        decorationsTabList.add(Registries.ITEM_GROUP.get(ItemGroups.BUILDING_BLOCKS));
        materialsTabList.add(Registries.ITEM_GROUP.get(ItemGroups.INGREDIENTS));
        toolsTabList.add(Registries.ITEM_GROUP.get(ItemGroups.TOOLS));
        foodTabList.add(Registries.ITEM_GROUP.get(ItemGroups.FOOD_AND_DRINK));
        combatTabList.add(Registries.ITEM_GROUP.get(ItemGroups.COMBAT));
        functionalTabList.add(Registries.ITEM_GROUP.get(ItemGroups.FUNCTIONAL));
        buildingBlocksTabList.add(Registries.ITEM_GROUP.get(ItemGroups.BUILDING_BLOCKS));
        initialized = true;
    }

    public static void addItemToTabs(Item item, List<ItemGroup> tabList) {
        if (!initialized) {
            init();
        }
        for (ItemGroup tab : tabList) {
            if (!itemCreativeTabs.containsKey(tab)) {
                itemCreativeTabs.put(tab, new ArrayList<>());
            }
            itemCreativeTabs.get(tab).add(item);
        }
    }

    private static void addItemToTab(Item item, ItemGroup tab) {
        if (!itemCreativeTabs.containsKey(tab)) {
            itemCreativeTabs.put(tab, new ArrayList<>());
        }
        itemCreativeTabs.get(tab).add(item);
    }

}
