package com.romanvonklein.skullmagic.networking;

import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.util.Identifier;

public class NetworkingConstants {
        public static final Identifier ESSENCE_CHARGE_UPDATE_ID = new Identifier(
                        SkullMagic.MODID + ":essence_charge_update_id");
        public static final Identifier SPELL_CAST_ID = new Identifier(
                        SkullMagic.MODID + ":spell_cast_id");
        public static final Identifier UNLINK_ESSENCEPOOL_ID = new Identifier(
                        SkullMagic.MODID + ":unlink_essencepool_id");
        public static final Identifier SPELL_CAST_FEEDBACK = new Identifier(SkullMagic.MODID + "spell_cast_feedback");
        public static final Identifier UPDATE_SPELL_LIST = new Identifier(SkullMagic.MODID + "update_spell_list");
        public static final Identifier UPDATE_LINK_LIST = new Identifier(SkullMagic.MODID + "update_link_list");
}
