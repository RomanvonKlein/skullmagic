package com.romanvonklein.skullmagic;

import static net.minecraft.world.gen.feature.Feature.ORE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.romanvonklein.skullmagic.blockEntities.BlockPlacerBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.BlockUserBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.CapacityCrystalBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.CooldownSpellPedestalBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.EfficiencySpellPedestalBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.FireCannonBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.PowerSpellPedestalBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.SkullAltarBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.SkullMagicSkullBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.SkullPedestalBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.SpellShrineBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.WitherEnergyChannelerBlockEntity;
import com.romanvonklein.skullmagic.blocks.AdvancedSpellShrine;
import com.romanvonklein.skullmagic.blocks.BlockPlacer;
import com.romanvonklein.skullmagic.blocks.BlockUser;
import com.romanvonklein.skullmagic.blocks.CapacityCrystal;
import com.romanvonklein.skullmagic.blocks.FireCannon;
import com.romanvonklein.skullmagic.blocks.IntermediateSpellShrine;
import com.romanvonklein.skullmagic.blocks.SimpleCooldownSpellPedestal;
import com.romanvonklein.skullmagic.blocks.SimpleEfficiencySpellPedestal;
import com.romanvonklein.skullmagic.blocks.SimplePowerSpellPedestal;
import com.romanvonklein.skullmagic.blocks.SimpleSpellShrine;
import com.romanvonklein.skullmagic.blocks.SkullAltar;
import com.romanvonklein.skullmagic.blocks.SkullMagicSkullBlock;
import com.romanvonklein.skullmagic.blocks.SkullPedestal;
import com.romanvonklein.skullmagic.blocks.WitherEnergyChanneler;
import com.romanvonklein.skullmagic.commands.Commands;
import com.romanvonklein.skullmagic.data.ServerData;
import com.romanvonklein.skullmagic.entities.EffectBall;
import com.romanvonklein.skullmagic.entities.FireBreath;
import com.romanvonklein.skullmagic.entities.WitherBreath;
import com.romanvonklein.skullmagic.items.KnowledgeOrb;
import com.romanvonklein.skullmagic.lootTablemodifiers.LootTableModifier;
import com.romanvonklein.skullmagic.networking.NetworkingConstants;
import com.romanvonklein.skullmagic.networking.ServerPackageSender;
import com.romanvonklein.skullmagic.screen.BlockPlacerScreenHandler;
import com.romanvonklein.skullmagic.structures.SkullMagicStructureTypes;
import com.romanvonklein.skullmagic.tasks.TaskManager;
import com.romanvonklein.skullmagic.util.CreativeTabLists;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureFlag;
import net.minecraft.resource.featuretoggle.FeatureManager;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.structure.rule.BlockMatchRuleTest;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.placementmodifier.CountPlacementModifier;
import net.minecraft.world.gen.placementmodifier.HeightRangePlacementModifier;
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier;

