package com.romanvonklein.skullmagic.structures;

import com.romanvonklein.skullmagic.SkullMagic;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.structure.Structure;

public interface SkullMagicStructureKeys {
    RegistryKey<Structure> DARK_TOWER = of("dark_tower");

    private static RegistryKey<Structure> of(String id) {
        return RegistryKey.of(RegistryKeys.STRUCTURE, new Identifier(SkullMagic.MODID,id));
    }
}
