package com.romanvonklein.skullmagic.blockEntities;

import javax.annotation.Nullable;

import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SkullAltarBlockEntity extends BlockEntity {

    public SkullAltarBlockEntity(BlockPos pos, BlockState state) {
        super(SkullMagic.SKULL_ALTAR_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    public static void tick(World world1, BlockPos pos, BlockState state1, SkullAltarBlockEntity be) {

    }
}