public class SkullMagic implements ModInitializer {
	public static String MODID = "skullmagic";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	// blocks
	public static final Block BLOCK_USER_BLOCK = new BlockUser(
			FabricBlockSettings.of(Material.METAL).strength(4.0f).nonOpaque());
	public static final Block CapacityCrystal = new CapacityCrystal(
			FabricBlockSettings.of(Material.AMETHYST).strength(4.0f).nonOpaque());
	public static final Block SkullPedestal = new SkullPedestal(
			FabricBlockSettings.of(Material.METAL).strength(4.0f).nonOpaque());
	public static final Block SIMPLE_SPELL_SHRINE = new SimpleSpellShrine(
			FabricBlockSettings.of(Material.METAL).strength(4.0f).nonOpaque());
	public static final Block INTERMEDIATE_SPELL_SHRINE = new IntermediateSpellShrine(
			FabricBlockSettings.of(Material.METAL).strength(4.0f).nonOpaque());
	public static final Block ADVANCED_SPELL_SHRINE = new AdvancedSpellShrine(
			FabricBlockSettings.of(Material.METAL).strength(4.0f).nonOpaque());
	public static final Block SPELL_POWER_PEDESTAL = new SimplePowerSpellPedestal(
			FabricBlockSettings.of(Material.METAL).strength(4.0f).nonOpaque());
	public static final Block SPELL_EFFICIENCY_PEDESTAL = new SimpleEfficiencySpellPedestal(
			FabricBlockSettings.of(Material.METAL).strength(4.0f).nonOpaque());
	public static final Block SPELL_COOLDOWN_PEDESTAL = new SimpleCooldownSpellPedestal(
			FabricBlockSettings.of(Material.METAL).strength(4.0f).nonOpaque());
	public static final Block SkullAltar = new SkullAltar(
			FabricBlockSettings.of(Material.METAL).strength(4.0f).nonOpaque());
	public static final Block FireCannon = new FireCannon(
			FabricBlockSettings.of(Material.METAL).strength(4.0f).nonOpaque());
	public static final Block BlockPlacer = new BlockPlacer(
			FabricBlockSettings.of(Material.METAL).strength(4.0f).nonOpaque());
	public static final Block WitherEnergyChanneler = new WitherEnergyChanneler(
			FabricBlockSettings.of(Material.METAL).strength(4.0f).nonOpaque());
	public static final Block ENDERMAN_HEAD_BLOCK = new SkullMagicSkullBlock(
			SkullMagicSkullBlock.SkullMagicType.ENDERMAN,
			AbstractBlock.Settings.of(Material.DECORATION).strength(1.0f));

	public static final Block SPIDER_HEAD_BLOCK = new SkullMagicSkullBlock(
			SkullMagicSkullBlock.SkullMagicType.SPIDER,
			AbstractBlock.Settings.of(Material.DECORATION).strength(1.0f));
	public static final Block BLAZE_HEAD_BLOCK = new SkullMagicSkullBlock(
			SkullMagicSkullBlock.SkullMagicType.BLAZE,
			AbstractBlock.Settings.of(Material.DECORATION).strength(1.0f));
	public static final Block SKULLIUM_ORE = new Block(FabricBlockSettings.of(Material.STONE).strength(2.0f));
	public static final Block SKULLIUM_BLOCK = new Block(FabricBlockSettings.of(Material.AMETHYST).strength(4.0f));

	// items
	public static ArrayList<KnowledgeOrb> knowledgeOrbs = new ArrayList<>();
	public static final Item SKULLIUM_SHARD = generateItem(new FabricItemSettings(),
			CreativeTabLists.functionalTabList);
	public static final Item SKULL_WAND = generateItem(new FabricItemSettings(), CreativeTabLists.functionalTabList);

	// generation
	public static final RegistryKey<PlacedFeature> SKULLIUM_ORE_PLACED_FEATURE = RegistryKey
			.of(RegistryKeys.PLACED_FEATURE, new Identifier(MODID, "ore_skullium"));

	public static Item generateItem(FabricItemSettings settings, List<ItemGroup> tablList) {
		Item result = new Item(settings);
		CreativeTabLists.addItemToTabs(result, tablList);
		return result;
	}

	public static void registerBlockWithItem(Block block, String id, List<ItemGroup> tabList) {
		Registry.register(Registries.BLOCK, new Identifier(MODID, id), block);
		Item result = Registry.register(Registries.ITEM, new Identifier(MODID, id),
				new BlockItem(block, new FabricItemSettings()));
		CreativeTabLists.addItemToTabs(result, tabList);
	}

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
	public static BlockEntityType<PowerSpellPedestalBlockEntity> POWER_SPELL_PEDESTAL_BLOCK_ENTITY;
	public static BlockEntityType<EfficiencySpellPedestalBlockEntity> EFFICIENCY_SPELL_PEDESTAL_BLOCK_ENTITY;
	public static BlockEntityType<CooldownSpellPedestalBlockEntity> COOLDOWN_SPELL_PEDESTAL_BLOCK_ENTITY;

