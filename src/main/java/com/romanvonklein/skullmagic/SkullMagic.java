package com.romanvonklein.skullmagic;

import com.romanvonklein.skullmagic.blocks.SkullPedestal;
import com.romanvonklein.skullmagic.blocks.SkullAltar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class SkullMagic implements ModInitializer {
	public static String MODID = "skullmagic";
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
	public static final Block SkullPedestal = new SkullPedestal(
			FabricBlockSettings.of(Material.METAL).strength(4.0f).requiresTool());
	public static final Block SkullAltar = new SkullAltar(
			FabricBlockSettings.of(Material.METAL).strength(4.0f).requiresTool());

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		// No magic constants!
		Registry.register(Registry.BLOCK, new Identifier(MODID, "skull_pedestal"), SkullPedestal);
		Registry.register(Registry.ITEM, new Identifier(MODID, "skull_pedestal"),
				new BlockItem(SkullPedestal, new FabricItemSettings().group(ItemGroup.MISC)));
		Registry.register(Registry.BLOCK, new Identifier(MODID, "skull_altar"), SkullAltar);
		Registry.register(Registry.ITEM, new Identifier(MODID, "skull_altar"),
				new BlockItem(SkullAltar, new FabricItemSettings().group(ItemGroup.MISC)));

		/*
		 * final Identifier COAL_ORE_LOOT_TABLE_ID = Blocks.COAL_ORE.getLootTableId();
		 * LootTableLoadingCallback.EVENT.register((resourceManager, lootManager, id,
		 * table, setter) -> {
		 * if (COAL_ORE_LOOT_TABLE_ID.equals(id)) {
		 * FabricLootPoolBuilder poolBuilder = FabricLootPoolBuilder.builder()
		 * .rolls(ConstantLootNumberProvider.create(1)) // Same as "rolls": 1 in the
		 * loot table json
		 * .with(ItemEntry.builder(Blocks.ZOMBIE_HEAD));
		 * 
		 * table.pool(poolBuilder);
		 * }
		 * });
		 * LOGGER.info(Config.configToString());
		 * LOGGER.info("Trying to load the config:");
		 * Config.getConfig();
		 * LOGGER.info(Config.configToString());
		 */

	}
}
