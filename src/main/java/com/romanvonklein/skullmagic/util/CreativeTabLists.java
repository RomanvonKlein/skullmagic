package com.romanvonklein.skullmagic.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CreativeTabLists {
    public static HashMap<ItemGroup, ArrayList<Item>> itemCreativeTabs = new HashMap<>();
    public static ArrayList<ItemGroup> decorationsTabList = new ArrayList<>();
    public static ArrayList<ItemGroup> materialsTabList = new ArrayList<>();
    public static ArrayList<ItemGroup> toolsTabList = new ArrayList<>();
    public static ArrayList<ItemGroup> foodTabList = new ArrayList<>();
    public static ArrayList<ItemGroup> combatTabList = new ArrayList<>();
    public static ArrayList<ItemGroup> buildingBlocksTabList = new ArrayList<>();
    public static ArrayList<ItemGroup> functionalTabList = new ArrayList<>();
    private static boolean initialized = false;

    private static void init() {
        decorationsTabList.add(ItemGroups.BUILDING_BLOCKS);
        materialsTabList.add(ItemGroups.INGREDIENTS);
        toolsTabList.add(ItemGroups.TOOLS);
        foodTabList.add(ItemGroups.FOOD_AND_DRINK);
        combatTabList.add(ItemGroups.COMBAT);
        functionalTabList.add(ItemGroups.FUNCTIONAL);
        buildingBlocksTabList.add(ItemGroups.BUILDING_BLOCKS);
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
