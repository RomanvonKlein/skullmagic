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

        HashMap<String, Float> zombieDrops = new HashMap<>();
        zombieDrops.put(Registry.ITEM.getId(Items.ZOMBIE_HEAD).toString(), 1.0f);
        defaultData.drops.put(Registry.ENTITY_TYPE.getId(EntityType.ZOMBIE).toString(), zombieDrops);
        HashMap<String, Float> skeletonDrops = new HashMap<>();
        zombieDrops.put(Registry.ITEM.getId(Items.SKELETON_SKULL).toString(), 1.0f);
        defaultData.drops.put(Registry.ENTITY_TYPE.getId(EntityType.SKELETON).toString(), skeletonDrops);
        HashMap<String, Float> creeperDrops = new HashMap<>();
        zombieDrops.put(Registry.ITEM.getId(Items.CREEPER_HEAD).toString(), 1.0f);
        defaultData.drops.put(Registry.ENTITY_TYPE.getId(EntityType.CREEPER).toString(), creeperDrops);

        defaultData.skulls.put(Registry.ITEM.getId(Items.ZOMBIE_HEAD).toString(), 1);
        defaultData.skulls.put(Registry.ITEM.getId(Items.SKELETON_SKULL).toString(), 1);
        defaultData.skulls.put(Registry.ITEM.getId(Items.CREEPER_HEAD).toString(), 3);

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
