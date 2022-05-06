package com.romanvonklein.skullmagic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.block.Blocks;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.util.Identifier;

public class SkullMagic implements ModInitializer {
	public static String MODID = "skullmagic";
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		// No magic constants!
		final Identifier COAL_ORE_LOOT_TABLE_ID = Blocks.COAL_ORE.getLootTableId();
		LootTableLoadingCallback.EVENT.register((resourceManager, lootManager, id, table, setter) -> {
			if (COAL_ORE_LOOT_TABLE_ID.equals(id)) {
				FabricLootPoolBuilder poolBuilder = FabricLootPoolBuilder.builder()
						.rolls(ConstantLootNumberProvider.create(1)) // Same as "rolls": 1 in the loot table json
						.with(ItemEntry.builder(Blocks.ZOMBIE_HEAD));

				table.pool(poolBuilder);
			}
		});
		LOGGER.info("Hello Fabric world!");
	}
}
