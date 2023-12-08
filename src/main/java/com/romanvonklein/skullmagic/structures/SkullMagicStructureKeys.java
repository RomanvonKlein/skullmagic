package com.romanvonklein.skullmagic.structures;

import com.romanvonklein.skullmagic.SkullMagic;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.structure.Structure;

public interface SkullMagicStructureKeys {
    RegistryKey<Structure> BIG_JIGSAW_STRUCUTURE_KEY = of("big_jigsaw_structure");

    private static RegistryKey<Structure> of(String id) {
        return RegistryKey.of(RegistryKeys.STRUCTURE, new Identifier(SkullMagic.MODID, id));
    }
}
