package com.romanvonklein.skullmagic.networking;

import com.romanvonklein.skullmagic.ClientInitializer;
import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.persistantState.ClientEssenceManager;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

public class ClientPackageReceiver {
    public static void receiveEssenceChargeUpdate(MinecraftClient client, ClientPlayNetworkHandler handler,
            PacketByteBuf buf, PacketSender responseSender) {
        int[] arr = buf.readIntArray(3);
        if (arr.length != 3) {
            SkullMagic.LOGGER.error("message " + NetworkingConstants.ESSENCE_CHARGE_UPDATE_ID
                    + " had wrong number of int parameters: " + arr.length);
        } else {
            client.execute(() -> {
                if (ClientInitializer.clientEssenceManager == null) {
                    ClientInitializer.clientEssenceManager = new ClientEssenceManager();
                }
                ClientInitializer.clientEssenceManager.essence = arr[0];
                ClientInitializer.clientEssenceManager.maxEssence = arr[1];
                ClientInitializer.clientEssenceManager.essenceChargeRate = arr[2];
            });
        }
    }

    public static void receiveUnlikeEssencePoolPacket(MinecraftClient client, ClientPlayNetworkHandler handler,
            PacketByteBuf buf, PacketSender responseSender) {
        ClientInitializer.clientEssenceManager = null;
    }

    public static void receiveUpdateSpellListPackage(MinecraftClient client, ClientPlayNetworkHandler handler,
            PacketByteBuf buf, PacketSender responseSender) {
        ClientInitializer.clientSpellManager.spellList.clear();
        String msgString = buf.readString();
        for (String valuePair : msgString.split(";")) {
            String[] parts = valuePair.split(":");
            ClientInitializer.clientSpellManager.spellList.put(parts[0], Integer.parseInt(parts[1]));
        }
    }
}
