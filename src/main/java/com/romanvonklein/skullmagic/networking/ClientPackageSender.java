package com.romanvonklein.skullmagic.networking;

import com.romanvonklein.skullmagic.SkullMagic;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;

public class ClientPackageSender {
    public static void sendCastSpellPackage(String spellName) {

        MinecraftClient client = MinecraftClient.getInstance();
        if (spellName != null && client != null && client.world != null
                && ClientPlayNetworking.canSend(NetworkingConstants.SPELL_CAST_ID)) {
            try {

                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeString(spellName);
                ClientPlayNetworking.send(NetworkingConstants.SPELL_CAST_ID, buf);
            } catch (Error e) {
                SkullMagic.LOGGER.error(
                        "Tried to send a package to server at a invalid time. Should not cause any further trouble.");
                SkullMagic.LOGGER.error(e.getMessage());

            }
        }
    }

    public static void sendToggleAutoCastPackage(String spellName) {
        if (spellName != null && ClientPlayNetworking.canSend(NetworkingConstants.SPELL_CAST_ID)) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(spellName);
            ClientPlayNetworking.send(NetworkingConstants.TOGGLE_AUTOCAST, buf);
        }
    }
}
