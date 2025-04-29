package com.romanvonklein.skullmagic.spells;

import static net.minecraft.structure.pool.StructurePoolBasedGenerator.generate;

import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class DungeonRiseSpell extends Spell {

    public static Identifier DarkTowerId = new Identifier(SkullMagic.MODID + ":overworld/dark_tower_base");

    protected DungeonRiseSpell() {
        super(15000, 100, 20, true);

    }

    @Override
    public boolean cast(ServerPlayerEntity player, Double powerLevel) {
        HitResult result = player.raycast(100, 1, false);
        boolean castResult = false;
        if (result != null) {
            Vec3d center = result.getPos();
            center = center.add(0, 9, 0);
            BlockPos pos = BlockPos.ofFloored(center);
            ServerWorld world = (ServerWorld) player.getWorld();

            Registry<StructurePool> registry = world.getRegistryManager().get(RegistryKeys.TEMPLATE_POOL);
            if (registry.containsId(DarkTowerId)) {
                RegistryEntry<StructurePool> registryEntry = registry.getEntry(registry.get(DarkTowerId));
                castResult = generate(world, registryEntry,
                        DarkTowerId,
                        11,
                        pos, false);
            } else {
                SkullMagic.LOGGER.warn("ID NOT found: " + DarkTowerId);
            }

        }
        return castResult;
    }

}
