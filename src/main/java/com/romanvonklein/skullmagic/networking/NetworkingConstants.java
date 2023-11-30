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
        public static final Identifier UPDATE_PLAYER_DATA = new Identifier(SkullMagic.MODID + "update_player_data");
        public static final Identifier EFFECT_EVENT = new Identifier(SkullMagic.MODID + "effect_event");
        public static final Identifier TARGETED_EFFECT_EVENT = new Identifier(
                        SkullMagic.MODID + "targeted_effect_event");
        public static final Identifier PARTICLE_EFFECT_EVENT = new Identifier(
                        SkullMagic.MODID + "particle_effect_event");
        public static final Identifier CONNECTING_EFFECT_EVENT = new Identifier(
                        SkullMagic.MODID + "connecting_effect_event");
        public static final Identifier DISCONNECTING_EFFECT_EVENT = new Identifier(
                        SkullMagic.MODID + "disconnecting_effect_event");
}