	// entities
	public static EntityType<EffectBall> EFFECT_BALL;
	public static EntityType<FireBreath> FIRE_BREATH;
	public static EntityType<WitherBreath> WITHER_BREATH;

	// screen handlers
	public static ScreenHandlerType<BlockPlacerScreenHandler> BLOCK_PLACER_SCREEN_HANDLER;

	// ore features
	private static final ConfiguredFeature<?, ?> END_SKULLIUM_ORE_CONFIGURED_FEATURE = new ConfiguredFeature<>(
			ORE,
			new OreFeatureConfig(
					new BlockMatchRuleTest(Blocks.END_STONE),
					SKULLIUM_ORE.getDefaultState(),
					9));

	public static PlacedFeature END_SKULLIUM_ORE_PLACED_FEATURE = new PlacedFeature(
			RegistryEntry.of(END_SKULLIUM_ORE_CONFIGURED_FEATURE),
			Arrays.asList(
					CountPlacementModifier.of(20),
					SquarePlacementModifier.of(),
					HeightRangePlacementModifier.uniform(YOffset.getBottom(), YOffset.fixed(256))));

	// particles
	public static final DefaultParticleType LINK_PARTICLE = FabricParticleTypes
			.simple();
	public static final DefaultParticleType SIMPLE_EFFECT_PARTICLE = FabricParticleTypes
			.simple();

	// custom managers
	private static ServerData serverData;
	public static TaskManager taskManager;

