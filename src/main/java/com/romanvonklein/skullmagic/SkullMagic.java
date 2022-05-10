package com.romanvonklein.skullmagic;

import com.romanvonklein.skullmagic.blockEntities.SkullAltarBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.SkullPedestalBlockEntity;
import com.romanvonklein.skullmagic.blocks.SkullAltar;
import com.romanvonklein.skullmagic.blocks.SkullPedestal;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class SkullMagic implements ModInitializer {
	public static String MODID = "skullmagic";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
	public static final Block SkullPedestal = new SkullPedestal(
			FabricBlockSettings.of(Material.METAL).strength(4.0f).requiresTool());
	public static Block SkullAltar = new SkullAltar(
			FabricBlockSettings.of(Material.METAL).strength(4.0f).requiresTool());
	public static BlockEntityType<SkullAltarBlockEntity> SKULL_ALTAR_BLOCK_ENTITY;
	public static BlockEntityType<SkullPedestalBlockEntity> SKULL_PEDESTAL_BLOCK_ENTITY;
	private static KeyBinding keyBinding;

	@Override
	public void onInitialize() {
		Registry.register(Registry.BLOCK, new Identifier(MODID, "skull_pedestal"), SkullPedestal);
		Registry.register(Registry.ITEM, new Identifier(MODID, "skull_pedestal"),
				new BlockItem(SkullPedestal, new FabricItemSettings().group(ItemGroup.MISC)));

		SKULL_ALTAR_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, MODID + ":skull_altar_block_entity",
				FabricBlockEntityTypeBuilder.create(SkullAltarBlockEntity::new, SkullAltar).build(null));
		SKULL_PEDESTAL_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE,
				MODID + ":skull_pedestal_block_entity",
				FabricBlockEntityTypeBuilder.create(SkullPedestalBlockEntity::new, SkullPedestal).build(null));
		Registry.register(Registry.BLOCK, new Identifier(MODID, "skull_altar"),
				SkullAltar);
		Registry.register(Registry.ITEM, new Identifier(MODID, "skull_altar"),
				new BlockItem(SkullAltar, new FabricItemSettings().group(ItemGroup.MISC)));
		// keybind for primary spell
		keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.skullmagic.primary", // The translation key of the keybinding's name
				InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
				GLFW.GLFW_KEY_R, // The keycode of the key
				"category.skullmagic.spells" // The translation key of the keybinding's category.
		));
		// register action for keybind
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (keyBinding.wasPressed()) {
				client.player.sendMessage(Text.of("currently stored essence for you: "), false);
			}
		});
	}
}
