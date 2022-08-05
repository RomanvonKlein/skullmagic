package com.romanvonklein.skullmagic.blockEntities;

import javax.annotation.Nullable;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.blocks.SkullPedestal;
import com.romanvonklein.skullmagic.config.Config;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

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
        tag.putIntArray("linkedAltarPosition", this.linkedAltarCoords);
        super.writeNbt(tag);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        linkedAltarCoords = tag.getIntArray("linkedAltarPosition");
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

    public boolean checkSkullPedestal(World world, BlockPos pos, SkullAltarBlockEntity skullAltarBlockEntity) {
        boolean result = false;
        // check if skull on top
        String skullCandidateIdentifier = Registry.BLOCK.getId(world.getBlockState(pos.up()).getBlock()).toString();

        if (Config.getConfig().skulls.containsKey(skullCandidateIdentifier)) {
            addSkullEssenceChargeRateToAltar(skullAltarBlockEntity, skullCandidateIdentifier);
            result = true;
        }
        return result;
    }

    public void addSkull(World world, BlockPos pos, String skullIdentifier, PlayerEntity player) {
        BlockPos altarPos = getSkullAltarNearby(world, pos);

        if (altarPos == null) {
            if (!world.isClient) {
                world.playSound(null, pos, SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS, 1f, 1f);
            }
        } else {
            SkullMagic.LOGGER.info("altar found at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
            if (!world.isClient) {
                world.playSound(null, pos, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1f, 1f);
            }
            // update pedestal charge rate
            world.setBlockState(pos, world.getBlockState(pos).with(SkullPedestal.CONNECTED, true));
            addSkullEssenceChargeRateToAltar(world.getBlockEntity(altarPos, SkullMagic.SKULL_ALTAR_BLOCK_ENTITY).get(),
                    skullIdentifier);
        }
    }

    private void addSkullEssenceChargeRateToAltar(SkullAltarBlockEntity altar, String skullIdentifier) {
        int essenceChargeRate = Config.getConfig().skulls.get(skullIdentifier);
        altar.addChargeRate(essenceChargeRate);
    }

    private static BlockPos getSkullAltarNearby(World world, BlockPos pos) {
        BlockPos altarFound = null;
        outer: for (int x = -scanRange; x < scanRange; x++) {
            for (int y = -scanHeight; y < scanHeight; y++) {
                for (int z = -scanRange; z < scanRange; z++) {
                    // TODO: better way of checking for the right blocktype
                    if (Registry.BLOCK.getId(world.getBlockState(pos.add(x, y, z)).getBlock()).toString()
                            .equals("skullmagic:skull_altar")) {
                        altarFound = pos.add(x, y, z);
                        break outer;
                    }
                }
            }
        }
        return altarFound;
    }

}