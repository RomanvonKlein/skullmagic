package com.romanvonklein.skullmagic.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.util.SpawnerEntry;

import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

public class Config {
    private static String configPath = "./Config/skullmagic.json";
    private static Gson gson;
    public static ConfigData data;

    private Config() {
    }

    public static class ConfigData {
        public Map<String, Map<String, Float>> drops;
        public Map<String, ArrayList<SpawnerEntry>> spawnerSpawns;

        public Map<String, Integer> skulls;
        public Map<String, Integer> shrines;
        public int scanWidth;
        public int scanHeight;
        public int supplyWidth;
        public int supplyHeight;

        public String[] defaultSpells;

        public int capacityCrystalStrength;
        public int altarCapacity;

        public int shrineRangePerLevel;

        public ConfigData() {
            this.drops = new HashMap<>();
            this.skulls = new HashMap<>();
            this.shrines = new HashMap<>();
            this.spawnerSpawns = new HashMap<>();
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
                            "Could not find or read config.json for skullmagic. Creating new one.");
                    data = getDefaultConfigData();
                    saveConfig();
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
        zombieDrops.put(Registries.ITEM.getId(Items.ZOMBIE_HEAD).toString(), 0.025f);
        defaultData.drops.put(Registries.ENTITY_TYPE.getId(EntityType.ZOMBIE).toString(), zombieDrops);
        HashMap<String, Float> skeletonDrops = new HashMap<>();
        skeletonDrops.put(Registries.ITEM.getId(Items.SKELETON_SKULL).toString(), 0.025f);
        defaultData.drops.put(Registries.ENTITY_TYPE.getId(EntityType.SKELETON).toString(), skeletonDrops);
        HashMap<String, Float> creeperDrops = new HashMap<>();
        creeperDrops.put(Registries.ITEM.getId(Items.CREEPER_HEAD).toString(), 0.025f);
        defaultData.drops.put(Registries.ENTITY_TYPE.getId(EntityType.CREEPER).toString(), creeperDrops);
        HashMap<String, Float> enderDragonDrops = new HashMap<>();
        enderDragonDrops.put(Registries.ITEM.getId(Items.DRAGON_HEAD).toString(), 1.0f);
        defaultData.drops.put(Registries.ENTITY_TYPE.getId(EntityType.ENDER_DRAGON).toString(), enderDragonDrops);
        HashMap<String, Float> spiderDrops = new HashMap<>();
        spiderDrops.put(Registries.ITEM.getId(SkullMagic.SPIDER_HEAD_BLOCK.asItem()).toString(), 0.025f);
        defaultData.drops.put(Registries.ENTITY_TYPE.getId(EntityType.SPIDER).toString(), spiderDrops);
        HashMap<String, Float> blazeDrops = new HashMap<>();
        blazeDrops.put(Registries.ITEM.getId(SkullMagic.BLAZE_HEAD_BLOCK.asItem()).toString(), 0.025f);
        defaultData.drops.put(Registries.ENTITY_TYPE.getId(EntityType.BLAZE).toString(), blazeDrops);
        HashMap<String, Float> endermanDrops = new HashMap<>();
        endermanDrops.put(Registries.ITEM.getId(SkullMagic.ENDERMAN_HEAD_BLOCK.asItem()).toString(), 0.025f);
        defaultData.drops.put(Registries.ENTITY_TYPE.getId(EntityType.ENDERMAN).toString(), endermanDrops);

        // skull values
        defaultData.skulls.put(Registries.ITEM.getId(Items.ZOMBIE_HEAD).toString(), 1);
        defaultData.skulls.put(Registries.ITEM.getId(Items.SKELETON_SKULL).toString(), 1);
        defaultData.skulls.put(Registries.ITEM.getId(Items.CREEPER_HEAD).toString(), 2);
        defaultData.skulls.put(Registries.ITEM.getId(Items.DRAGON_HEAD).toString(), 15);
        defaultData.skulls.put(Registries.ITEM.getId(SkullMagic.SPIDER_HEAD_BLOCK.asItem()).toString(), 1);
        defaultData.skulls.put(Registries.ITEM.getId(SkullMagic.ENDERMAN_HEAD_BLOCK.asItem()).toString(), 3);
        defaultData.skulls.put(Registries.ITEM.getId(SkullMagic.BLAZE_HEAD_BLOCK.asItem()).toString(), 4);
        defaultData.skulls.put(Registries.ITEM.getId(Items.WITHER_SKELETON_SKULL.asItem()).toString(), 4);

        // shrine values
        defaultData.shrines.put(Registries.ITEM.getId(SkullMagic.SIMPLE_SPELL_SHRINE.asItem()).toString(), 3);

        defaultData.scanHeight = 2;
        defaultData.scanWidth = 5;
        defaultData.supplyWidth = 32;
        defaultData.supplyHeight = 16;
        defaultData.defaultSpells = new String[] {};

        // other values
        defaultData.capacityCrystalStrength = 500;
        defaultData.altarCapacity = 1000;
        defaultData.shrineRangePerLevel = 5;

        // spawner lists
        defaultData.spawnerSpawns.put("easy", new ArrayList<>());
        defaultData.spawnerSpawns.put("medium", new ArrayList<>());
        defaultData.spawnerSpawns.put("hard", new ArrayList<>());
        SpawnerEntry zombie = new SpawnerEntry("/execute in %s run summon minecraft:zombie %d %d %d", 1, 500, 4, 4);
        defaultData.spawnerSpawns.get("easy").add(zombie);
        defaultData.spawnerSpawns.get("medium").add(zombie);
        defaultData.spawnerSpawns.get("hard").add(zombie);
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
