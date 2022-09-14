package com.romanvonklein.skullmagic;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.romanvonklein.skullmagic.blockEntities.BlockPlacerBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.BlockUserBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.CapacityCrystalBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.FireCannonBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.SkullAltarBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.SkullMagicSkullBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.SkullPedestalBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.SpellPedestalBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.SpellShrineBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.WitherEnergyChannelerBlockEntity;
import com.romanvonklein.skullmagic.blocks.AdvancedSpellShrine;
import com.romanvonklein.skullmagic.blocks.BlockPlacer;
import com.romanvonklein.skullmagic.blocks.BlockUser;
import com.romanvonklein.skullmagic.blocks.CapacityCrystal;
import com.romanvonklein.skullmagic.blocks.FireCannon;
import com.romanvonklein.skullmagic.blocks.IntermediateSpellShrine;
import com.romanvonklein.skullmagic.blocks.SimpleSpellPedestal;
import com.romanvonklein.skullmagic.blocks.SimpleSpellShrine;
import com.romanvonklein.skullmagic.blocks.SkullAltar;
import com.romanvonklein.skullmagic.blocks.SkullMagicSkullBlock;
import com.romanvonklein.skullmagic.blocks.SkullPedestal;
import com.romanvonklein.skullmagic.blocks.WitherEnergyChanneler;
import com.romanvonklein.skullmagic.commands.Commands;
import com.romanvonklein.skullmagic.entities.EffectBall;
import com.romanvonklein.skullmagic.entities.FireBreath;
import com.romanvonklein.skullmagic.essence.EssenceManager;
import com.romanvonklein.skullmagic.items.KnowledgeOrb;
import com.romanvonklein.skullmagic.networking.NetworkingConstants;
import com.romanvonklein.skullmagic.screen.BlockPlacerScreenHandler;
import com.romanvonklein.skullmagic.spells.SpellManager;
import com.romanvonklein.skullmagic.structurefeatures.DarkTowerFeature;
import com.romanvonklein.skullmagic.tasks.TaskManager;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;

public class SkullMagic implements ModInitializer {
	public static String MODID = "skullmagic";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	// structure features
	public static final Codec<StructurePoolFeatureConfig> SKULLMAGIC_CODEC = RecordCodecBuilder
			.create(instance -> instance
					.group(StructurePool.REGISTRY_CODEC.fieldOf("start_pool")
							.forGetter(t -> ((StructurePoolFeatureConfig) t).getStartPool()),
							Codec.intRange(0, 1000).fieldOf("size")
									.forGetter(StructurePoolFeatureConfig::getSize))
					.apply(instance,
							StructurePoolFeatureConfig::new));
	/*
	 * .create(instance -> instance
	 * .group(((MapCodec) StructurePool.REGISTRY_CODEC.fieldOf("start_pool"))
	 * .forGetter(t -> ((StructurePoolFeatureConfig) t).getStartPool()),
	 * ((MapCodec) Codec.intRange(0, 1000).fieldOf("size"))
	 * .forGetter(t -> {
	 * SkullMagic.LOGGER.info("Getting size for structurePool");
	 * return ((StructurePoolFeatureConfig) t).getSize();
	 * }))
	 * .apply(instance,
	 * (t, u) -> new StructurePoolFeatureConfig(((RegistryEntry<StructurePool>) t),
	 * ((int) u))));
	 */
	public static final StructureFeature<StructurePoolFeatureConfig> DARK_TOWER = Registry.register(
			Registry.STRUCTURE_FEATURE,
			MODID + ":dark_tower",
			new DarkTowerFeature(SKULLMAGIC_CODEC));

