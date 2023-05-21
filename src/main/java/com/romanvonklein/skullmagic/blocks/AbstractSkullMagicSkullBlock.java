package com.romanvonklein.skullmagic.blocks;

import org.jetbrains.annotations.Nullable;

import com.romanvonklein.skullmagic.blockEntities.SkullMagicSkullBlockEntity;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.Equipment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AbstractSkullMagicSkullBlock
        extends BlockWithEntity
        implements Equipment {
    private final SkullMagicSkullBlock.SkullMagicSkullType type;

    public AbstractSkullMagicSkullBlock(SkullMagicSkullBlock.SkullMagicSkullType type,
            AbstractBlock.Settings settings) {
        super(settings);
        this.type = type;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
            BlockEntityType<T> type) {
        // if (world.isClient) {
        // boolean bl;
        // boolean bl2 = bl = state.isOf(Blocks.DRAGON_HEAD) ||
        // state.isOf(Blocks.DRAGON_WALL_HEAD)
        // || state.isOf(Blocks.PIGLIN_HEAD) || state.isOf(Blocks.PIGLIN_WALL_HEAD);
        // if (bl) {
        // return AbstractSkullMagicSkullBlock.checkType(type, BlockEntityType.SKULL,
        // SkullMagicSkullBlockEntity::tick);
        // }
        // }
        return null;
    }

    public SkullMagicSkullBlock.SkullMagicSkullType getSkullType() {
        return this.type;
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

    @Override
    public EquipmentSlot getSlotType() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SkullMagicSkullBlockEntity(pos, state);
    }

}
