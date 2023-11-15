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
    private static final Identifier SKELETON_ENTITY_IDENTIFIER = EntityType.SKELETON.getLootTableId();
    private static final Identifier ENDERMAN_ENTITY_IDENTIFIER = EntityType.ENDERMAN.getLootTableId();
    private static final Identifier SPIDER_ENTITY_IDENTIFIER = EntityType.SPIDER.getLootTableId();
    private static final Identifier BLAZE_ENTITY_IDENTIFIER = EntityType.BLAZE.getLootTableId();

    public static void initializeLootTableModifications() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (source.isBuiltin()) {
                if (ZOMBIE_ENTITY_IDENTIFIER.equals(id)) {
                    LootPool.Builder poolBuilder = LootPool.builder()
                            .rolls(UniformLootNumberProvider.create(9f, 10.0f))
                            .with(ItemEntry.builder(Blocks.ZOMBIE_HEAD).weight(1));
                    tableBuilder.pool(poolBuilder);
                } else if (SKELETON_ENTITY_IDENTIFIER.equals(id)) {
                    LootPool.Builder poolBuilder = LootPool.builder()
                            .rolls(UniformLootNumberProvider.create(9f, 10.0f))
                            .with(ItemEntry.builder(Blocks.SKELETON_SKULL).weight(1));
                    tableBuilder.pool(poolBuilder);
                } else if (ENDERMAN_ENTITY_IDENTIFIER.equals(id)) {
                    LootPool.Builder poolBuilder = LootPool.builder()
                            .rolls(UniformLootNumberProvider.create(9f, 10.0f))
                            .with(ItemEntry.builder(SkullMagic.ENDERMAN_HEAD_BLOCK).weight(1));
                    tableBuilder.pool(poolBuilder);

                } else if (SPIDER_ENTITY_IDENTIFIER.equals(id)) {
                    LootPool.Builder poolBuilder = LootPool.builder()
                            .rolls(UniformLootNumberProvider.create(9f, 10.0f))
                            .with(ItemEntry.builder(SkullMagic.SPIDER_HEAD_BLOCK).weight(1));
                    tableBuilder.pool(poolBuilder);

                } else if (BLAZE_ENTITY_IDENTIFIER.equals(id)) {
                    LootPool.Builder poolBuilder = LootPool.builder()
                            .rolls(UniformLootNumberProvider.create(9f, 10.0f))
                            .with(ItemEntry.builder(SkullMagic.BLAZE_HEAD_BLOCK).weight(1));
                    tableBuilder.pool(poolBuilder);
                }
            }
        });
    }
}
