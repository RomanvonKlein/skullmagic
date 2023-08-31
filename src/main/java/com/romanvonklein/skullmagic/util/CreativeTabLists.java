package com.romanvonklein.skullmagic.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

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
        decorationsTabList.add(ItemGroup.BUILDING_BLOCKS);
        decorationsTabList.add(ItemGroup.SEARCH);
        materialsTabList.add(ItemGroup.MATERIALS);
        materialsTabList.add(ItemGroup.SEARCH);
        toolsTabList.add(ItemGroup.TOOLS);
        toolsTabList.add(ItemGroup.SEARCH);
        foodTabList.add(ItemGroup.FOOD);
        foodTabList.add(ItemGroup.SEARCH);
        combatTabList.add(ItemGroup.COMBAT);
        combatTabList.add(ItemGroup.SEARCH);
        functionalTabList.add(ItemGroup.REDSTONE);
        functionalTabList.add(ItemGroup.SEARCH);
        buildingBlocksTabList.add(ItemGroup.BUILDING_BLOCKS);
        buildingBlocksTabList.add(ItemGroup.SEARCH);
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
