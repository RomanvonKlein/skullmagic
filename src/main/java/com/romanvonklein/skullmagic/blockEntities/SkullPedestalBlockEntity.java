package com.romanvonklein.skullmagic.blockEntities;

import org.jetbrains.annotations.Nullable;

import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;

public class SkullPedestalBlockEntity extends BlockEntity {
    public static final int scanRange = 5;
    public static final int scanHeight = 1;
    private int[] linkedAltarCoords;

    public SkullPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(SkullMagic.SKULL_PEDESTAL_BLOCK_ENTITY, pos, state);
        SkullMagic.LOGGER.info("Creating SkullpedestalBlockEntity");
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        if (this.linkedAltarCoords != null) {
            tag.putIntArray("linkedAltarPosition", this.linkedAltarCoords);
        }
        super.writeNbt(tag);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        if (tag.contains("linkedAltarPosition")) {
            try {
                linkedAltarCoords = tag.getIntArray("linkedAltarPosition");
                if (linkedAltarCoords != null) {
                    SkullMagic.LOGGER
                            .info(String.format("Successfully loaded linkedAltarCoords from nbt: %d %d %d",
                                    linkedAltarCoords[0], linkedAltarCoords[1], linkedAltarCoords[2]));
                }
            } catch (Exception e) {
                SkullMagic.LOGGER.error("Failed loading linked altar position from NBT data!");
            }
        }
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
}