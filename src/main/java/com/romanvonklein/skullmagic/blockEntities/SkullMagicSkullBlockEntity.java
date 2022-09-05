package com.romanvonklein.skullmagic.blockEntities;

import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SkullMagicSkullBlockEntity
        extends BlockEntity {
    private int ticksPowered;
    private boolean powered;

    public SkullMagicSkullBlockEntity(BlockPos pos, BlockState state) {
        super(SkullMagic.SKULL_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, SkullMagicSkullBlockEntity blockEntity) {
        if (world.isReceivingRedstonePower(pos)) {
            blockEntity.powered = true;
            ++blockEntity.ticksPowered;
        } else {
            blockEntity.powered = false;
        }
    }

    

    // @Override
    // public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
    //         BlockEntityType<T> type) {
    //     return checkType(type, SkullMagic.BLOCK_PLACER_BLOCK_ENTITY,
    //             (world1, pos, state1, be) -> BlockPlacerBlockEntity.tick(world1, pos, state1, be));
    // }

    public float getTicksPowered(float tickDelta) {
        if (this.powered) {
            return (float) this.ticksPowered + tickDelta;
        }
        return this.ticksPowered;
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return this.createNbt();
    }

}
