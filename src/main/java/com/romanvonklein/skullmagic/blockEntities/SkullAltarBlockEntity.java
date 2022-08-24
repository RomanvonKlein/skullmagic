package com.romanvonklein.skullmagic.blockEntities;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.config.Config;
import com.romanvonklein.skullmagic.networking.NetworkingConstants;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

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

    public void tryAddPedestal(BlockPos pedestalPos) {
        // TODO: maybe pass the ids down the line instead of passing blockpos and
        // getting the ids in every function...
        // String pedestalIdentifier =
        // Registry.BLOCK.getId(this.world.getBlockState(pedestalPos).getBlock()).toString();
        String skullIdentifier = Registry.BLOCK.getId(this.world.getBlockState(pedestalPos.up()).getBlock()).toString();

        if (Config.getConfig().skulls.containsKey(skullIdentifier)) {
            if (!world.isClient) {
                world.playSound(null, pos, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1f, 1f);
            }
            this.essenceChargeRate += Config.getConfig().skulls.get(skullIdentifier);
            SkullMagic.StateManager.addPedestalLink(pedestalPos, this.pos);
        }
    }

    public void removePedestal(BlockPos pedestalPos) {
        String skullIdentifier = getPedestalSkullIdentifier(pedestalPos.up());
        if (!skullIdentifier.equals("") && Config.getConfig().skulls.containsKey(skullIdentifier)) {
            this.essenceChargeRate -= Config.getConfig().skulls.get(skullIdentifier);
        }
    }

    private String getPedestalSkullIdentifier(BlockPos pedestalPos) {
        return Registry.BLOCK.getId(world.getBlockState(pedestalPos).getBlock()).toString();
    }

    public static void tick(World world, BlockPos pos, BlockState state, SkullAltarBlockEntity be) {
        if (world.isClient) {

        } else {

            int prevEssence = be.getEssence();
            be.chargeEssence();
            UUID linkedPlayerUUID = null;
            try {
                linkedPlayerUUID = UUID.fromString(be.linkedPlayerID);
            } catch (Exception e) {
            }
            if (prevEssence != be.getEssence() && linkedPlayerUUID != null
                    && ((ServerWorld) world).getPlayerByUuid(linkedPlayerUUID) != null) {
                // create package data consisting of current essence, max essence and essence
                // charge rate
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeIntArray(new int[] { be.essence, be.maxEssence, be.essenceChargeRate });
                ServerPlayNetworking.send(
                        (ServerPlayerEntity) (world.getPlayerByUuid(UUID.fromString(be.linkedPlayerID))),
                        NetworkingConstants.ESSENCE_CHARGE_UPDATE_ID, buf);
            }
        }
    }

    public int getEssence() {
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
                String linkedUUID = SkullMagic.StateManager.getPlayerLinkedToAltar(this.pos);
                if (linkedUUID.equals("")) {
                    this.linkedPlayerID = player.getUuidAsString();
                    SkullMagic.StateManager.addAltarLink(player.getUuid(), this.pos);
                    player.sendMessage(Text.of("Linked you to this altar."), true);
                } else if (linkedUUID.equals(player.getUuidAsString())) {
                    this.linkedPlayerID = "";
                    SkullMagic.StateManager.removeAltarLink(player.getUuid(), this.pos);
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

    public void setEssence(int i) {
        this.essence = i;
    }

    public void setMaxEssence(int i) {
        this.maxEssence = i;
    }

    public void setChargeRate(int i) {
        this.essenceChargeRate = i;
    }

    public boolean discharge(int i) {
        if (this.essence >= i) {
            this.essence -= i;
            return true;
        } else {
            return false;
        }
    }
}