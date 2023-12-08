package com.romanvonklein.skullmagic.blocks;

import org.jetbrains.annotations.Nullable;

import com.romanvonklein.skullmagic.blockEntities.SkullMagicSpawnerBlockEntity;
import com.romanvonklein.skullmagic.util.Util;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SkullMagicSpawner extends BlockWithEntity {
    private String spawnClass;

    public SkullMagicSpawner(Settings settings, String spawnClass) {
        super(settings);
        this.spawnClass = spawnClass;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        SkullMagicSpawnerBlockEntity ent = new SkullMagicSpawnerBlockEntity(pos, state);
        ent.setSpawnSettings(Util.getSpawnForSpawnerWithClass(this.spawnClass));
        return ent;
    }

    public String getSpawnClass() {
        return this.spawnClass;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world,
            BlockState state,
            BlockEntityType<T> type) {

        return SkullMagicSpawnerBlockEntity::tick;
    }
}
