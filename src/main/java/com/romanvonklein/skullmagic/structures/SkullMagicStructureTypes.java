package com.romanvonklein.skullmagic.structures;

import com.mojang.serialization.Codec;
import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.util.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public class SkullMagicStructureTypes<S extends Structure> {
    public static StructureType<BigJigsawStructure> JIGSAW;

    public static void init() {
        JIGSAW = register("big_jigsaw", BigJigsawStructure.CODEC);
    }

    private static <S extends BigJigsawStructure> StructureType<S> register(String id, Codec<S> codec) {
        return Registry.register(Registry.STRUCTURE_TYPE, new Identifier(SkullMagic.MODID, id), () -> {
            return codec;
        });
    }
}
