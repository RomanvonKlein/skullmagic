package com.romanvonklein.skullmagic.blockEntities;

import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class SkullMagicSkullBlockEntity
        extends BlockEntity {

    public SkullMagicSkullBlockEntity(BlockPos pos,
            BlockState state) {
        super(SkullMagic.SKULL_BLOCK_ENTITY, pos, state);
    }

    // public static void tick(World world, BlockPos pos, BlockState state,
    // SkullMagicSkullBlockEntity blockEntity) {
    // }

    // @Override
    // public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world,
    // BlockState state,
    // BlockEntityType<T> type) {
    // return checkType(type, SkullMagic.BLOCK_PLACER_BLOCK_ENTITY,
    // (world1, pos, state1, be) -> BlockPlacerBlockEntity.tick(world1, pos, state1,
    // be));
    // }

    // public float getTicksPowered(float tickDelta) {
    // if (this.powered) {
    // return (float) this.ticksPowered + tickDelta;
    // }
    // return this.ticksPowered;
    // }

    // public BlockEntityUpdateS2CPacket toUpdatePacket() {
    // return BlockEntityUpdateS2CPacket.create(this);
    // }

    // @Override
    // public NbtCompound toInitialChunkDataNbt() {
    // return this.createNbt();
    // }

}
