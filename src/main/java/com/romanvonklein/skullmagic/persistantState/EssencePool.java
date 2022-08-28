package com.romanvonklein.skullmagic.persistantState;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.config.Config;
import com.romanvonklein.skullmagic.networking.NetworkingConstants;
import com.romanvonklein.skullmagic.util.Parsing;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

public class EssencePool extends PersistentState {
    int essence;
    int essenceChargeRate;
    int maxEssence;
    int chargeCooldown = 5;
    int chargeCooldownLeft = 0;
    BlockPos position;
    UUID linkedPlayerID;
    HashMap<BlockPos, String> linkedPedestals = new HashMap<>();

    public static Map<UUID, EssencePool> playerToEssencePool = new HashMap<>();

    public static EssencePool getEssencePoolForPlayer(UUID playerID) {
        if (playerToEssencePool.containsKey(playerID)) {
            return getEssencePoolForPlayer(playerID);
        } else {
            return null;
        }
    }

    public EssencePool() {
        this.essence = 0;
        this.essenceChargeRate = 0;
        this.maxEssence = 100;
    }

    public EssencePool(BlockPos pos) {
        this.position = pos;
        this.essence = 0;
        this.essenceChargeRate = 0;
        this.maxEssence = 100;
    }

    public EssencePool(BlockPos pos, int essence, int essenceChargeRate, int maxEssence) {
        this.position = pos;
        this.essence = essence;
        this.essenceChargeRate = essenceChargeRate;
        this.maxEssence = maxEssence;
    }

    public void tick(World world) {
        if (!world.isClient) {
            if (this.linkedPlayerID != null) {
                this.chargeCooldownLeft--;
                if (this.chargeCooldownLeft <= 0) {
                    chargeCooldownLeft = chargeCooldown;
                    this.essence += this.essenceChargeRate;
                    if (this.essence > this.maxEssence) {
                        this.essence = this.maxEssence;
                    }
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeIntArray(new int[] { this.essence, this.maxEssence, this.essenceChargeRate });
                    // TODO: this seems to fail. was the playerid set correctly when linking?
                    ServerPlayNetworking.send(
                            (ServerPlayerEntity) (world.getPlayerByUuid(this.linkedPlayerID)),
                            NetworkingConstants.ESSENCE_CHARGE_UPDATE_ID, buf);
                }
            }
        }
    }

    public int getEssence() {
        return this.essence;
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

    public NbtCompound writeNbt(NbtCompound tag) {
        SkullMagic.LOGGER.info("Saving this Mana pool:");

        tag.putString("linkedPlayerID", this.linkedPlayerID.toString());
        tag.putInt("essence", this.essence);
        tag.putInt("maxEssence", this.maxEssence);
        tag.putInt("essenceChargeRate", this.essenceChargeRate);
        NbtCompound linkedPedestalsNBT = new NbtCompound();
        for (Entry<BlockPos, String> entry : this.linkedPedestals.entrySet()) {
            linkedPedestalsNBT.putString(entry.getKey().toShortString(), entry.getValue());
        }
        tag.put("linkedPedestals", linkedPedestalsNBT);
        return tag;
    }

    public static EssencePool fromNbt(NbtCompound tag) {
        System.out.println("Reading this EssencePools from NBT");
        EssencePool pool = new EssencePool();
        pool.linkedPlayerID = tag.getUuid("linkedPlayerID");
        pool.essence = tag.getInt("essence");
        pool.maxEssence = tag.getInt("maxEssence");
        pool.essenceChargeRate = tag.getInt("essenceChargeRate");
        NbtCompound pedestalList = tag.getCompound("linkedPedestals");
        pedestalList.getKeys().forEach((shortString) -> {
            // TODO: make this an int array for multiple values in skulls
            pool.linkedPedestals.put(Parsing.shortStringToBlockPos(shortString), pedestalList.getString(shortString));
        });
        return pool;
    }
    /*
     * public void checkAllPedestals(World world) {
     * // reset all values directly influenced by pedestals
     * this.essenceChargeRate = 0;
     * ArrayList<SkullPedestalBlockEntity> checkedSkullPedestals = new
     * ArrayList<>();
     * 
     * // check if all saved pedestal locations still contain a pedestal with a
     * skull
     * // on it
     * for (int i = 0; i < linkedPedestals.size(); i += 3) {
     * BlockPos candidatePos =
     * Parsing.shortStringToBlockPos(linkedPedestals.get(i));
     * Optional<SkullPedestalBlockEntity> candidate = world.getBlockEntity(
     * candidatePos,
     * SkullMagic.SKULL_PEDESTAL_BLOCK_ENTITY);
     * if (candidate.isPresent()) {
     * if (candidate.get().checkSkullPedestal(world, new BlockPos(pos), this)) {
     * checkedSkullPedestals.add(candidate.get());
     * }
     * }
     * }
     * // readd all valid pedestals back to this.connctedPedestals
     * this.linkedPedestals = new int[checkedSkullPedestals.size() * 3];
     * for (int i = 0; i < checkedSkullPedestals.size() * 3; i++) {
     * SkullPedestalBlockEntity ent = checkedSkullPedestals.get(i);
     * BlockPos pos = ent.getPos();
     * this.linkedPedestals[i] = pos.getX();
     * this.linkedPedestals[i + 1] = pos.getY();
     * this.linkedPedestals[i + 2] = pos.getZ();
     * }
     * }
     */

    public void removePedestal(BlockPos pos) {
        // TODO: add more fields other than essencechargerate here...
        int lostChargeRate = Config.getConfig().skulls.get(this.linkedPedestals.get(pos));
        SkullMagic.LOGGER.info("Removing " + lostChargeRate + "from essence pool.");
        this.essenceChargeRate -= lostChargeRate;
        this.linkedPedestals.remove(pos);
    }

    public void linkPedestal(BlockPos pedestalPos, String skullIdentifier) {
        // TODO: add more fields other than essencechargerate here...
        int addChargeRate = Config.getConfig().skulls.get(skullIdentifier);
        this.essenceChargeRate += addChargeRate;
        this.linkedPedestals.put(pedestalPos, skullIdentifier);
    }
}
