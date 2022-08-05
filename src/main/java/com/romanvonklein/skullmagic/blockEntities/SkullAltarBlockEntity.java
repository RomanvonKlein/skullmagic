package com.romanvonklein.skullmagic.blockEntities;

import java.util.Optional;

import javax.annotation.Nullable;

import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import java.util.ArrayList;

public class SkullAltarBlockEntity extends BlockEntity {
    private int essence = 0;
    private int essenceChargeRate = 0;
    private int maxEssence = 100;
    private int[] connectedPedestals;
    private String linkedPlayerID = "";

    public SkullAltarBlockEntity(BlockPos pos, BlockState state) {
        super(SkullMagic.SKULL_ALTAR_BLOCK_ENTITY, pos, state);

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

        if (tag.contains("essence")) {
            try {
                essence = tag.getInt("essence");
                SkullMagic.LOGGER.info("Successfully loaded essence from nbt: " + essence);
            } catch (Exception e) {
                SkullMagic.LOGGER.error("Failed loading essence for altar from NBT data!");
            }
        }
        if (tag.contains("essenceChargeRate")) {
            try {
                essenceChargeRate = tag.getInt("essenceChargeRate");
                SkullMagic.LOGGER.info("Successfully loaded essencechargeRate from nbt: " + essenceChargeRate);
            } catch (Exception e) {
                SkullMagic.LOGGER.error("Failed loading essenceChargeRate from NBT data!");
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

    public static void tick(World world, BlockPos pos, BlockState state, SkullAltarBlockEntity be) {
        if (world.isClient) {

        } else {
            int prevEssence = be.getEssence();
            be.chargeEssence();
            if (prevEssence != be.getEssence()) {
                // TODO: send update to client??
            }
            // SkullMagic.LOGGER.info(prevEssence + "->" + be.getEssence() + "(" +
            // be.essenceChargeRate + ")");
        }
    }

    private int getEssence() {
        return this.essence;

    }

    public void addChargeRate(int amount) {
        this.essenceChargeRate += amount;
    }

    public void removeChargeRate(int amount) {
        this.essenceChargeRate -= amount;
    }

    public void chargeEssence() {
        this.essence += this.essenceChargeRate;
        if (this.essence > this.maxEssence) {
            this.essence = this.maxEssence;
        }
    }

    public String getLinkedPlayerId() {
        return this.linkedPlayerID;
    }

    public void trySetLinkedPlayer(PlayerEntity player) {

        if (!world.isClient) {
            if (SkullMagic.StateManager.playerHasLink(player.getUuid())
                    && !SkullMagic.StateManager.getLinkedAltarBlockPos(player.getUuid()).equals(this.pos)) {
                player.sendMessage(Text.of("You already have an altar linked to you at "
                        + SkullMagic.StateManager.getAltarPosLinkedToPlayer(player.getUuid()) + "."), true);
            } else {

                String playerID = player.getUuidAsString();
                if (this.linkedPlayerID.equals("")) {
                    this.linkedPlayerID = playerID;
                    SkullMagic.StateManager.addLink(player.getUuid(), this.pos);
                    player.sendMessage(Text.of("Linked you to this altar."), true);
                } else if (this.linkedPlayerID.equals(playerID)) {
                    this.linkedPlayerID = "";
                    SkullMagic.StateManager.removeLink(player.getUuid(), this.pos);
                    player.sendMessage(Text.of("Unlinked you from this altar."), true);
                } else {
                    player.sendMessage(Text.of("This altar is already linked to another player."), true);
                }
            }
        }
    }

    public void checkAllPedestals(World world) {
        // reset all values directly influenced by pedestals
        this.essenceChargeRate = 0;
        ArrayList<SkullPedestalBlockEntity> checkedSkullPedestals = new ArrayList<>();

        // check if all saved pedestal locations still contain a pedestal with a skull
        // on it
        for (int i = 0; i < connectedPedestals.length; i += 3) {
            BlockPos candidatePos = new BlockPos(connectedPedestals[i], connectedPedestals[i + 1],
                    connectedPedestals[i + 2]);
            Optional<SkullPedestalBlockEntity> candidate = world.getBlockEntity(
                    candidatePos,
                    SkullMagic.SKULL_PEDESTAL_BLOCK_ENTITY);
            if (candidate.isPresent()) {
                if (candidate.get().checkSkullPedestal(world, new BlockPos(pos), this)) {
                    checkedSkullPedestals.add(candidate.get());
                }
            }
        }
        // readd all valid pedestals back to this.connctedPedestals
        this.connectedPedestals = new int[checkedSkullPedestals.size() * 3];
        for (int i = 0; i < checkedSkullPedestals.size() * 3; i++) {
            SkullPedestalBlockEntity ent = checkedSkullPedestals.get(i);
            BlockPos pos = ent.getPos();
            this.connectedPedestals[i] = pos.getX();
            this.connectedPedestals[i + 1] = pos.getY();
            this.connectedPedestals[i + 2] = pos.getZ();
        }
    }

    public String getEssenceSummary() {
        return String.format("%d/%d(%d)", this.essence, this.maxEssence, this.essenceChargeRate);
    }

    // TODO: read into this for networking
    /*
     * @Nullable
     * 
     * @Override
     * public Packet<ClientPlayPacketListener> toUpdatePacket() {
     * return BlockEntityUpdateS2CPacket.create(this);
     * }
     * 
     * @Override
     * public NbtCompound toInitialChunkDataNbt() {
     * return createNbt();
     * }
     */
}