package com.romanvonklein.skullmagic.networking;

import java.util.ArrayList;
import java.util.HashMap;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.essence.EssencePool;
import com.romanvonklein.skullmagic.spells.PlayerSpellData;
import com.romanvonklein.skullmagic.util.Parsing;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

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

    public static void sendUpdateLinksPackage(ServerPlayerEntity player) {

        EssencePool pool = SkullMagic.essenceManager.getEssencePoolForPlayer(player.getUuid());
        if (pool != null) {
            StringBuilder builder = new StringBuilder();
            PacketByteBuf buf = PacketByteBufs.create();

            // Skull Altars
            ArrayList<BlockPos> pedestals = pool.getLinkedPedestals();
            // first, add the altar blockpos
            builder.append(Parsing.blockPosToString(pool.getAltarPos()) + ":");

            // then add all the pedestals assigned to it
            for (BlockPos pedestalPos : pedestals) {
                builder.append(Parsing.blockPosToString(pedestalPos) + ".");
            }

            // Spell Altars
            builder.append(";");

            HashMap<BlockPos, ArrayList<BlockPos>> spellAltars = SkullMagic.spellManager.getAllShrinePools(
                    player.getUuid(),
                    player.getEntityWorld().getRegistryKey());
            boolean first = true;
            for (BlockPos spellAltarPos : spellAltars.keySet()) {
                if (first) {
                    first = false;
                    builder.append(Parsing.blockPosToString(spellAltarPos));
                } else {
                    builder.append("|" + Parsing.blockPosToString(spellAltarPos));
                }
                for (BlockPos pos : spellAltars.get(spellAltarPos)) {
                    builder.append(Parsing.blockPosToString(pos) + ".");
                }
            }

            String result = builder.toString();
            buf.writeString(result);
            ServerPlayNetworking.send(player, NetworkingConstants.UPDATE_LINK_LIST, buf);
        }
    }
}
