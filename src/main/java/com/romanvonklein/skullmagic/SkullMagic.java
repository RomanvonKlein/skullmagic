package com.romanvonklein.skullmagic;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.romanvonklein.skullmagic.blockEntities.FireCannonBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.SkullAltarBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.SkullPedestalBlockEntity;
import com.romanvonklein.skullmagic.blocks.FireCannon;
import com.romanvonklein.skullmagic.blocks.SkullAltar;
import com.romanvonklein.skullmagic.blocks.SkullPedestal;
import com.romanvonklein.skullmagic.commands.Commands;
import com.romanvonklein.skullmagic.entities.EffectBall;
import com.romanvonklein.skullmagic.essence.EssenceManager;
import com.romanvonklein.skullmagic.items.KnowledgeOrb;
import com.romanvonklein.skullmagic.networking.NetworkingConstants;
import com.romanvonklein.skullmagic.spells.SpellManager;
import com.romanvonklein.skullmagic.tasks.TaskManager;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
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

	public static ArrayList<KnowledgeOrb> knowledgeOrbs = new ArrayList<>();

	// block entities
	public static BlockEntityType<SkullAltarBlockEntity> SKULL_ALTAR_BLOCK_ENTITY;
	public static BlockEntityType<SkullPedestalBlockEntity> SKULL_PEDESTAL_BLOCK_ENTITY;
	public static BlockEntityType<FireCannonBlockEntity> FIRE_CANNON_BLOCK_ENTITY;

	// entities
	public static EntityType<EffectBall> EFFECT_BALL;

	// entity renderers
	public static final EntityModelLayer MODEL_EFFECT_BALL_LAYER = new EntityModelLayer(
			new Identifier(MODID, "effectball"), "main");

	// textures
	public static Identifier ESSENCE_BAR_FRAME_TEXTURE;

	// custom managers
	public static EssenceManager essenceManager;
	public static SpellManager spellManager;
	public static TaskManager taskManager;

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

		// register items
		knowledgeOrbs = KnowledgeOrb.generateKnowledgeOrbs();
		for (KnowledgeOrb orb : knowledgeOrbs) {
			Registry.register(Registry.ITEM, new Identifier(MODID, orb.spellName + "_orb"), orb);
		}

		// register entities
		EFFECT_BALL = Registry.register(Registry.ENTITY_TYPE, new Identifier(MODID, "effect_ball"),
				FabricEntityTypeBuilder.create(SpawnGroup.MISC, EffectBall::new)
						.dimensions(EntityDimensions.fixed(0.75f, 0.75f)).build());

		// register entity renderers
		EntityRendererRegistry.register(EFFECT_BALL, (context) -> {
			return new FlyingItemEntityRenderer<EffectBall>(context, 1.0f, false);
		});

		// register textures
		// Registry.register(Registry.)
		ESSENCE_BAR_FRAME_TEXTURE = new Identifier(MODID, "textures/gui/essencebar.png");
		// EntityModelLayerRegistry.registerModelLayer(MODEL_EFFECT_BALL_LAYER,
		// CubeEntityModel::getTexturedModelData);
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

		// TODO: this only applies when the altar is broken by a player - other events
		// isnt this double? as its also in the onBreak methods of the blocks?
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
							spellManager.castSpell(spellname, serverPlayerEntity, serverPlayerEntity.world);
						});
					}
				});
	}
}