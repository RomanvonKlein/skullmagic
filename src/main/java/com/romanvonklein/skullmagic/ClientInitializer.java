package com.romanvonklein.skullmagic;

import java.util.HashMap;
import java.util.Map.Entry;

import org.lwjgl.glfw.GLFW;

import com.romanvonklein.skullmagic.blockEntities.PowerSpellPedestalBlockEntityRenderer;
import com.romanvonklein.skullmagic.blockEntities.EfficiencySpellPedestalBlockEntityRenderer;
import com.romanvonklein.skullmagic.blockEntities.CooldownSpellPedestalBlockEntityRenderer;
import com.romanvonklein.skullmagic.blockEntities.ItemHolderBlockEntityRendererShrine;
import com.romanvonklein.skullmagic.blockEntities.SkullMagicSkullBlockEntityRenderer;
import com.romanvonklein.skullmagic.entities.EffectBall;
import com.romanvonklein.skullmagic.entities.FireBreath;
import com.romanvonklein.skullmagic.entities.WitherBreath;
import com.romanvonklein.skullmagic.essence.ClientEssenceManager;
import com.romanvonklein.skullmagic.hud.EssenceStatus;
import com.romanvonklein.skullmagic.items.KnowledgeOrb;
import com.romanvonklein.skullmagic.networking.ClientPackageReceiver;
import com.romanvonklein.skullmagic.networking.ClientPackageSender;
import com.romanvonklein.skullmagic.networking.NetworkingConstants;
import com.romanvonklein.skullmagic.screen.BlockPlacerScreen;
import com.romanvonklein.skullmagic.spells.ClientSpellManager;
import com.romanvonklein.skullmagic.spells.SpellManager;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ClientInitializer implements ClientModInitializer {
    // keybindings
    private static KeyBinding primarySpellKeyBinding;
    private static KeyBinding cycleSpellKeyBinding;

    private static HashMap<String, KeyBinding> spellKeyBindings = new HashMap<String, KeyBinding>();

    private static ClientEssenceManager clientEssenceManager;
    private static ClientSpellManager clientSpellManager;

    // entity renderers
    public static final EntityModelLayer MODEL_EFFECT_BALL_LAYER = new EntityModelLayer(
            new Identifier(SkullMagic.MODID, "effectball"), "main");
    public static final EntityModelLayer MODEL_FIRE_BREATH_LAYER = new EntityModelLayer(
            new Identifier(SkullMagic.MODID, "firebreath"), "main");

    // textures
    public static Identifier ESSENCE_BAR_FRAME_TEXTURE;
    public static Identifier COOLDOWN_BAR_FRAME_TEXTURE;
    public static HashMap<String, Identifier> SPELL_ICONS;

    @Override
    public void onInitializeClient() {
        // keybinds
        primarySpellKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.skullmagic.primary",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.skullmagic.spells"));
        cycleSpellKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.skullmagic.cycle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.skullmagic.spells"));
        // register all spell keybinds (set to none by default)
        for (String spellName : SpellManager.SpellDict.keySet()) {
            spellKeyBindings.put(spellName, KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    "key.skullmagic." + spellName,
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_UNKNOWN,
                    "category.skullmagic.spells")));
        }

        // register action for keybind
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (primarySpellKeyBinding.wasPressed()) {
                ClientPackageSender.sendCastSpellPackage(clientSpellManager.selectedSpellName);
            }
            while (cycleSpellKeyBinding.wasPressed()) {
                if (ClientInitializer.getClientEssenceManager() != null) {
                    clientSpellManager.cycleSpell();
                    client.player.sendMessage(Text.of(clientSpellManager.selectedSpellName), true);
                }
            }
            for (Entry<String, KeyBinding> spellEntry : spellKeyBindings.entrySet()) {
                while (spellEntry.getValue().wasPressed()) {
                    ClientPackageSender.sendCastSpellPackage(spellEntry.getKey());
                }
            }
            // Draw particles for visualization TODO: check for item held or toggle key
            // pressed
            ClientInitializer.getClientSpellManager().tickParticles(client);
            
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            clientEssenceManager = null;
        });

        // register entity renderers
        EntityRendererRegistry.register(SkullMagic.EFFECT_BALL, (context) -> {
            return new FlyingItemEntityRenderer<EffectBall>(context, 1.0f, false);
        });
        EntityRendererRegistry.register(SkullMagic.FIRE_BREATH, (context) -> {
            return new FlyingItemEntityRenderer<FireBreath>(context, 1.0f, false);
        });
        EntityRendererRegistry.register(SkullMagic.WITHER_BREATH, (context) -> {
            return new FlyingItemEntityRenderer<WitherBreath>(context, 1.0f, false);
        });

        BlockEntityRendererRegistry.register(SkullMagic.SKULL_BLOCK_ENTITY, SkullMagicSkullBlockEntityRenderer::new);
        BlockRenderLayerMap.INSTANCE.putBlock(SkullMagic.CapacityCrystal, RenderLayer.getTranslucent());

        BlockEntityRendererRegistry.register(SkullMagic.POWER_SPELL_PEDESTAL_BLOCK_ENTITY,
                PowerSpellPedestalBlockEntityRenderer::new);

        BlockEntityRendererRegistry.register(SkullMagic.COOLDOWN_SPELL_PEDESTAL_BLOCK_ENTITY,
                CooldownSpellPedestalBlockEntityRenderer::new);

        BlockEntityRendererRegistry.register(SkullMagic.EFFICIENCY_SPELL_PEDESTAL_BLOCK_ENTITY,
                EfficiencySpellPedestalBlockEntityRenderer::new);

        BlockEntityRendererRegistry.register(SkullMagic.SPELL_SHRINE_BLOCK_ENTITY,
                ItemHolderBlockEntityRendererShrine::new);

        // register textures
        ESSENCE_BAR_FRAME_TEXTURE = new Identifier(SkullMagic.MODID, "textures/gui/essencebar.png");
        COOLDOWN_BAR_FRAME_TEXTURE = new Identifier(SkullMagic.MODID, "textures/gui/cooldownbar.png");
        SPELL_ICONS = new HashMap<>();
        for (

        KnowledgeOrb orb : SkullMagic.knowledgeOrbs) {
            SPELL_ICONS.put(orb.spellName,
                    new Identifier(SkullMagic.MODID, "textures/gui/" + orb.spellName + "_icon.png"));
        }

        // register particles
        ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
                .register(((atlasTexture, registry) -> {
                    registry.register(new Identifier(SkullMagic.MODID, "particle/link_particle"));
                }));

        ParticleFactoryRegistry.getInstance().register(SkullMagic.LINK_PARTICLE, FlameParticle.Factory::new);
        // screenstuff
        HandledScreens.register(SkullMagic.BLOCK_PLACER_SCREEN_HANDLER, BlockPlacerScreen::new);
        // clientside hud render stuff
        HudRenderCallback.EVENT.register(EssenceStatus::drawEssenceStatus);

        // Networking
        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.ESSENCE_CHARGE_UPDATE_ID,
                ClientPackageReceiver::receiveEssenceChargeUpdate);

        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.UNLINK_ESSENCEPOOL_ID,
                ClientPackageReceiver::receiveUnlinkEssencePoolPacket);

        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.UPDATE_SPELL_LIST,
                ClientPackageReceiver::receiveUpdateSpellListPackage);
    }

    public static ClientSpellManager getClientSpellManager() {
        if (clientSpellManager == null) {
            clientSpellManager = new ClientSpellManager();
        }
        return clientSpellManager;
    }

    public static ClientEssenceManager getClientEssenceManager() {
        return clientEssenceManager;
    }

    public static void unsetClientEssenceManager() {
        clientEssenceManager = null;
    }

    public static void createClientEssenceManager(int essence, int maxEssence, int essenceChargeRate) {
        clientEssenceManager = new ClientEssenceManager();
        clientEssenceManager.essence = essence;
        clientEssenceManager.maxEssence = maxEssence;
        clientEssenceManager.essenceChargeRate = essenceChargeRate;
    }

}