	@Override
	public void onInitialize() {
		// register commands
		Commands.registerCommands();

		// register blockentities
		WITHER_ENERGY_CHANNELER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
				MODID + ":wither_energy_channeler_block_entity",
				FabricBlockEntityTypeBuilder.create(WitherEnergyChannelerBlockEntity::new, WitherEnergyChanneler)
						.build(null));
		POWER_SPELL_PEDESTAL_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
				MODID + ":power_spell_pedestal_block_entity",
				FabricBlockEntityTypeBuilder.create(PowerSpellPedestalBlockEntity::new, SPELL_POWER_PEDESTAL)
						.build(null));
		EFFICIENCY_SPELL_PEDESTAL_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
				MODID + ":efficiency_spell_pedestal_block_entity",
				FabricBlockEntityTypeBuilder.create(EfficiencySpellPedestalBlockEntity::new, SPELL_EFFICIENCY_PEDESTAL)
						.build(null));
		COOLDOWN_SPELL_PEDESTAL_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
				MODID + ":cooldown_spell_pedestal_block_entity",
				FabricBlockEntityTypeBuilder.create(CooldownSpellPedestalBlockEntity::new, SPELL_COOLDOWN_PEDESTAL)
						.build(null));
		SPELL_SHRINE_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
				MODID + ":spell_shrine_block_entity",
				FabricBlockEntityTypeBuilder.create(SpellShrineBlockEntity::new, SIMPLE_SPELL_SHRINE,
						INTERMEDIATE_SPELL_SHRINE, ADVANCED_SPELL_SHRINE).build(null));
		BLOCK_USER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
				MODID + ":block_user_block_entity",
				FabricBlockEntityTypeBuilder.create(BlockUserBlockEntity::new, BLOCK_USER_BLOCK).build(null));
		CAPACITY_CRYSTAL_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
				MODID + ":capacity_crystal_block_entity",
				FabricBlockEntityTypeBuilder.create(CapacityCrystalBlockEntity::new, CapacityCrystal).build(null));
		FIRE_CANNON_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, MODID + ":fire_cannon_block_entity",
				FabricBlockEntityTypeBuilder.create(FireCannonBlockEntity::new, FireCannon).build(null));
		BLOCK_PLACER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
				MODID + ":block_placer_block_entity",
				FabricBlockEntityTypeBuilder.create(BlockPlacerBlockEntity::new, BlockPlacer).build(null));
		SKULL_ALTAR_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, MODID + ":skull_altar_block_entity",
				FabricBlockEntityTypeBuilder.create(SkullAltarBlockEntity::new, SkullAltar).build(null));
		SKULL_PEDESTAL_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
				MODID + ":skull_pedestal_block_entity",
				FabricBlockEntityTypeBuilder.create(SkullPedestalBlockEntity::new, SkullPedestal).build(null));

		SKULL_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
				MODID + ":skull_block_entity",
				FabricBlockEntityTypeBuilder.create(SkullMagicSkullBlockEntity::new, ENDERMAN_HEAD_BLOCK,
						SPIDER_HEAD_BLOCK, BLAZE_HEAD_BLOCK).build(null));

		// register blocks
		registerBlockWithItem(BLOCK_USER_BLOCK, "block_user", CreativeTabLists.functionalTabList);
		registerBlockWithItem(WitherEnergyChanneler, "wither_energy_channeler", CreativeTabLists.functionalTabList);
		registerBlockWithItem(CapacityCrystal, "capacity_crystal", CreativeTabLists.functionalTabList);
		registerBlockWithItem(FireCannon, "fire_cannon", CreativeTabLists.functionalTabList);
		registerBlockWithItem(SkullPedestal, "skull_pedestal", CreativeTabLists.functionalTabList);
		registerBlockWithItem(SkullAltar, "skull_altar", CreativeTabLists.functionalTabList);
		registerBlockWithItem(BlockPlacer, "block_placer", CreativeTabLists.functionalTabList);
		registerBlockWithItem(SPELL_POWER_PEDESTAL, "spell_power_pedestal", CreativeTabLists.functionalTabList);
		registerBlockWithItem(SPELL_EFFICIENCY_PEDESTAL, "spell_efficiency_pedestal",
				CreativeTabLists.functionalTabList);
		registerBlockWithItem(SPELL_COOLDOWN_PEDESTAL, "spell_cooldown_pedestal", CreativeTabLists.functionalTabList);
		registerBlockWithItem(SIMPLE_SPELL_SHRINE, "simple_spell_shrine", CreativeTabLists.functionalTabList);
		registerBlockWithItem(INTERMEDIATE_SPELL_SHRINE, "intermediate_spell_shrine",
				CreativeTabLists.functionalTabList);
		registerBlockWithItem(ADVANCED_SPELL_SHRINE, "advanced_spell_shrine", CreativeTabLists.functionalTabList);
		registerBlockWithItem(ENDERMAN_HEAD_BLOCK, "enderman_head", CreativeTabLists.functionalTabList);
		// registerBlockWithItem(INTERMEDIATE_SPELL_SHRINE,"intermediate_spell_shrine",CreativeTabLists.miscTabList);
		registerBlockWithItem(SPIDER_HEAD_BLOCK, "spider_head", CreativeTabLists.functionalTabList);
		registerBlockWithItem(BLAZE_HEAD_BLOCK, "blaze_head", CreativeTabLists.functionalTabList);
		registerBlockWithItem(SKULLIUM_ORE, "skullium_ore", CreativeTabLists.functionalTabList);
		registerBlockWithItem(SKULLIUM_BLOCK, "skullium_block", CreativeTabLists.functionalTabList);

		// register spells
		ServerData.initSpells();

		// register items
		knowledgeOrbs = KnowledgeOrb.generateKnowledgeOrbs();
		for (KnowledgeOrb orb : knowledgeOrbs) {
			Registry.register(Registries.ITEM, new Identifier(MODID, orb.spellName + "_orb"), orb);
		}
		Registry.register(Registries.ITEM, new Identifier(MODID, "skullium_shard"), SKULLIUM_SHARD);
		Registry.register(Registries.ITEM, new Identifier(MODID, "skull_wand"), SKULL_WAND);

		// register entities
		EFFECT_BALL = Registry.register(Registries.ENTITY_TYPE, new Identifier(MODID, "effect_ball"),
				FabricEntityTypeBuilder.create(SpawnGroup.MISC, EffectBall::new)
						.dimensions(EntityDimensions.fixed(0.75f, 0.75f)).build());
		FIRE_BREATH = Registry.register(Registries.ENTITY_TYPE, new Identifier(MODID, "fire_breath"),
				FabricEntityTypeBuilder.create(SpawnGroup.MISC, FireBreath::new)
						.dimensions(EntityDimensions.fixed(0.1f, 0.1f)).build());
		WITHER_BREATH = Registry.register(Registries.ENTITY_TYPE, new Identifier(MODID, "wither_breath"),
				FabricEntityTypeBuilder.create(SpawnGroup.MISC, WitherBreath::new)
						.dimensions(EntityDimensions.fixed(0.1f, 0.1f)).build());

		// register particles
		Registry.register(Registries.PARTICLE_TYPE, new Identifier(MODID, "link_particle"), LINK_PARTICLE);
		Registry.register(Registries.PARTICLE_TYPE, new Identifier(MODID, "simple_effect_particle"),
				SIMPLE_EFFECT_PARTICLE);
		FeatureManager.Builder builder = new FeatureManager.Builder("skullmagic");
		FeatureFlag base = builder.addFlag(new Identifier(MODID, "block_player_screen"));

		FeatureSet set = FeatureSet.of(base);
		// register screenhandler stuff
		BLOCK_PLACER_SCREEN_HANDLER = Registry.register(
				Registries.SCREEN_HANDLER, new Identifier(MODID, "block_placer_screen_handler"),
				new ScreenHandlerType<>(BlockPlacerScreenHandler::new, set));

		// register stuff for saving to persistent state manager.
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			LOGGER.info("Initializing Server side data");
			serverData = (ServerData) server.getWorld(World.OVERWORLD).getPersistentStateManager()
					.getOrCreate(ServerData::fromNbt, ServerData::new, MODID + "_serverData");
		});
		// init structure types
		SkullMagicStructureTypes.init();

		ServerTickEvents.START_SERVER_TICK.register(server -> {
			serverData.tick(server);
			getTaskManager().tick();
		});
		ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender, server) -> {
			getServerData().createPlayerEntryIfNotExists(serverPlayNetworkHandler.player);
			ServerPackageSender.sendUpdatePlayerDataPackageForPlayer(serverPlayNetworkHandler.player);
		});

		ServerPlayNetworking.registerGlobalReceiver(NetworkingConstants.SPELL_CAST_ID,
				(server, serverPlayerEntity, handler, buf, packetSender) -> {
					String spellname = buf.readString(100);
					if (serverData.playerHasAltar(serverPlayerEntity)) {
						server.execute(() -> {
							serverData.tryCastSpell(spellname, serverPlayerEntity, serverPlayerEntity.world);
						});
					}
				});

		// feature initialization
		// Registry.register(Registries.FEATURE, new Identifier(MODID,
		// "end_skullium_ore"),
		// END_SKULLIUM_ORE_CONFIGURED_FEATURE);
		// Registry.register(Registries.FEATURE, new Identifier(MODID,
		// "end_skullium_ore"),
		// END_SKULLIUM_ORE_PLACED_FEATURE);
		// BiomeModifications.addFeature(BiomeSelectors.foundInTheEnd(),
		// GenerationStep.Feature.UNDERGROUND_ORES,
		// RegistryKey.of(RegistryKeys.PLACED_FEATURE, new Identifier(MODID,
		// "end_skullium_ore")));

		// initialize loot tables
		LootTableModifier.initializeLootTableModifications();
	}

	public TaskManager getTaskManager() {
		if (taskManager == null) {
			taskManager = new TaskManager();
		}
		return taskManager;
	}

	public static ServerData getServerData() {
		return serverData;
	}

	public static void updatePlayer(UUID playerToUpdate) {
		if (playerToUpdate != null) {
			SkullMagic.getServerData().updatePlayer(playerToUpdate);
		}
	}
}