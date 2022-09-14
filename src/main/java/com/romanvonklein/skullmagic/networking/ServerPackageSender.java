package com.romanvonklein.skullmagic.networking;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.spells.PlayerSpellData;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class ServerPackageSender {
    public static void sendUpdateSpellListPackage(ServerPlayerEntity player) {
        StringBuilder builder = new StringBuilder();
        PacketByteBuf buf = PacketByteBufs.create();
        // String msg = "";
        SkullMagic.spellManager.availableSpells.get(player.getGameProfile().getId()).entrySet().forEach((entry) -> {
            PlayerSpellData spellData = entry.getValue();
            String appendix = entry.getKey() + ":" + Integer.toString(spellData.cooldownLeft) + ","
                    + Double.toString(spellData.getEfficiencyLevel()) + ","
                    + Double.toString(spellData.getPowerLevel()) + ","
                    + Double.toString(spellData.getCooldownReductionLevel())
                    + ";";
            builder.append(appendix);
        });
        String result = builder.toString();
        buf.writeString(result);
        ServerPlayNetworking.send(player, NetworkingConstants.UPDATE_SPELL_LIST, buf);
        
    }
}
