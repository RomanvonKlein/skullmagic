package com.romanvonklein.skullmagic.lootTablemodifiers;

import com.romanvonklein.skullmagic.SkullMagic;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.util.Identifier;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;

public class LootTableModifier {
    private static final Identifier ZOMBIE_ENTITY_IDENTIFIER = EntityType.ZOMBIE.getLootTableId();

    public static void initializeLootTableModifications() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (source.isBuiltin() && ZOMBIE_ENTITY_IDENTIFIER.equals(id)) {
                LootPool.Builder poolBuilder = LootPool.builder().rolls(UniformLootNumberProvider.create(0.0f,1.0f)).with(ItemEntry.builder(Blocks.ZOMBIE_HEAD).weight(1));
                tableBuilder.pool(poolBuilder);
            }
        });
    }
}