	// blocks
	public static final Block BLOCK_USER_BLOCK = new BlockUser(
			FabricBlockSettings.of(Material.METAL).strength(4.0f).nonOpaque());
	public static final Block CapacityCrystal = new CapacityCrystal(
			FabricBlockSettings.of(Material.AMETHYST).strength(4.0f).nonOpaque());
	public static final Block SkullPedestal = new SkullPedestal(
			FabricBlockSettings.of(Material.METAL).strength(4.0f).nonOpaque());
	public static Block SIMPLE_SPELL_SHRINE = new SimpleSpellShrine(
			FabricBlockSettings.of(Material.METAL).strength(4.0f).nonOpaque());
	public static Block INTERMEDIATE_SPELL_SHRINE = new IntermediateSpellShrine(
			FabricBlockSettings.of(Material.METAL).strength(4.0f).nonOpaque());
	public static Block ADVANCED_SPELL_SHRINE = new AdvancedSpellShrine(
			FabricBlockSettings.of(Material.METAL).strength(4.0f).nonOpaque());
	public static Block SPELL_PEDESTAL = new SimpleSpellPedestal(
			FabricBlockSettings.of(Material.METAL).strength(4.0f).nonOpaque());
	public static Block SkullAltar = new SkullAltar(
			FabricBlockSettings.of(Material.METAL).strength(4.0f).nonOpaque());
	public static Block FireCannon = new FireCannon(
			FabricBlockSettings.of(Material.METAL).strength(4.0f).nonOpaque());
	public static Block BlockPlacer = new BlockPlacer(
			FabricBlockSettings.of(Material.METAL).strength(4.0f).nonOpaque());
	public static final Block WitherEnergyChanneler = new WitherEnergyChanneler(
			FabricBlockSettings.of(Material.METAL).strength(4.0f).nonOpaque());
	public static final Block ENDERMAN_HEAD_BLOCK = new SkullMagicSkullBlock(SkullMagicSkullBlock.SkullType.ENDERMAN,
			AbstractBlock.Settings.of(Material.DECORATION).strength(1.0f));
	public static final Block SPIDER_HEAD_BLOCK = new SkullMagicSkullBlock(SkullMagicSkullBlock.SkullType.SPIDER,
			AbstractBlock.Settings.of(Material.DECORATION).strength(1.0f));
	public static final Block BLAZE_HEAD_BLOCK = new SkullMagicSkullBlock(SkullMagicSkullBlock.SkullType.BLAZE,
			AbstractBlock.Settings.of(Material.DECORATION).strength(1.0f));

	public static ArrayList<KnowledgeOrb> knowledgeOrbs = new ArrayList<>();

	// block entities
	public static BlockEntityType<SkullAltarBlockEntity> SKULL_ALTAR_BLOCK_ENTITY;
	public static BlockEntityType<SkullPedestalBlockEntity> SKULL_PEDESTAL_BLOCK_ENTITY;
	public static BlockEntityType<FireCannonBlockEntity> FIRE_CANNON_BLOCK_ENTITY;
	public static BlockEntityType<BlockPlacerBlockEntity> BLOCK_PLACER_BLOCK_ENTITY;
	public static BlockEntityType<SkullMagicSkullBlockEntity> SKULL_BLOCK_ENTITY;
	public static BlockEntityType<CapacityCrystalBlockEntity> CAPACITY_CRYSTAL_BLOCK_ENTITY;
	public static BlockEntityType<WitherEnergyChannelerBlockEntity> WITHER_ENERGY_CHANNELER_BLOCK_ENTITY;
	public static BlockEntityType<BlockUserBlockEntity> BLOCK_USER_BLOCK_ENTITY;
	public static BlockEntityType<SpellShrineBlockEntity> SPELL_SHRINE_BLOCK_ENTITY;
	public static BlockEntityType<SpellPedestalBlockEntity> SPELL_PEDESTAL_BLOCK_ENTITY;

	// entities
	public static EntityType<EffectBall> EFFECT_BALL;
	public static EntityType<FireBreath> FIRE_BREATH;

	// screen handlers
	public static ScreenHandlerType<BlockPlacerScreenHandler> BLOCK_PLACER_SCREEN_HANDLER;

	// custom managers
	public static EssenceManager essenceManager;
	public static SpellManager spellManager;
	public static TaskManager taskManager;

