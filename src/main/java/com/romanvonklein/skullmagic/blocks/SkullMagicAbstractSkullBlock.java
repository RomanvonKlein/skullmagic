package com.romanvonklein.skullmagic.blocks;

import net.minecraft.entity.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

import com.romanvonklein.skullmagic.blockEntities.SkullMagicSkullBlockEntity;

import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Equipment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SkullMagicAbstractSkullBlock
        extends BlockWithEntity
        implements Equipment {
    private final SkullMagicSkullBlock.SkullType type;

    public SkullMagicAbstractSkullBlock(SkullMagicSkullBlock.SkullType type, Settings settings) {
        super(settings);
        this.type = type;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SkullMagicSkullBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
            BlockEntityType<T> type) {
        return null;
    }

    public SkullMagicSkullBlock.SkullType getSkullType() {
        return this.type;
    }

    @Override
    public EquipmentSlot getSlotType() {
        return EquipmentSlot.HEAD;
    }
}
