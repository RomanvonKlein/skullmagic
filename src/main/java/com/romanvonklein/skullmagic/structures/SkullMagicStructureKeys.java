package com.romanvonklein.skullmagic.structures;

import com.romanvonklein.skullmagic.SkullMagic;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.gen.structure.Structure;

public interface SkullMagicStructureKeys {
    RegistryKey<Structure> BIG_JIGSAW_STRUCUTURE_KEY = of("big_jigsaw_structure");

    private static RegistryKey<Structure> of(String id) {
        return RegistryKey.of(Registry.STRUCTURE_KEY, new Identifier(SkullMagic.MODID, id));
    }
}