	@Override
	public void onInitialize() {
		// register commands
		Commands.registerCommands();

		// register blockentities
		WITHER_ENERGY_CHANNELER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE,
				MODID + ":wither_energy_channeler_block_entity",
				FabricBlockEntityTypeBuilder.create(WitherEnergyChannelerBlockEntity::new, WitherEnergyChanneler)
						.build(null));
		SPELL_PEDESTAL_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE,
				MODID + ":spell_pedestal_block_entity",
				FabricBlockEntityTypeBuilder.create(SpellPedestalBlockEntity::new, SPELL_PEDESTAL).build(null));
		SPELL_SHRINE_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE,
				MODID + ":spell_shrine_block_entity",
				FabricBlockEntityTypeBuilder.create(SpellShrineBlockEntity::new, SIMPLE_SPELL_SHRINE,
						INTERMEDIATE_SPELL_SHRINE, ADVANCED_SPELL_SHRINE).build(null));
		BLOCK_USER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE,
				MODID + ":block_user_block_entity",
				FabricBlockEntityTypeBuilder.create(BlockUserBlockEntity::new, BLOCK_USER_BLOCK).build(null));
		CAPACITY_CRYSTAL_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE,
				MODID + ":capacity_crystal_block_entity",
				FabricBlockEntityTypeBuilder.create(CapacityCrystalBlockEntity::new, CapacityCrystal).build(null));
		FIRE_CANNON_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, MODID + ":fire_cannon_block_entity",
				FabricBlockEntityTypeBuilder.create(FireCannonBlockEntity::new, FireCannon).build(null));
		BLOCK_PLACER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, MODID + ":block_placer_block_entity",
				FabricBlockEntityTypeBuilder.create(BlockPlacerBlockEntity::new, BlockPlacer).build(null));
		SKULL_ALTAR_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, MODID + ":skull_altar_block_entity",
				FabricBlockEntityTypeBuilder.create(SkullAltarBlockEntity::new, SkullAltar).build(null));
		SKULL_PEDESTAL_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE,
				MODID + ":skull_pedestal_block_entity",
				FabricBlockEntityTypeBuilder.create(SkullPedestalBlockEntity::new, SkullPedestal).build(null));

		SKULL_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE,
				MODID + ":skull_block_entity",
				FabricBlockEntityTypeBuilder.create(SkullMagicSkullBlockEntity::new, ENDERMAN_HEAD_BLOCK,
						SPIDER_HEAD_BLOCK, BLAZE_HEAD_BLOCK).build(null));

		// register blocks
		Registry.register(Registry.BLOCK, new Identifier(MODID, "block_user"), BLOCK_USER_BLOCK);
		Registry.register(Registry.ITEM, new Identifier(MODID, "block_user"),
				new BlockItem(BLOCK_USER_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
		Registry.register(Registry.BLOCK, new Identifier(MODID, "wither_energy_channeler"), WitherEnergyChanneler);
		Registry.register(Registry.ITEM, new Identifier(MODID, "wither_energy_channeler"),
				new BlockItem(WitherEnergyChanneler, new FabricItemSettings().group(ItemGroup.MISC)));

		Registry.register(Registry.BLOCK, new Identifier(MODID, "capacity_crystal"), CapacityCrystal);
		Registry.register(Registry.ITEM, new Identifier(MODID, "capacity_crystal"),
				new BlockItem(CapacityCrystal, new FabricItemSettings().group(ItemGroup.MISC)));

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

		Registry.register(Registry.BLOCK, new Identifier(MODID, "block_placer"), BlockPlacer);
		Registry.register(Registry.ITEM, new Identifier(MODID, "block_placer"),
				new BlockItem(BlockPlacer, new FabricItemSettings().group(ItemGroup.MISC)));

		Registry.register(Registry.BLOCK, new Identifier(MODID, "spell_pedestal"), SPELL_PEDESTAL);
		Registry.register(Registry.ITEM, new Identifier(MODID, "spell_pedestal"),
				new BlockItem(SPELL_PEDESTAL, new FabricItemSettings().group(ItemGroup.MISC)));

		Registry.register(Registry.BLOCK, new Identifier(MODID, "simple_spell_shrine"), SIMPLE_SPELL_SHRINE);
		Registry.register(Registry.ITEM, new Identifier(MODID, "simple_spell_shrine"),
				new BlockItem(SIMPLE_SPELL_SHRINE, new FabricItemSettings().group(ItemGroup.MISC)));

		Registry.register(Registry.BLOCK, new Identifier(MODID, "intermediate_spell_shrine"),
				INTERMEDIATE_SPELL_SHRINE);
		Registry.register(Registry.ITEM, new Identifier(MODID, "intermediate_spell_shrine"),
				new BlockItem(INTERMEDIATE_SPELL_SHRINE, new FabricItemSettings().group(ItemGroup.MISC)));

		Registry.register(Registry.BLOCK, new Identifier(MODID, "advanced_spell_shrine"), ADVANCED_SPELL_SHRINE);
		Registry.register(Registry.ITEM, new Identifier(MODID, "advanced_spell_shrine"),
				new BlockItem(ADVANCED_SPELL_SHRINE, new FabricItemSettings().group(ItemGroup.MISC)));

		Registry.register(Registry.BLOCK, new Identifier(MODID, "enderman_head"), ENDERMAN_HEAD_BLOCK);
		Registry.register(Registry.ITEM, new Identifier(MODID, "enderman_head"),
				new BlockItem(ENDERMAN_HEAD_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));

		Registry.register(Registry.BLOCK, new Identifier(MODID, "spider_head"), SPIDER_HEAD_BLOCK);
		Registry.register(Registry.ITEM, new Identifier(MODID, "spider_head"),
				new BlockItem(SPIDER_HEAD_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));

		Registry.register(Registry.BLOCK, new Identifier(MODID, "blaze_head"), BLAZE_HEAD_BLOCK);
		Registry.register(Registry.ITEM, new Identifier(MODID, "blaze_head"),
				new BlockItem(BLAZE_HEAD_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));

		// register items
		knowledgeOrbs = KnowledgeOrb.generateKnowledgeOrbs();
		for (KnowledgeOrb orb : knowledgeOrbs) {
			Registry.register(Registry.ITEM, new Identifier(MODID, orb.spellName + "_orb"), orb);
		}

		// register entities
		EFFECT_BALL = Registry.register(Registry.ENTITY_TYPE, new Identifier(MODID, "effect_ball"),
				FabricEntityTypeBuilder.create(SpawnGroup.MISC, EffectBall::new)
						.dimensions(EntityDimensions.fixed(0.75f, 0.75f)).build());
		FIRE_BREATH = Registry.register(Registry.ENTITY_TYPE, new Identifier(MODID, "fire_breath"),
				FabricEntityTypeBuilder.create(SpawnGroup.MISC, FireBreath::new)
						.dimensions(EntityDimensions.fixed(0.1f, 0.1f)).build());

		// register screenhandler stuff
		BLOCK_PLACER_SCREEN_HANDLER = Registry.register(
				Registry.SCREEN_HANDLER, new Identifier(MODID, "block_placer_screen_handler"),
				new ScreenHandlerType<>((syncId, inventory) -> new BlockPlacerScreenHandler(syncId, inventory)));

		// register stuff for saving to persistent state manager.
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			LOGGER.info("Initializing Essence Manager");
			essenceManager = (EssenceManager) server.getWorld(World.OVERWORLD).getPersistentStateManager()
					.getOrCreate(EssenceManager::fromNbt, EssenceManager::new, MODID + "_essenceManager");
			spellManager = (SpellManager) server.getWorld(World.OVERWORLD).getPersistentStateManager()
					.getOrCreate(SpellManager::fromNbt, SpellManager::new, MODID + "_spellManager");
			taskManager = new TaskManager();
		});

		ServerTickEvents.START_SERVER_TICK.register(server -> {
			essenceManager.tick(server);
			spellManager.tick(server);
			taskManager.tick();
		});
		ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender, server) -> {
			spellManager.playerJoined(serverPlayNetworkHandler.player);
		});

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
					if (essenceManager.playerHasEssencePool(serverPlayerEntity.getGameProfile().getId())) {
						server.execute(() -> {
							spellManager.castSpell(spellname, serverPlayerEntity, serverPlayerEntity.world);
						});
					}
				});
	}
}