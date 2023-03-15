package com.romanvonklein.skullmagic.essence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.config.Config;
import com.romanvonklein.skullmagic.networking.NetworkingConstants;
import com.romanvonklein.skullmagic.networking.ServerPackageSender;
import com.romanvonklein.skullmagic.util.Parsing;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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
    public UUID linkedPlayerID;
    HashMap<BlockPos, String> linkedPedestals = new HashMap<>();
    ArrayList<BlockPos> Consumers = new ArrayList<>();

    public EssencePool() {
        this.essence = 0;
        this.essenceChargeRate = 0;
        this.maxEssence = Config.getConfig().altarCapacity;
    }

    public EssencePool(BlockPos pos, UUID playerID) {
        this.position = pos;
        this.linkedPlayerID = playerID;
        this.essence = 0;
        this.essenceChargeRate = 0;
        this.maxEssence = Config.getConfig().altarCapacity;
    }

    public EssencePool(BlockPos pos, int essence, int essenceChargeRate, int maxEssence) {
        this.position = pos;
        this.essence = essence;
        this.essenceChargeRate = essenceChargeRate;
        this.maxEssence = maxEssence;
    }

    public void reduceMaxEssence(int amount) {
        this.maxEssence -= amount;
        if (maxEssence < Config.getConfig().altarCapacity) {
            maxEssence = Config.getConfig().altarCapacity;
        }
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
                    ServerPlayerEntity player = (ServerPlayerEntity) (world.getPlayerByUuid(this.linkedPlayerID));
                    if (player != null) {
                        ServerPlayNetworking.send(player, NetworkingConstants.ESSENCE_CHARGE_UPDATE_ID, buf);
                    }
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
        if (this.linkedPlayerID != null) {
            tag.putUuid("linkedPlayerID", this.linkedPlayerID);
        }
        tag.putInt("essence", this.essence);
        tag.putInt("maxEssence", this.maxEssence);
        tag.putInt("essenceChargeRate", this.essenceChargeRate);
        NbtCompound altarPosition = new NbtCompound();
        altarPosition.putInt("x", this.position.getX());
        altarPosition.putInt("y", this.position.getY());
        altarPosition.putInt("z", this.position.getZ());
        tag.put("altarPosition", altarPosition);
        NbtCompound linkedPedestalsNBT = new NbtCompound();
        for (Entry<BlockPos, String> entry : this.linkedPedestals.entrySet()) {
            linkedPedestalsNBT.putString(entry.getKey().toShortString(), entry.getValue());
        }
        tag.put("linkedPedestals", linkedPedestalsNBT);

        NbtCompound linkedConsumersNBT = new NbtCompound();
        for (int i = 0; i < this.Consumers.size(); i++) {
            linkedConsumersNBT.putString(Integer.toString(i), this.Consumers.get(i).toShortString());
        }
        tag.put("linkedConsumers", linkedConsumersNBT);
        return tag;
    }

    public static EssencePool fromNbt(NbtCompound tag) {
        EssencePool pool = new EssencePool();
        if (tag.contains("linkedPlayerID")) {
            pool.linkedPlayerID = tag.getUuid("linkedPlayerID");
        }
        pool.essence = tag.getInt("essence");
        pool.maxEssence = tag.getInt("maxEssence");
        pool.essenceChargeRate = tag.getInt("essenceChargeRate");
        NbtCompound altarPosition = tag.getCompound("altarPosition");
        int x = altarPosition.getInt("x");
        int y = altarPosition.getInt("y");
        int z = altarPosition.getInt("z");
        pool.position = new BlockPos(x, y, z);
        NbtCompound pedestalList = tag.getCompound("linkedPedestals");
        pedestalList.getKeys().forEach((shortString) -> {
            pool.linkedPedestals.put(Parsing.shortStringToBlockPos(shortString), pedestalList.getString(shortString));
        });
        NbtCompound linkedConsumersList = tag.getCompound("linkedConsumers");
        linkedConsumersList.getKeys().forEach((index) -> {
            pool.Consumers.add(Parsing.shortStringToBlockPos(linkedConsumersList.getString(index)));
        });
        return pool;
    }

    public void removePedestal(ServerWorld world, BlockPos pos) {
        if (this.linkedPedestals.containsKey(pos)) {
            int lostChargeRate = Config.getConfig().skulls.get(this.linkedPedestals.get(pos));
            SkullMagic.LOGGER.info("Removing " + lostChargeRate + " from essence pool.");
            this.essenceChargeRate -= lostChargeRate;
            this.linkedPedestals.remove(pos);
            if (this.linkedPlayerID != null) {
                ServerPlayerEntity player = (ServerPlayerEntity) world.getPlayerByUuid(this.linkedPlayerID);
                if (player != null) {
                    ServerPackageSender.sendUpdateLinksPackage(player);
                }
            }
        }
    }

    public void linkPedestal(ServerWorld world, BlockPos pedestalPos, String skullIdentifier) {
        int addChargeRate = Config.getConfig().skulls.get(skullIdentifier);
        this.essenceChargeRate += addChargeRate;
        this.linkedPedestals.put(pedestalPos, skullIdentifier);
        if (this.linkedPlayerID != null) {
            ServerPlayerEntity player = (ServerPlayerEntity) world.getPlayerByUuid(this.linkedPlayerID);
            if (player != null) {
                ServerPackageSender.sendUpdateLinksPackage(player);
            }
        }
    }

    public void addConsumer(BlockPos pos) {
        this.Consumers.add(pos);
    }

    public void removeConsumer(BlockPos pos) {
        this.Consumers.remove(pos);
    }

    public JsonObject toJsonElement() {
        JsonObject elem = new JsonObject();
        elem.addProperty("essence", this.essence);
        elem.addProperty("maxEssence", this.maxEssence);
        elem.addProperty("essenceChargeRate", this.essenceChargeRate);
        elem.addProperty("position", this.position.toShortString());
        JsonArray consumerList = new JsonArray();
        for (BlockPos pos : this.Consumers) {
            consumerList.add(pos.toShortString());
        }
        elem.add("consumers", consumerList);
        return elem;
    }

    public ArrayList<BlockPos> getLinkedPedestals() {
        ArrayList<BlockPos> result = new ArrayList<BlockPos>();
        result.addAll(this.linkedPedestals.keySet());
        return result;
    }

    public BlockPos getAltarPos() {
        return this.position;
    }
}
