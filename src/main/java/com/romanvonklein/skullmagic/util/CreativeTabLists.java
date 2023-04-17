package com.romanvonklein.skullmagic.util;


import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CreativeTabLists {
    public static HashMap<ItemGroup, ArrayList<Item>> itemCreativeTabs;
    public static ArrayList<ItemGroup> decorationsTabList = new ArrayList<>();
    public static ArrayList<ItemGroup> materialsTabList = new ArrayList<>();
    public static ArrayList<ItemGroup> toolsTabList = new ArrayList<>();
    public static ArrayList<ItemGroup> foodTabList = new ArrayList<>();
    public static ArrayList<ItemGroup> combatTabList = new ArrayList<>();
    public static ArrayList<ItemGroup> buildingBlocksTabList = new ArrayList<>();
    public static ArrayList<ItemGroup> miscTabList = new ArrayList<>();

    public static void init() {
        itemCreativeTabs = new HashMap<>();
        // TODO: actually add the decorations tab to the list
        decorationsTabList.add(ItemGroups.BUILDING_BLOCKS);
        // TODO: actually add the materials tab to the list
        materialsTabList.add(ItemGroups.INGREDIENTS);
        toolsTabList.add(ItemGroups.TOOLS);
        foodTabList.add(ItemGroups.FOOD_AND_DRINK);
        combatTabList.add(ItemGroups.COMBAT);
        miscTabList.add(ItemGroups.getDefaultTab());
        buildingBlocksTabList.add(ItemGroups.BUILDING_BLOCKS);
    }
    public static void addItemToTabs(Item item, List<ItemGroup> tabList) {
        for (ItemGroup tab : tabList)
        {
            if (!itemCreativeTabs.containsKey(tab)) {
                itemCreativeTabs.put(tab, new ArrayList<>());
            }
            itemCreativeTabs.get(tab).add(item);
        }
    }
    public static void addItemToTab(Item item, ItemGroup tab) {
        if (!itemCreativeTabs.containsKey(tab)) {
            itemCreativeTabs.put(tab, new ArrayList<>());
        }
        itemCreativeTabs.get(tab).add(item);
    }

}
