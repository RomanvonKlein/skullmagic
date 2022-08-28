package com.romanvonklein.skullmagic.networking;

import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.util.Identifier;

public class NetworkingConstants {
        public static final Identifier ESSENCE_CHARGE_UPDATE_ID = new Identifier(
                        SkullMagic.MODID + ":essence_charge_update_id");
        public static final Identifier SPELL_CAST_ID = new Identifier(
                        SkullMagic.MODID + ":essence_charge_update_id");
        public static final Identifier UNLINK_ESSENCEPOOL_ID = new Identifier(
                        SkullMagic.MODID + ":unlink_essencepool_id");
}
