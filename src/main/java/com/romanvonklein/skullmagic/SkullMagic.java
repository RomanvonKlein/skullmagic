package com.romanvonklein.skullmagic;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.romanvonklein.skullmagic.blockEntities.SkullAltarBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.SkullPedestalBlockEntity;
import com.romanvonklein.skullmagic.blocks.SkullAltar;
import com.romanvonklein.skullmagic.blocks.SkullPedestal;
import com.romanvonklein.skullmagic.networking.NetworkingConstants;
import com.romanvonklein.skullmagic.persistantState.ClientEssenceManager;
import com.romanvonklein.skullmagic.persistantState.EssenceManager;
import com.romanvonklein.skullmagic.spells.SpellManager;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

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
	public static EssenceManager essenceManager;

	@Environment(EnvType.CLIENT)
	private static ClientEssenceManager clientEssenceManager;

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
		// register stuff for saving to persistent state manager.
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			LOGGER.info("Initializing Essence Manager");
			essenceManager = (EssenceManager) server.getWorld(World.OVERWORLD).getPersistentStateManager()
					.getOrCreate(EssenceManager::fromNbt, EssenceManager::new, MODID);
		});
		ServerTickEvents.START_SERVER_TICK.register(server -> {
			essenceManager.tick(server);
		});

		// register action for keybind
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (keyBinding.wasPressed()) {
				PacketByteBuf buf = PacketByteBufs.create();
				buf.writeString("fireball");
				ClientPlayNetworking.send(NetworkingConstants.SPELL_CAST_ID, buf);
				// .send(
				// (ServerPlayerEntity)
				// (world.getPlayerByUuid(UUID.fromString(be.linkedPlayerID))),
				// NetworkingConstants.ESSENCE_CHARGE_UPDATE_ID, buf);
			}
		});

		// clientside hud render stuff
		HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
			// collect data to draw for player
			if (clientEssenceManager != null) {
				int borderwidth = 5;
				int barwidth = 100;
				int barheight = 25;
				int pxPerEssence = Math
						.toIntExact(Math.round(100.0 / Double.valueOf(clientEssenceManager.maxEssence)));
				int x = 10;
				int y = 10;
				// border
				drawRect(matrixStack, x - borderwidth, y - borderwidth, barwidth + 2 * borderwidth,
						barheight + 2 * borderwidth, 0xc2c2c2);
				// essence
				drawRect(matrixStack, x, y, clientEssenceManager.essence * pxPerEssence, barheight, 0x114c9e);
				// empty
				drawRect(matrixStack, x + clientEssenceManager.essence * pxPerEssence, y,
						barwidth - clientEssenceManager.essence * pxPerEssence, barheight, 0x787f8a);
			}
		});

		// TODO: this only applies when the altar is broken by a player - other events
		// (explosions, ...) might cause trouble
		PlayerBlockBreakEvents.AFTER.register(((world, player, pos, state, entity) -> {
			if (entity != null && entity.getType().equals(SKULL_ALTAR_BLOCK_ENTITY)) {
				// broke a skullAltar
				essenceManager.removeSkullAltar(world.getRegistryKey(), pos);
			} else if (entity != null && entity.getType().equals(SKULL_PEDESTAL_BLOCK_ENTITY)) {
				// broke a skullpedestal
				essenceManager.removePedestal(world.getRegistryKey(), pos);
				// TODO: also check when skulls are destroyed on top of pedestals...
				// LOGGER.info(entity.toString());
				// LOGGER.info(entity.getType().toString());
			}
		}));

		// Networking
		ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.ESSENCE_CHARGE_UPDATE_ID,
				(client, handler, buf, responseSender) -> {
					int[] arr = buf.readIntArray(3);
					if (arr.length != 3) {
						LOGGER.error("message " + NetworkingConstants.ESSENCE_CHARGE_UPDATE_ID
								+ " had wrong number of int parameters: " + arr.length);
					} else {
						client.execute(() -> {
							if (clientEssenceManager == null) {
								clientEssenceManager = new ClientEssenceManager();
							}
							clientEssenceManager.essence = arr[0];
							clientEssenceManager.maxEssence = arr[1];
							clientEssenceManager.essenceChargeRate = arr[2];
						});
					}
				});

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

	/**
	 * Draws a rectangle on the screen
	 * 
	 * @param posX
	 *               the x positon on the screen
	 * @param posY
	 *               the y positon on the screen
	 * @param width
	 *               the width of the rectangle
	 * @param height
	 *               the height of the rectangle
	 * @param color
	 *               the color of the rectangle
	 */
	private static void drawRect(MatrixStack ms, int posX, int posY, int width, int height, int color) {
		if (color == -1)
			return;
		float f3;
		if (color <= 0xFFFFFF && color >= 0)
			f3 = 1.0F;
		else
			f3 = (color >> 24 & 255) / 255.0F;
		float f = (color >> 16 & 255) / 255.0F;
		float f1 = (color >> 8 & 255) / 255.0F;
		float f2 = (color & 255) / 255.0F;
		RenderSystem.enableBlend();
		RenderSystem.disableTexture();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.disableDepthTest();
		BufferBuilder vertexbuffer = Tessellator.getInstance().getBuffer();
		vertexbuffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		vertexbuffer.vertex(ms.peek().getPositionMatrix(), posX, posY + height, 0).color(f, f1, f2, f3).next();
		vertexbuffer.vertex(ms.peek().getPositionMatrix(), posX + width, posY + height, 0).color(f, f1, f2, f3).next();
		vertexbuffer.vertex(ms.peek().getPositionMatrix(), posX + width, posY, 0).color(f, f1, f2, f3).next();
		vertexbuffer.vertex(ms.peek().getPositionMatrix(), posX, posY, 0).color(f, f1, f2, f3).next();
		vertexbuffer.end();
		BufferRenderer.draw(vertexbuffer);
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
		RenderSystem.enableDepthTest();
	}

}
