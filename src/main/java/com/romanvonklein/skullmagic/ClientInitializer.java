package com.romanvonklein.skullmagic;

import org.lwjgl.glfw.GLFW;

import com.romanvonklein.skullmagic.essence.ClientEssenceManager;
import com.romanvonklein.skullmagic.hud.EssenceStatus;
import com.romanvonklein.skullmagic.networking.ClientPackageReceiver;
import com.romanvonklein.skullmagic.networking.ClientPackageSender;
import com.romanvonklein.skullmagic.networking.NetworkingConstants;
import com.romanvonklein.skullmagic.spells.ClientSpellManager;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class ClientInitializer implements ClientModInitializer {
    // keybindings
    private static KeyBinding primarySpellKeyBinding;
    private static KeyBinding cycleSpellKeyBinding;
    private static ClientEssenceManager clientEssenceManager;
    private static ClientSpellManager clientSpellManager;

    @Override
    public void onInitializeClient() {
        // keybinds
        primarySpellKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.skullmagic.primary", // The translation key of the keybinding's name
                InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_R, // The keycode of the key
                "category.skullmagic.spells" // The translation key of the keybinding's category.
        ));
        cycleSpellKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.skullmagic.cycle", // The translation key of the keybinding's name
                InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_G, // The keycode of the key
                "category.skullmagic.spells" // The translation key of the keybinding's category.
        ));

        // register action for keybind
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (primarySpellKeyBinding.wasPressed()) {
                ClientPackageSender.sendCastSpellPackage(clientSpellManager.selectedSpellName);
            }
            while (cycleSpellKeyBinding.wasPressed()) {
                clientSpellManager.cycleSpell();
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            clientEssenceManager = null;
        });

        // clientside hud render stuff
        HudRenderCallback.EVENT.register(EssenceStatus::drawEssenceStatus);

        // Networking
        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.ESSENCE_CHARGE_UPDATE_ID,
                ClientPackageReceiver::receiveEssenceChargeUpdate);

        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.UNLINK_ESSENCEPOOL_ID,
                ClientPackageReceiver::receiveUnlikeEssencePoolPacket);

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