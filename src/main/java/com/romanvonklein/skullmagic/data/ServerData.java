package com.romanvonklein.skullmagic.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.NotImplementedException;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.blockEntities.AConsumerBlockEntity;
import com.romanvonklein.skullmagic.blocks.ASPellPedestal;
import com.romanvonklein.skullmagic.config.Config;
import com.romanvonklein.skullmagic.networking.ServerPackageSender;
import com.romanvonklein.skullmagic.spells.Spell;
import com.romanvonklein.skullmagic.spells.SpellInitializer;
import com.romanvonklein.skullmagic.util.Parsing;
import com.romanvonklein.skullmagic.util.Util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

public class ServerData extends PersistentState {

    HashMap<UUID, PlayerData> players;
    static private Map<String, ? extends Spell> spells;

    public ServerData() {
        this.players = new HashMap<UUID, PlayerData>();
    }

    public ServerData(HashMap<UUID, PlayerData> players) {
        this.players = players;
        this.generateDataShortcuts();
        this.GenerateBufferedData();
    }

    /**
     * This function generates all buffered values as read from the current state of
     * the ServerData instance.
     * That data includes spell details( cost, efficiency, power... )
     */
    private void GenerateBufferedData() {
        throw new NotImplementedException();
    }

    /**
     * This funciton generates data shortcuts for both easier and more performant
     * access to the data contained in the deeper layers of this ServerData
     * instance.
     */
    private void generateDataShortcuts() {
        throw new NotImplementedException();
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    public PlayerData getPlayerData(UUID playerID) {
        return players.get(playerID);
    }

    public void tick(MinecraftServer server) {
        for (PlayerData data : this.players.values()) {
            if (data.getEssencePool() != null) {
                data.getEssencePool().tick(server);
            }
        }
    }

    public void removeSkullAltar(World world, BlockPos pos) {
        throw new NotImplementedException();
    }

    public void removePedestal(ServerWorld world, BlockPos pos) {
        throw new NotImplementedException();

    }

    public void removeSpellAltar(ServerWorld world, BlockPos pos) {
        throw new NotImplementedException();

    }

    public void removeSpellCooldownPedestal(ServerWorld world, BlockPos pos) {
        throw new NotImplementedException();

    }

    public void removeSpellEfficiencyPedestal(ServerWorld world, BlockPos pos) {
        throw new NotImplementedException();

    }

    public void removeSpellPowerPedestal(ServerWorld world, BlockPos pos) {
        throw new NotImplementedException();

    }

    public void removeConsumer(RegistryKey<World> worldKey, BlockPos pos) {
        throw new NotImplementedException();
    }

    public WorldBlockPos getAltarWorldPosForPlayer(ServerPlayerEntity player) {
        return new WorldBlockPos(this.players.get(player.getUuid()).essencePool.altarPos,
                this.players.get(player.getUuid()).essencePool.worldKey);
    }

    /**
     * Checks wether the altar can be linked or unlinked from player. Accordingly
     * proceeds to create a new link, remove an existing one, or tell the player why
     * no actioncan be performed.
     * 
     * @param player
     * @param pos
     */
    public void trySetLinkedPlayer(ServerPlayerEntity player, WorldBlockPos pos) {
        if (!playerHasAltar(player)) {
            // player has no altar
            if (altarIsBound(pos)) {
                // altar is bound => tell player
                player.sendMessage(new TranslatableText("skullmagic.message.altar_linked_to_other_player"), true);

            } else {
                // altar is unlinked => bind the altar
                linkAltar(player, pos);
                player.sendMessage(new TranslatableText("skullmagic.message.unlinked_altar_from_player"), true);

            }
        } else {
            // player already has an altar
            if (pos.equals(getAltarWorldPosForPlayer(player))) {
                // the clicked altar is the player's => unbind the altar
                unlinkAltar(player);
                player.sendMessage(new TranslatableText("skullmagic.message.unlinked_altar_from_player"), true);

            } else if (altarIsBound(pos)) {
                // the player clicked someone elses altar => tell player
                player.sendMessage(new TranslatableText("skullmagic.message.already_linked_to_altar_at")
                        .append(pos.toShortString()), true);
            } else {
                // the player clickes an unlinked altar => tell player
                player.sendMessage(new TranslatableText("skullmagic.message.already_linked_to_altar_at")
                        .append(pos.toShortString()), true);
            }
        }
    }

    public void linkAltar(ServerPlayerEntity player, WorldBlockPos pos) {
        ServerWorld world = player.getServer().getWorld(pos.worldKey);
        HashMap<BlockPos, String> pedestals = getUnlinkedSkullPedestalsInBox(new Box());
        ArrayList<BlockPos> consumers = getUnlinkedConsumersInBox(world,
                new Box(pos.subtract(new Vec3i(Config.getConfig().scanWidth, Config.getConfig().scanHeight,
                        Config.getConfig().scanWidth)),
                        pos.add(new Vec3i(Config.getConfig().scanWidth, Config.getConfig().scanHeight,
                                Config.getConfig().scanWidth))));
        this.players.get(player.getUuid()).essencePool = new EssencePool(pos, pos.worldKey, pedestals, consumers);
        ServerPackageSender.sendUpdatePlayerDataPackageForPlayer(player);
    }

    private ArrayList<BlockPos> getUnlinkedConsumersInBox(ServerWorld world, Box box) {
        ArrayList<BlockPos> results = new ArrayList<>();

        ArrayList<? extends AConsumerBlockEntity> candidates = new ArrayList<>();
        candidates.addAll(Util.getBlockEntitiesOfTypeInBox(world, box,
                SkullMagic.FIRE_CANNON_BLOCK_ENTITY));

        for (AConsumerBlockEntity entity : candidates) {
            if (!consumerIsLinked(new WorldBlockPos(entity.getPos(),
                    world.getRegistryKey())))
                results.add(entity.getPos());
        }
        return results;
    }

    public void unlinkAltar(ServerPlayerEntity player) {
        this.players.get(player.getUuid()).essencePool = new EssencePool();
        ServerPackageSender.sendUpdatePlayerDataPackageForPlayer(player);
    }

    public boolean altarIsBound(WorldBlockPos pos) {
        boolean result = false;
        for (PlayerData data : this.players.values()) {
            EssencePool pool = data.getEssencePool();
            if (pool != null && pool.worldKey.equals(pos.worldKey)
                    && pool.altarPos.toShortString().equals(pos.toShortString())) {
                result = true;
                break;
            }
        }
        return result;
    }

    public void removeCapacityCrystal(RegistryKey<World> registryKey, BlockPos pos) {
        throw new NotImplementedException();
    }

    public void tryAddCapacityCrystal(RegistryKey<World> registryKey, BlockPos pos, UUID id) {
        throw new NotImplementedException();
    }

    public void clear() {
        players = new HashMap<>();
    }

    public boolean addConsumer(RegistryKey<World> registryKey, BlockPos pos, UUID id) {
        throw new NotImplementedException();
    }

    public EssencePool getEssencePoolForConsumer(RegistryKey<World> registryKey, BlockPos pos) {
        return null;
    }

    public void tryLinkSkullPedestalToNearbyAltar(ServerWorld world, BlockPos down) {
    }

    public String toJsonString() {
        // TODO: implement this...
        return this.toString();
    }

    public boolean playerHasAltar(ServerPlayerEntity serverPlayerEntity) {
        UUID id = serverPlayerEntity.getGameProfile().getId();
        return this.players.containsKey(id) && this.players.get(id).getEssencePool() != null;
    }

    // Deep Access Functions

    // serialization

    public NbtCompound getNbtCompoundForPlayer(ServerPlayerEntity player) {
        NbtCompound compound = new NbtCompound();
        NbtCompound playerDataCompound = new NbtCompound();
        this.players.get(player.getUuid()).writeNbt(playerDataCompound);
        compound.put("playerdata", playerDataCompound);
        compound.putString("spellnames", String.join(";", getSpellNames()));
        return compound;
    }

    public NbtCompound writeNbt(NbtCompound tag) {
        // players
        for (UUID id : this.players.keySet()) {
            NbtCompound playerDataCompound = new NbtCompound();
            this.players.get(id).writeNbt(playerDataCompound);
            tag.put(id.toString(), playerDataCompound);
        }
        return tag;
    }

    public static ServerData fromNbt(NbtCompound tag) {
        // players
        HashMap<UUID, PlayerData> players = new HashMap<>();
        for (String key : tag.getKeys()) {
            players.put(UUID.fromString(key), PlayerData.fromNbt(tag.getCompound(key)));
        }

        return new ServerData(players);
    }

    public boolean doesPlayerKnowSpell(UUID playerID, String spellname) {
        return this.players.get(playerID).spells.containsKey(spellname);
    }

    public static int getLevelCost(String spellname) {
        return spells.get(spellname).learnLevelCost;
    }

    public boolean learnSpell(ServerPlayerEntity player, String spellname, boolean force) {
        UUID playerID = player.getGameProfile().getId();
        boolean success = false;
        if (!doesPlayerKnowSpell(playerID, spellname)) {
            // check player level and deduct if sufficient
            if (force) {
                this.players.get(playerID).spells.put(spellname, new SpellData(spells.get(spellname)));
                ServerPackageSender.sendUpdatePlayerDataPackageForPlayer(player);
                success = true;
            } else {
                int spellcost = getLevelCost(spellname);
                if (player.experienceLevel >= spellcost) {
                    // player.getServer().level
                    player.addExperienceLevels(-spellcost);
                    this.players.get(playerID).spells.put(spellname, new SpellData(spells.get(spellname)));
                    ServerPackageSender.sendUpdatePlayerDataPackageForPlayer(player);
                    success = true;
                } else {
                    player.sendMessage(new TranslatableText("skullmagic.message.missing_required_level")
                            .append(Integer.toString(spellcost)), true);
                }
            }
        }
        return success;
    }

    public static String[] getSpellNames() {
        return Parsing.setToStringArr(spells.keySet());
    }

    public void removeSpellShrine(ServerWorld world, BlockPos pos) {
        throw new NotImplementedException();
    }

    public void removeSpellPedestal(ServerWorld world, BlockPos pos, String type) {
        throw new NotImplementedException();
    }

    public boolean tryAddSpellPedestal(ServerWorld world, BlockPos pos, UUID id, String spellname,
            ASPellPedestal asPellPedestal) {
        throw new NotImplementedException();
        // return false;
    }

    public void learnAllSpellsForPlayer(ServerPlayerEntity player) {
        throw new NotImplementedException();
    }

    public void castSpell(String spellname, ServerPlayerEntity serverPlayerEntity, World world) {
        throw new NotImplementedException();
    }

    public void removeSpellShrineForPlayer(ServerWorld world, BlockPos pos, ServerPlayerEntity player) {
        throw new NotImplementedException();
    }

    public void addNewSpellShrineForPlayer(ServerWorld world, BlockPos pos, UUID id, String spellname) {
        throw new NotImplementedException();
    }

    public boolean playerHasSpellShrine(UUID playerid, String spellname) {
        throw new NotImplementedException();
    }

    public BlockPos getSpellShrineForPlayer(UUID playerid, String spellname) {
        throw new NotImplementedException();
    }

    public boolean canConsumerApply(WorldBlockPos pos, int essenceCost) {
        throw new NotImplementedException();
    }

    public void applyConsumer(WorldBlockPos pos, int essenceCost) {
        throw new NotImplementedException();
    }

    public ServerPlayerEntity getPlayerForConsumerWorldPos(WorldBlockPos worldPos) {
        return null;
    }

    public static void initSpells() {
        spells = SpellInitializer.initSpells();
    }

    public static Map<String, ? extends Spell> getSpells() {
        return spells;
    }

    public void createPlayerEntryIfNotExists(ServerPlayerEntity player) {
        UUID playerID = player.getUuid();
        if (!players.containsKey(playerID)) {
            players.put(playerID, new PlayerData());
        }
    }
}