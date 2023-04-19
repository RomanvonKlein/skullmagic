package com.romanvonklein.skullmagic.networking;

import com.romanvonklein.skullmagic.ClientInitializer;
import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.data.ClientData;
import com.romanvonklein.skullmagic.effects.CastSpellEffects;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.nbt.NbtCompound;

public class ClientPackageReceiver {

    // TODO: maybe keep this package, as its going to be send ALOT and is better for
    // performance than creating the full playerdata thing every time...
    public static void receiveEssenceChargeUpdate(MinecraftClient client, ClientPlayNetworkHandler handler,
            PacketByteBuf buf, PacketSender responseSender) {
        int[] arr = buf.readIntArray(3);
        if (arr.length != 3) {
            SkullMagic.LOGGER.error("message " + NetworkingConstants.ESSENCE_CHARGE_UPDATE_ID
                    + " had wrong number of int parameters: " + arr.length);
        } else {
            client.execute(() -> {
                ClientData clientData = ClientInitializer.getClientData();
                clientData.setEssence(arr[0]);
                clientData.setMaxEssence(arr[1]);
                clientData.setEssenceChargeRate(arr[2]);
            });
        }
    }

    public static void receiveUnlinkEssencePoolPacket(MinecraftClient client, ClientPlayNetworkHandler handler,
            PacketByteBuf buf, PacketSender responseSender) {
        ClientInitializer.unsetClientData();
    }

    public static void receiveUpdatePlayerDataPackage(MinecraftClient client, ClientPlayNetworkHandler handler,
            PacketByteBuf buf, PacketSender responseSender) {
        ClientInitializer.setClientData(ClientData.fromNbt(buf.readNbt()), true);
    }

    public static void receiveEffectPackage(MinecraftClient client, ClientPlayNetworkHandler handler,
            PacketByteBuf buf, PacketSender responseSender) {
        try {
            NbtCompound nbt = buf.readNbt();
            String spellname = nbt.getString("spellname");
            double spellPower = nbt.getDouble("power");
            double x = nbt.getDouble("x");
            double y = nbt.getDouble("y");
            double z = nbt.getDouble("z");
            String worldkey = nbt.getString("worldkey");
            Vec3d pos = new Vec3d(x, y, z);
            CastSpellEffects.castSpellEffect(client, spellname, worldkey, pos, spellPower);
        } catch (Exception e) {
            SkullMagic.LOGGER.error("Failed parsing effect Package!", e);
        }
    }
}
