package com.romanvonklein.skullmagic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.romanvonklein.skullmagic.blockEntities.FireCannonBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.SkullAltarBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.SkullPedestalBlockEntity;
import com.romanvonklein.skullmagic.blocks.FireCannon;
import com.romanvonklein.skullmagic.blocks.SkullAltar;
import com.romanvonklein.skullmagic.blocks.SkullPedestal;
import com.romanvonklein.skullmagic.commands.Commands;
import com.romanvonklein.skullmagic.networking.NetworkingConstants;
import com.romanvonklein.skullmagic.persistantState.EssenceManager;
import com.romanvonklein.skullmagic.spells.SpellManager;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class SkullMagic implements ModInitializer {
	public static String MODID = "skullmagic";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	// blocks
	public static final Block SkullPedestal = new SkullPedestal(
			FabricBlockSettings.of(Material.METAL).strength(4.0f).requiresTool().nonOpaque());
	public static Block SkullAltar = new SkullAltar(
			FabricBlockSettings.of(Material.METAL).strength(4.0f).requiresTool().nonOpaque());
	public static Block FireCannon = new FireCannon(
			FabricBlockSettings.of(Material.METAL).strength(4.0f).requiresTool().nonOpaque());

	// block entities
	public static BlockEntityType<SkullAltarBlockEntity> SKULL_ALTAR_BLOCK_ENTITY;
	public static BlockEntityType<SkullPedestalBlockEntity> SKULL_PEDESTAL_BLOCK_ENTITY;
	public static BlockEntityType<FireCannonBlockEntity> FIRE_CANNON_BLOCK_ENTITY;

	// custom managers
	public static EssenceManager essenceManager;

	@Override
	public void onInitialize() {
		// register commands
		Commands.registerCommands();

		// register blockentities
		FIRE_CANNON_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, MODID + ":fire_cannon_block_entity",
				FabricBlockEntityTypeBuilder.create(FireCannonBlockEntity::new, FireCannon).build(null));
		SKULL_ALTAR_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, MODID + ":skull_altar_block_entity",
				FabricBlockEntityTypeBuilder.create(SkullAltarBlockEntity::new, SkullAltar).build(null));
		SKULL_PEDESTAL_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE,
				MODID + ":skull_pedestal_block_entity",
				FabricBlockEntityTypeBuilder.create(SkullPedestalBlockEntity::new, SkullPedestal).build(null));

		// register blocks
		Registry.register(Registry.BLOCK, new Identifier(MODID, "fire_cannon"), FireCannon);
		Registry.register(Registry.ITEM, new Identifier(MODID, "fire_cannon"),

				new BlockItem(FireCannon, new FabricItemSettings().group(ItemGroup.MISC)));

		Registry.register(Registry.BLOCK, new Identifier(MODID, "skull_pedestal"), SkullPedestal);
		Registry.register(Registry.ITEM, new Identifier(MODID, "skull_pedestal"),
				new BlockItem(SkullPedestal, new FabricItemSettings().group(ItemGroup.MISC)));

		Registry.register(Registry.BLOCK, new Identifier(MODID, "skull_altar"),
				SkullAltar);
		Registry.register(Registry.ITEM, new Identifier(MODID, "skull_altar"),
				new BlockItem(SkullAltar, new FabricItemSettings().group(ItemGroup.MISC)));
		// register stuff for saving to persistent state manager.
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			LOGGER.info("Initializing Essence Manager");
			essenceManager = (EssenceManager) server.getWorld(World.OVERWORLD).getPersistentStateManager()
					.getOrCreate(EssenceManager::fromNbt, EssenceManager::new, MODID);

		});
		ServerTickEvents.START_SERVER_TICK.register(server -> {
			essenceManager.tick(server);
		});

		// TODO: this only applies when the altar is broken by a player - other events
		// (explosions, ...) might cause trouble
		PlayerBlockBreakEvents.AFTER.register(((world, player, pos, state, entity) -> {
			if (entity != null && entity.getType().equals(SKULL_ALTAR_BLOCK_ENTITY)) {
				// broke a skullAltar
				essenceManager.removeSkullAltar(world, pos);
			} else if (entity != null && entity.getType().equals(SKULL_PEDESTAL_BLOCK_ENTITY)) {
				// broke a skullpedestal
				essenceManager.removePedestal(world.getRegistryKey(), pos);
			}
		}));

		ServerPlayNetworking.registerGlobalReceiver(NetworkingConstants.SPELL_CAST_ID,
				(server, serverPlayerEntity, handler, buf, packetSender) -> {
					String spellname = buf.readString(100);
					if (essenceManager.playerHasEssencePool(serverPlayerEntity.getUuid())) {
						server.execute(() -> {
							SpellManager.castSpell(spellname, serverPlayerEntity, serverPlayerEntity.world);
						});
					}
				});
	}
}
