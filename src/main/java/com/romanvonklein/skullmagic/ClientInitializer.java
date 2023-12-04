package com.romanvonklein.skullmagic;

import java.util.HashMap;
import java.util.Map.Entry;

import org.lwjgl.glfw.GLFW;

import com.romanvonklein.skullmagic.blockEntities.CooldownSpellPedestalBlockEntityRenderer;
import com.romanvonklein.skullmagic.blockEntities.EfficiencySpellPedestalBlockEntityRenderer;
import com.romanvonklein.skullmagic.blockEntities.ItemHolderBlockEntityRendererShrine;
import com.romanvonklein.skullmagic.blockEntities.PowerSpellPedestalBlockEntityRenderer;
import com.romanvonklein.skullmagic.data.ClientData;
import com.romanvonklein.skullmagic.data.ServerData;
import com.romanvonklein.skullmagic.effects.particles.ConnectingEffectParticle;
import com.romanvonklein.skullmagic.effects.particles.EffectController;
import com.romanvonklein.skullmagic.effects.particles.LinkingParticle;
import com.romanvonklein.skullmagic.effects.particles.SimpleEffectParticle;
import com.romanvonklein.skullmagic.effects.particles.SlowingEffectParticle;
import com.romanvonklein.skullmagic.entities.EffectBall;
import com.romanvonklein.skullmagic.entities.FireBreath;
import com.romanvonklein.skullmagic.entities.WitherBreath;
import com.romanvonklein.skullmagic.hud.EssenceStatusHud;
import com.romanvonklein.skullmagic.items.KnowledgeOrb;
import com.romanvonklein.skullmagic.networking.ClientPackageReceiver;
import com.romanvonklein.skullmagic.networking.ClientPackageSender;
import com.romanvonklein.skullmagic.networking.NetworkingConstants;
import com.romanvonklein.skullmagic.screen.BlockPlacerScreen;
import com.romanvonklein.skullmagic.util.CreativeTabInitializer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
// import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ClientInitializer implements ClientModInitializer {
        // keybindings
        private static KeyBinding primarySpellKeyBinding;
        private static KeyBinding cycleSpellKeyBinding;
        private static KeyBinding toggleSpellAutoCastKeyBinding;

        private static HashMap<String, KeyBinding> spellKeyBindings = new HashMap<String, KeyBinding>();

        private static ClientData clientData;
        private static EffectController effectController;

        // entity renderers
        public static final EntityModelLayer MODEL_EFFECT_BALL_LAYER = new EntityModelLayer(
                        new Identifier(SkullMagic.MODID, "effectball"), "main");
        public static final EntityModelLayer MODEL_FIRE_BREATH_LAYER = new EntityModelLayer(
                        new Identifier(SkullMagic.MODID, "firebreath"), "main");
        // textures
        public static Identifier ESSENCE_BAR_FRAME_TEXTURE = new Identifier(SkullMagic.MODID,
                        "textures/gui/essencebar.png");
        public static Identifier COOLDOWN_BAR_FRAME_TEXTURE = new Identifier(SkullMagic.MODID,
                        "textures/gui/cooldownbar.png");
        public static Identifier AUTOCAST_INDICATOR = new Identifier(SkullMagic.MODID,
                        "textures/gui/autocast_indicator.png");
        public static HashMap<String, Identifier> SPELL_ICONS;

        @Override
        public void onInitializeClient() {
                // effectcontroller
                effectController = new EffectController();
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
                toggleSpellAutoCastKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                                "key.skullmagic.toggleAutoCast",
                                InputUtil.Type.KEYSYM,
                                GLFW.GLFW_KEY_Z,
                                "category.skullmagic.spells"));
                // register all spell keybinds (set to none by default)
                for (String spellName : ServerData.getSpellNames()) {
                        spellKeyBindings.put(spellName, KeyBindingHelper.registerKeyBinding(new KeyBinding(
                                        "key.skullmagic." + spellName,
                                        InputUtil.Type.KEYSYM,
                                        GLFW.GLFW_KEY_UNKNOWN,
                                        "category.skullmagic.spells")));
                }

                // creative inventory tabs
                ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
                        CreativeTabInitializer.init();
                });
                // register action for keybind
                ClientTickEvents.END_CLIENT_TICK.register(client -> {
                        while (primarySpellKeyBinding.wasPressed()) {
                                ClientPackageSender.sendCastSpellPackage(clientData.getSelectedSpellName());
                        }
                        while (cycleSpellKeyBinding.wasPressed()) {
                                if (ClientInitializer.getClientData() != null) {
                                        clientData.cycleSpell(client);
                                        client.player.sendMessage(Text.of(clientData.getSelectedSpellName()), true);
                                }
                        }
                        while (toggleSpellAutoCastKeyBinding.wasPressed()) {
                                if (ClientInitializer.getClientData() != null) {
                                        ClientPackageSender
                                                        .sendToggleAutoCastPackage(clientData.getSelectedSpellName());
                                        client.player.sendMessage(
                                                        Text.of(Text.translatable("skullmagic.message.toggled_autocast").getString()
                                                                        + clientData.getSelectedSpellName()),
                                                        true);
                                        ClientInitializer.getClientData().toggleSpellAutoCast(
                                                        ClientInitializer.getClientData().getSelectedSpellName());
                                        ClientInitializer.getClientData().checkAutoCasts();
                                }
                        }
                        for (Entry<String, KeyBinding> spellEntry : spellKeyBindings.entrySet()) {
                                while (spellEntry.getValue().wasPressed()) {
                                        ClientPackageSender.sendCastSpellPackage(spellEntry.getKey());
                                }
                        }
                        // Draw particles for visualization
                        ClientInitializer.getEffectController().tickParticles(client);

                });
                ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
                        clientData = null;
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

                // TODO: more rotation steps for skulls?
                // BlockEntityRendererFactories.register(SkullMagic.SKULL_BLOCK_ENTITY,
                // SkullMagicSkullBlockEntityRenderer::new);

                BlockRenderLayerMap.INSTANCE.putBlock(SkullMagic.CapacityCrystal, RenderLayer.getTranslucent());
                BlockRenderLayerMap.INSTANCE.putBlock(SkullMagic.SKULLMAGIC_EASY_SPAWNER_BLOCK,
                                RenderLayer.getCutout());
                BlockRenderLayerMap.INSTANCE.putBlock(SkullMagic.SKULLMAGIC_MEDIUM_SPAWNER_BLOCK,
                                RenderLayer.getCutout());
                BlockRenderLayerMap.INSTANCE.putBlock(SkullMagic.SKULLMAGIC_HARD_SPAWNER_BLOCK,
                                RenderLayer.getCutout());

                BlockEntityRendererFactories.register(SkullMagic.POWER_SPELL_PEDESTAL_BLOCK_ENTITY,
                                PowerSpellPedestalBlockEntityRenderer::new);

                BlockEntityRendererFactories.register(SkullMagic.COOLDOWN_SPELL_PEDESTAL_BLOCK_ENTITY,
                                CooldownSpellPedestalBlockEntityRenderer::new);

                BlockEntityRendererFactories.register(SkullMagic.EFFICIENCY_SPELL_PEDESTAL_BLOCK_ENTITY,
                                EfficiencySpellPedestalBlockEntityRenderer::new);

                BlockEntityRendererFactories.register(SkullMagic.SPELL_SHRINE_BLOCK_ENTITY,
                                ItemHolderBlockEntityRendererShrine::new);

                SPELL_ICONS = new HashMap<>();
                for (

                KnowledgeOrb orb : SkullMagic.knowledgeOrbs) {
                        SPELL_ICONS.put(orb.spellName,
                                        new Identifier(SkullMagic.MODID,
                                                        "textures/gui/" + orb.spellName + "_icon.png"));
                }

                // register particles
                /*
                 * ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
                 * .register(((atlasTexture, registry) -> {
                 * registry.register(new Identifier(SkullMagic.MODID,
                 * "particle/link_particle"));
                 * }));
                 */
                ParticleFactoryRegistry.getInstance().register(SkullMagic.LINK_PARTICLE, LinkingParticle.Factory::new);
                ParticleFactoryRegistry.getInstance().register(SkullMagic.SLOWING_EFFECT_PARTICLE,
                                SlowingEffectParticle.Factory::new);
                ParticleFactoryRegistry.getInstance().register(SkullMagic.SIMPLE_EFFECT_PARTICLE,
                                SimpleEffectParticle.Factory::new);
                ParticleFactoryRegistry.getInstance().register(SkullMagic.CONNECTING_EFFECT_PARTICLE,
                                ConnectingEffectParticle.Factory::new);
                ParticleFactoryRegistry.getInstance().register(SkullMagic.DISCONNECTING_EFFECT_PARTICLE,
                                ConnectingEffectParticle.Factory::new);
                // screenstuff
                HandledScreens.register(SkullMagic.BLOCK_PLACER_SCREEN_HANDLER, BlockPlacerScreen::new);
                // clientside hud render stuff

                HudRenderCallback.EVENT.register(new EssenceStatusHud());

                // Networking
                ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.ESSENCE_CHARGE_UPDATE_ID,
                                ClientPackageReceiver::receiveEssenceChargeUpdate);

                ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.UNLINK_ESSENCEPOOL_ID,
                                ClientPackageReceiver::receiveUnlinkEssencePoolPacket);

                ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.UPDATE_PLAYER_DATA,
                                ClientPackageReceiver::receiveUpdatePlayerDataPackage);
                ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.EFFECT_EVENT,
                                ClientPackageReceiver::receiveEffectPackage);
                ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.TARGETED_EFFECT_EVENT,
                                ClientPackageReceiver::receiveTargetedEffectPackage);
                ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.PARTICLE_EFFECT_EVENT,
                                ClientPackageReceiver::receiveParticleEffectPackage);
                ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.CONNECTING_EFFECT_EVENT,
                                ClientPackageReceiver::receiveConnectingEffectPackage);
                ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.DISCONNECTING_EFFECT_EVENT,
                                ClientPackageReceiver::receiveDisconnectingEffectPackage);

                ClientTickEvents.START_CLIENT_TICK.register(client -> {
                        if (client.world != null && clientData != null) {
                                clientData.tick(client);
                        }
                });

        }

        private static EffectController getEffectController() {
                return effectController;
        }

        public static void unsetClientData() {
                clientData = null;
        }

        public static ClientData getClientData() {
                if (clientData == null) {
                        clientData = new ClientData();
                }
                return clientData;
        }

        public static void setClientData(ClientData newData, boolean keepSelectedSpell) {
                if (keepSelectedSpell && clientData != null && clientData.getSelectedSpellName() != null
                                && newData.knowsSpell(clientData.getSelectedSpellName())) {
                        newData.setSelectedSpellName(clientData.getSelectedSpellName());
                }
                clientData = newData;
                // Cast spells on autocast
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.world != null) {
                        clientData.checkAutoCasts();
                }
        }

}