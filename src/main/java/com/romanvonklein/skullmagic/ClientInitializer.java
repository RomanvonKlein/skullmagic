package com.romanvonklein.skullmagic;

import org.lwjgl.glfw.GLFW;

import com.romanvonklein.skullmagic.hud.EssenceStatus;
import com.romanvonklein.skullmagic.networking.NetworkingConstants;
import com.romanvonklein.skullmagic.persistantState.ClientEssenceManager;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;

public class ClientInitializer implements ClientModInitializer {
    // keybindings
    private static KeyBinding keyBinding;
    public static ClientEssenceManager clientEssenceManager;

    @Override
    public void onInitializeClient() {
        // keybinds
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.skullmagic.primary", // The translation key of the keybinding's name
                InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_R, // The keycode of the key
                "category.skullmagic.spells" // The translation key of the keybinding's category.
        ));
        // register action for keybind
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.wasPressed()) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeString("fireball");
                ClientPlayNetworking.send(NetworkingConstants.SPELL_CAST_ID, buf);
            }
        });

        // clientside hud render stuff
        HudRenderCallback.EVENT.register(EssenceStatus::drawEssenceStatus);

        // Networking
        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.ESSENCE_CHARGE_UPDATE_ID,
                (client, handler, buf, responseSender) -> {
                    int[] arr = buf.readIntArray(3);
                    if (arr.length != 3) {
                        SkullMagic.LOGGER.error("message " + NetworkingConstants.ESSENCE_CHARGE_UPDATE_ID
                                + " had wrong number of int parameters: " + arr.length);
                    } else {
                        client.execute(() -> {
                            SkullMagic.LOGGER.info("received essece='" + arr[0] + "', maxEssence = '" + arr[1]
                                    + ", and essenceChargeRate= '" + arr[2] + "'");
                            if (clientEssenceManager == null) {
                                clientEssenceManager = new ClientEssenceManager();
                            }
                            clientEssenceManager.essence = arr[0];
                            clientEssenceManager.maxEssence = arr[1];
                            clientEssenceManager.essenceChargeRate = arr[2];
                        });
                    }
                });
        ClientPlayNetworking.registerGlobalReceiver(NetworkingConstants.UNLINK_ESSENCEPOOL_ID,
                (client, handler, buf, responseSender) -> {
                    clientEssenceManager = null;
                });
    }

}