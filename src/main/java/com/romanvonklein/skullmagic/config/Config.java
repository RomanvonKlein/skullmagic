package com.romanvonklein.skullmagic.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.util.registry.Registry;

public class Config {
    private static String configPath = "./Config/skullmagic.json";
    private static Gson gson;
    public static ConfigData data;

    public static class ConfigData {
        public HashMap<String, Map<String, Float>> drops;

        public HashMap<String, Integer> skulls;
        public int scanWidth;
        public int scanHeight;
        public int supplyWidth;
        public int supplyHeight;

        public String[] defaultSpells;

        public int capacityCrystalStrength;

        public int altarCapacity;

        public ConfigData() {
            this.drops = new HashMap<String, Map<String, Float>>();
            this.skulls = new HashMap<String, Integer>();
        }
    }

    public static ConfigData getConfig() {
        if (data == null) {
            gson = new Gson();
            File configFile = new File(configPath);
            if (configFile.exists() && !configFile.isDirectory() && configFile.canRead()) {
                String fileContent;
                try {
                    fileContent = Files.readString(Path.of(configPath));
                    data = gson.fromJson(fileContent, ConfigData.class);

                } catch (IOException e) {
                    SkullMagic.LOGGER.info(
                            "Could not find or read config.json for skullmagic.");
                }
            } else {
                // could not find the file, so using default config.
                data = getDefaultConfigData();
                saveConfig();
            }
        }
        return data;
    }

    private static void saveConfig() {
        try {
            String jsonString = gson.toJson(data);
            FileOutputStream outputStream;
            outputStream = new FileOutputStream(configPath);
            byte[] strToBytes = jsonString.getBytes();
            outputStream.write(strToBytes);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static ConfigData getDefaultConfigData() {
        ConfigData defaultData = new ConfigData();

        // mobdrops
        HashMap<String, Float> zombieDrops = new HashMap<>();
        zombieDrops.put(Registry.ITEM.getId(Items.ZOMBIE_HEAD).toString(), 0.025f);
        defaultData.drops.put(Registry.ENTITY_TYPE.getId(EntityType.ZOMBIE).toString(), zombieDrops);
        HashMap<String, Float> skeletonDrops = new HashMap<>();
        skeletonDrops.put(Registry.ITEM.getId(Items.SKELETON_SKULL).toString(), 0.025f);
        defaultData.drops.put(Registry.ENTITY_TYPE.getId(EntityType.SKELETON).toString(), skeletonDrops);
        HashMap<String, Float> creeperDrops = new HashMap<>();
        creeperDrops.put(Registry.ITEM.getId(Items.CREEPER_HEAD).toString(), 0.025f);
        defaultData.drops.put(Registry.ENTITY_TYPE.getId(EntityType.CREEPER).toString(), creeperDrops);
        HashMap<String, Float> enderDragonDrops = new HashMap<>();
        enderDragonDrops.put(Registry.ITEM.getId(Items.DRAGON_HEAD).toString(), 1.0f);
        defaultData.drops.put(Registry.ENTITY_TYPE.getId(EntityType.ENDER_DRAGON).toString(), enderDragonDrops);
        HashMap<String, Float> spiderDrops = new HashMap<>();
        spiderDrops.put(Registry.ITEM.getId(SkullMagic.SPIDER_HEAD_BLOCK.asItem()).toString(), 0.025f);
        defaultData.drops.put(Registry.ENTITY_TYPE.getId(EntityType.SPIDER).toString(), spiderDrops);
        HashMap<String, Float> blazeDrops = new HashMap<>();
        blazeDrops.put(Registry.ITEM.getId(SkullMagic.BLAZE_HEAD_BLOCK.asItem()).toString(), 0.025f);
        defaultData.drops.put(Registry.ENTITY_TYPE.getId(EntityType.BLAZE).toString(), blazeDrops);
        HashMap<String, Float> endermanDrops = new HashMap<>();
        endermanDrops.put(Registry.ITEM.getId(SkullMagic.ENDERMAN_HEAD_BLOCK.asItem()).toString(), 0.025f);
        defaultData.drops.put(Registry.ENTITY_TYPE.getId(EntityType.ENDERMAN).toString(), endermanDrops);

        // skull values
        defaultData.skulls.put(Registry.ITEM.getId(Items.ZOMBIE_HEAD).toString(), 1);
        defaultData.skulls.put(Registry.ITEM.getId(Items.SKELETON_SKULL).toString(), 1);
        defaultData.skulls.put(Registry.ITEM.getId(Items.CREEPER_HEAD).toString(), 2);
        defaultData.skulls.put(Registry.ITEM.getId(Items.DRAGON_HEAD).toString(), 25);
        defaultData.skulls.put(Registry.ITEM.getId(SkullMagic.SPIDER_HEAD_BLOCK.asItem()).toString(), 1);
        defaultData.skulls.put(Registry.ITEM.getId(SkullMagic.ENDERMAN_HEAD_BLOCK.asItem()).toString(), 3);
        defaultData.skulls.put(Registry.ITEM.getId(SkullMagic.BLAZE_HEAD_BLOCK.asItem()).toString(), 4);
        defaultData.scanHeight = 2;
        defaultData.scanWidth = 5;
        defaultData.supplyWidth = 32;
        defaultData.supplyHeight = 16;
        defaultData.defaultSpells = new String[] {};

        // other values
        defaultData.capacityCrystalStrength = 100;
        defaultData.altarCapacity = 100;

        return defaultData;
    }

    public static String configToString() {
        String output = "";
        if (data == null) {
            output = "Config not yet initialized.";
        } else {
            output = gson.toJson(data);
        }
        return output;
    }
}
