package com.romanvonklein.skullmagic;

import com.romanvonklein.skullmagic.blockEntities.SkullAltarBlockEntity;
import com.romanvonklein.skullmagic.blocks.SkullAltar;
import com.romanvonklein.skullmagic.blocks.SkullPedestal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
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
	public static Block SkullAltar = new SkullAltar(
			FabricBlockSettings.of(Material.METAL).strength(4.0f).requiresTool());
	public static BlockEntityType<SkullAltarBlockEntity> SKULL_ALTAR_BLOCK_ENTITY;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		// No magic constants!
		Registry.register(Registry.BLOCK, new Identifier(MODID, "skull_pedestal"), SkullPedestal);
		Registry.register(Registry.ITEM, new Identifier(MODID, "skull_pedestal"),
				new BlockItem(SkullPedestal, new FabricItemSettings().group(ItemGroup.MISC)));

		SKULL_ALTAR_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, MODID + ":skull_altar_block_entity",
				FabricBlockEntityTypeBuilder.create(SkullAltarBlockEntity::new, SkullAltar).build(null));
		 Registry.register(Registry.BLOCK, new Identifier(MODID, "skull_altar"),
		 SkullAltar);
		 Registry.register(Registry.ITEM, new Identifier(MODID, "skull_altar"),
		 new BlockItem(SkullAltar, new FabricItemSettings().group(ItemGroup.MISC)));

	}
}
