package com.romanvonklein.skullmagic.blockEntities;

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
import net.minecraft.world.World;

public class SkullAltarBlockEntity extends BlockEntity {
    private int essence = 0;
    private int essenceChargeRate = 0;
    private int maxEssence = 100;
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
        if (!world.isClient) {
            int prevEssence = be.getEssence();
            be.chargeEssence();
            SkullMagic.LOGGER.info(prevEssence + "->" + be.getEssence());
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
        String playerID = player.getUuidAsString();
        if (this.linkedPlayerID.equals("")) {
            this.linkedPlayerID = playerID;
            player.sendMessage(Text.of("Linked you to this altar."), true);
            //ServerWorld.getPersistentStateManager().save
        } else if (this.linkedPlayerID.equals(playerID)) {
            this.linkedPlayerID = "";
            player.sendMessage(Text.of("Unlinked you from this altar."), true);
        } else {
            player.sendMessage(Text.of("This altar is already linked to another player."), true);
        }
    }
}