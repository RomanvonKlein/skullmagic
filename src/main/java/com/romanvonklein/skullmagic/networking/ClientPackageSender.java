package com.romanvonklein.skullmagic.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

public class ClientPackageSender {
    public static void sendCastSpellPackage(String spellName) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(spellName);
        ClientPlayNetworking.send(NetworkingConstants.SPELL_CAST_ID, buf);
    }
}
