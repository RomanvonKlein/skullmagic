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
    private int essence = 0;
    private int essenceChargeRate = 0;

    public SkullAltarBlockEntity(BlockPos pos, BlockState state) {
        super(SkullMagic.SKULL_ALTAR_BLOCK_ENTITY, pos, state);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        tag.putInt("essence", essence);
        tag.putInt("essenceChargeRate", essenceChargeRate);
        super.writeNbt(tag);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        essence = tag.getInt("essence");
        essenceChargeRate = tag.getInt("essenceChargeRate");
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

    public static void tick(World world, BlockPos pos, BlockState state, SkullAltarBlockEntity be) {
        be.chargeEssence();
        SkullMagic.LOGGER.info("TICKING SKULL ALTAR ");
    }

    public void chargeEssence() {
        this.essence += this.essenceChargeRate;
    }
}