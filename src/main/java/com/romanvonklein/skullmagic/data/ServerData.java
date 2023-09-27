package com.romanvonklein.skullmagic.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.blockEntities.AConsumerBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.ASpellPedestalBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.CapacityCrystalBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.CooldownSpellPedestalBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.EfficiencySpellPedestalBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.PowerSpellPedestalBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.SkullAltarBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.SpellShrineBlockEntity;
import com.romanvonklein.skullmagic.blocks.ASpellPedestal;
import com.romanvonklein.skullmagic.blocks.ASpellShrine;
import com.romanvonklein.skullmagic.config.Config;
import com.romanvonklein.skullmagic.items.KnowledgeOrb;
import com.romanvonklein.skullmagic.networking.ServerPackageSender;
import com.romanvonklein.skullmagic.spells.Spell;
import com.romanvonklein.skullmagic.spells.SpellInitializer;
import com.romanvonklein.skullmagic.util.Parsing;
import com.romanvonklein.skullmagic.util.Util;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

public class ServerData extends PersistentState {

    private ArrayList<UUID> playersToUpdate = new ArrayList<>();
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

    public void setChangedForPlayer(UUID playerID) {
        this.playersToUpdate.add(playerID);
    }

    /**
     * This function generates all buffered values as read from the current state of
     * the ServerData instance.
     * That data includes spell details( cost, efficiency, power... )
     */
    private void GenerateBufferedData() {
        SkullMagic.LOGGER.warn("Buffer gerneration not implemented yet.");
        // throw new NotImplementedException();
    }

    /**
     * This funciton generates data shortcuts for both easier and more performant
     * access to the data contained in the deeper layers of this ServerData
     * instance.
     */
    private void generateDataShortcuts() {
        SkullMagic.LOGGER.warn("Data shortcuts not yet implemented!");
        // throw new NotImplementedException();
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    public PlayerData getPlayerData(UUID playerID) {
        return players.get(playerID);
    }

    public void tick(MinecraftServer server) {
        for (UUID playerID : this.players.keySet()) {
            PlayerData data = this.players.get(playerID);
            data.tick(server, playerID);
        }
        for (UUID playerID : this.playersToUpdate) {
            ServerPackageSender.sendUpdatePlayerDataPackageForPlayer(server.getPlayerManager().getPlayer(playerID));
        }
        this.playersToUpdate.clear();
    }

    public void clearEssencePool(UUID playerId) {
        this.players.get(playerId).getEssencePool().clear();
        SkullMagic.updatePlayer(playerId);
    }

    public void removeSkullAltar(World world, BlockPos pos) {
        WorldBlockPos altarPos = new WorldBlockPos(pos, world.getRegistryKey());
        if (altarIsBound(altarPos)) {
            UUID ownerID = getPlayerConnectedToAltar(altarPos);
            clearEssencePool(ownerID);
        }
    }

    public void removePedestal(ServerWorld world, BlockPos pos) {
        // TODO:: with data shortcuts, this might be more efficient.
        // TODO: old code - refactor
        boolean result = false;
        UUID playerid = null;
        for (UUID candidateID : this.players.keySet()) {
            PlayerData data = this.players.get(candidateID);
            if (data.getEssencePool().getWorldKey() != null && data.getEssencePool().getAltarPos() != null
                    && data.getEssencePool().getWorldKey().toString().equals(world.getRegistryKey().toString())) {
                BlockPos remPos = null;
                for (BlockPos pedPos : data.essencePool.getPedestalPositions()) {
                    if (pos.equals(pedPos)) {
                        remPos = pedPos;
                        result = true;
                        break;
                    }
                }
                if (remPos != null) {
                    data.essencePool.removePedestal(remPos, candidateID);
                    playerid = candidateID;
                    break;
                }
            }
        }
        if (result) {
            world.playSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_BEACON_DEACTIVATE,
                    SoundCategory.BLOCKS,
                    1.0f, 1.0f, true);
            ServerPackageSender
                    .sendUpdatePlayerDataPackageForPlayer(world.getServer().getPlayerManager().getPlayer(playerid));
        }
    }

    public void tryRemoveSpellAltar(ServerWorld world, BlockPos pos) {
        UUID ownerid = getOwnerForSpellAltar(new WorldBlockPos(pos, world.getRegistryKey()));
        if (ownerid != null) {
            removeSpellShrineForPlayer(world, pos, ownerid);
        }
    }

    private UUID getOwnerForSpellAltar(WorldBlockPos worldBlockPos) {
        UUID result = null;
        for (UUID playerID : this.players.keySet()) {
            if (this.players.get(playerID).hasSpellShrineAt(worldBlockPos)) {
                result = playerID;
                break;
            }
        }
        return result;
    }

    private UUID getOwnerForAltar(WorldBlockPos worldBlockPos) {
        UUID result = null;
        for (UUID playerID : this.players.keySet()) {
            if (this.players.get(playerID).isSameAltarPos(worldBlockPos)) {
                result = playerID;
                break;
            }
        }
        return result;
    }

    public void removeSpellCooldownPedestal(ServerWorld world, BlockPos pos) {
        for (UUID playerID : this.players.keySet()) {
            PlayerData data = this.players.get(playerID);
            if (data.tryRemoveSpellCooldownPedestal(new WorldBlockPos(pos, world.getRegistryKey()), playerID)) {
                break;
            }
        }

    }

    public void removeSpellEfficiencyPedestal(ServerWorld world, BlockPos pos) {
        for (UUID playerID : this.players.keySet()) {
            PlayerData data = this.players.get(playerID);
            if (data.tryRemoveSpellEfficiencyPedestal(new WorldBlockPos(pos, world.getRegistryKey()), playerID)) {
                break;
            }
        }

    }

    public void removeSpellPowerPedestal(ServerWorld world, BlockPos pos) {
        for (UUID playerID : this.players.keySet()) {
            PlayerData data = this.players.get(playerID);
            if (data.tryRemoveSpellPowerPedestal(new WorldBlockPos(pos, world.getRegistryKey()), playerID)) {
                break;
            }
        }
    }

    public void removeConsumer(RegistryKey<World> worldKey, BlockPos pos) {
        for (UUID playerID : this.players.keySet()) {
            PlayerData data = this.players.get(playerID);
            if (data.tryRemoveConsumer(new WorldBlockPos(pos, worldKey), playerID)) {
                break;
            }
        }
    }

    public WorldBlockPos getAltarWorldPosForPlayer(ServerPlayerEntity player) {
        WorldBlockPos result = null;
        if (playerHasAltar(player)) {
            result = new WorldBlockPos(this.players.get(player.getUuid()).essencePool.getAltarPos(),
                    this.players.get(player.getUuid()).essencePool.getWorldKey());
        }
        return result;
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
                player.sendMessage(Text.translatable("skullmagic.message.altar_linked_to_other_player"), true);

            } else {
                // altar is unlinked => bind the altar
                linkAltar(player, pos);
                player.sendMessage(Text.translatable("skullmagic.message.linked_altar_to_player"), true);

            }
        } else {
            // player already has an altar
            if (pos.equals(getAltarWorldPosForPlayer(player))) {
                // the clicked altar is the player's => unbind the altar
                unlinkAltar(player);
                player.sendMessage(Text.translatable("skullmagic.message.unlinked_altar_from_player"), true);

            } else if (altarIsBound(pos)) {
                // the player clicked someone elses altar => tell player
                player.sendMessage(Text.translatable("skullmagic.message.already_linked_to_altar_at")
                        .append(pos.toShortString()), true);
            } else {
                // the player clickes an unlinked altar => tell player
                player.sendMessage(Text.translatable("skullmagic.message.already_linked_to_altar_at")
                        .append(pos.toShortString()), true);
            }
        }
    }

    public void linkAltar(ServerPlayerEntity player, WorldBlockPos pos) {
        ServerWorld world = player.getServer().getWorld(pos.worldKey);
        Vec3i distVec = new Vec3i(Config.getConfig().scanWidth, Config.getConfig().scanHeight,
                Config.getConfig().scanWidth);
        Box searchbox = new Box(pos.subtract(distVec), pos.add(distVec));
        HashMap<BlockPos, String> pedestals = getUnlinkedSkullPedestalsInBox(world,
                searchbox);
        ArrayList<BlockPos> consumers = getUnlinkedConsumersInBox(world, searchbox);
        ArrayList<BlockPos> capacityCrystals = getUnlinkedCapacityCrystalsInBox(world, searchbox);
        this.players.get(player.getUuid())
                .setEssencePool(new EssencePool(pos, pos.worldKey, pedestals, consumers, capacityCrystals, 0),
                        player.getUuid());
    }

    private ArrayList<BlockPos> getUnlinkedCapacityCrystalsInBox(ServerWorld world, Box box) {
        ArrayList<BlockPos> results = new ArrayList<>();

        ArrayList<CapacityCrystalBlockEntity> candidates = new ArrayList<>();
        candidates.addAll(Util.getBlockEntitiesOfTypeInBox(world, box,
                SkullMagic.CAPACITY_CRYSTAL_BLOCK_ENTITY));

        for (CapacityCrystalBlockEntity entity : candidates) {
            if (!capacityCrystalIsLinked(new WorldBlockPos(entity.getPos(),
                    world.getRegistryKey())))
                results.add(entity.getPos());
        }
        return results;
    }

    private ArrayList<BlockPos> getUnlinkedConsumersInBox(ServerWorld world, Box box) {
        ArrayList<BlockPos> results = new ArrayList<>();

        ArrayList<AConsumerBlockEntity> candidates = new ArrayList<>();

        // find all types of consumers. There's propably a smarter way to do this...
        candidates.addAll(Util.getBlockEntitiesOfTypeInBox(world, box,
                SkullMagic.FIRE_CANNON_BLOCK_ENTITY));
        candidates.addAll(Util.getBlockEntitiesOfTypeInBox(world, box,
                SkullMagic.BLOCK_PLACER_BLOCK_ENTITY));
        candidates.addAll(Util.getBlockEntitiesOfTypeInBox(world, box,
                SkullMagic.BLOCK_USER_BLOCK_ENTITY));
        candidates.addAll(Util.getBlockEntitiesOfTypeInBox(world, box,
                SkullMagic.WITHER_ENERGY_CHANNELER_BLOCK_ENTITY));

        for (AConsumerBlockEntity entity : candidates) {
            if (!consumerIsLinked(new WorldBlockPos(entity.getPos(),
                    world.getRegistryKey())))
                results.add(entity.getPos());
        }
        return results;
    }

    private boolean consumerIsLinked(WorldBlockPos worldBlockPos) {
        // TODO: this could be alot more efficient if we were to use data shortcuts
        // consumersToEssencePools.
        boolean result = false;
        outer: for (PlayerData data : this.players.values()) {
            for (BlockPos pos : data.getEssencePool().getConsumerPositions()) {
                if (worldBlockPos.getX() == pos.getX() && worldBlockPos.getY() == pos.getY()
                        && worldBlockPos.getZ() == pos.getZ()
                        && data.getEssencePool().getWorldKey().toString().equals(worldBlockPos.worldKey.toString())) {
                    result = true;
                    break outer;
                }
            }
        }
        return result;
    }

    private boolean capacityCrystalIsLinked(WorldBlockPos worldBlockPos) {
        // TODO: this could be alot more efficient if we were to use data shortcuts
        // consumersToEssencePools.
        boolean result = false;
        outer: for (PlayerData data : this.players.values()) {
            for (BlockPos pos : data.getEssencePool().getCapacityCrystalPositions()) {
                if (worldBlockPos.getX() == pos.getX() && worldBlockPos.getY() == pos.getY()
                        && worldBlockPos.getZ() == pos.getZ()
                        && data.getEssencePool().getWorldKey().toString().equals(worldBlockPos.worldKey.toString())) {
                    result = true;
                    break outer;
                }
            }
        }
        return result;
    }

    private HashMap<BlockPos, String> getUnlinkedSkullPedestalsInBox(ServerWorld world, Box box) {
        HashMap<BlockPos, String> results = new HashMap<BlockPos, String>();

        for (BlockEntity ent : Util.getBlockEntitiesOfTypeInBox(world, box,
                SkullMagic.SKULL_PEDESTAL_BLOCK_ENTITY)) {
            BlockPos pos = ent.getPos();
            if (Util.getPedestalSkullIdentifier(world, pos) != null
                    && !pedestalIsLinked(new WorldBlockPos(pos, world.getRegistryKey()))) {
                results.put(pos, Registries.BLOCK.getId(world.getBlockState(pos.up()).getBlock()).toString());
            }
        }

        return results;
    }

    private boolean pedestalIsLinked(WorldBlockPos worldBlockPos) {
        boolean result = false;

        outer: for (PlayerData data : this.players.values()) {
            if (data.getEssencePool().getWorldKey() != null
                    && data.getEssencePool().getWorldKey().toString().equals(worldBlockPos.worldKey.toString())) {
                for (BlockPos pedestalPos : data.getEssencePool().getPedestalPositions()) {
                    if (worldBlockPos.isEqualTo(pedestalPos)) {
                        result = true;
                        break outer;
                    }
                }
            }
        }

        return result;
    }

    public void unlinkAltar(ServerPlayerEntity player) {
        this.players.get(player.getUuid()).essencePool = new EssencePool();
        ServerPackageSender.sendUpdatePlayerDataPackageForPlayer(player);
    }

    public boolean altarIsBound(WorldBlockPos pos) {
        boolean result = false;
        for (PlayerData data : this.players.values()) {
            EssencePool pool = data.getEssencePool();
            if (pool != null && pool.getWorldKey() != null
                    && pool.getWorldKey().toString().equals(pos.worldKey.toString())
                    && pos.isEqualTo(pool.getAltarPos())) {
                result = true;
                break;
            }
        }
        return result;
    }

    public void removeCapacityCrystal(RegistryKey<World> registryKey, BlockPos pos) {

        for (UUID playerID : this.players.keySet()) {
            PlayerData data = this.players.get(playerID);
            if (data.hasCapacityCrystal(registryKey, pos)) {
                data.removeCapacityCrystal(pos, playerID);
                break;
            }
        }
    }

    public void tryAddCapacityCrystal(ServerWorld world, BlockPos pos, ServerPlayerEntity player) {
        if (playerHasAltar(player)) {
            UUID playerID = player.getGameProfile().getId();
            PlayerData data = this.players.get(playerID);
            if (Util.inRange(data.getAltarPos(), pos, Config.getConfig().scanWidth,
                    Config.getConfig().scanHeight)) {
                data.getEssencePool().addCapacityCrystal(pos, playerID);
            }
        }
    }

    public void clear() {
        players = new HashMap<>();
    }

    public boolean tryAddConsumer(RegistryKey<World> registryKey, BlockPos pos, UUID playerID) {
        boolean result = false;
        if (this.playerHasAltar(playerID)) {
            result = this.players.get(playerID).tryAddConsumer(registryKey, pos, playerID);
        }
        return result;
    }

    public EssencePool getEssencePoolForConsumer(RegistryKey<World> registryKey, BlockPos pos) {
        EssencePool result = null;
        for (UUID playerID : this.players.keySet()) {
            PlayerData data = this.players.get(playerID);
            if (data.hasConsumerAtPos(new WorldBlockPos(pos, registryKey))) {
                result = data.getEssencePool();
            }
        }
        return result;
    }

    public void tryLinkSkullPedestalToNearbyAltar(ServerWorld world, BlockPos pedPos) {
        String skullIdentifier = Util.getPedestalSkullIdentifier(world, pedPos);
        if (skullIdentifier != null
                && !pedestalIsLinked(new WorldBlockPos(pedPos, world.getRegistryKey()))) {
            HashMap<WorldBlockPos, UUID> activeAltars = getActiveAltarsInBox(world,
                    new Box(pedPos.subtract((new Vec3i(Config.getConfig().scanWidth, Config.getConfig().scanHeight,
                            Config.getConfig().scanWidth))),
                            pedPos.add(new Vec3i(Config.getConfig().scanWidth, Config.getConfig().scanHeight,
                                    Config.getConfig().scanWidth))));
            for (WorldBlockPos altarPos : activeAltars.keySet()) {
                linkSkullPedestalToPlayerAltar(world, activeAltars.get(altarPos), pedPos, skullIdentifier);
                break;
            }

        }
    }

    private void linkSkullPedestalToPlayerAltar(ServerWorld world, UUID playerID, BlockPos pedPos,
            String skullIdentifier) {
        this.players.get(playerID).getEssencePool().addPedestal(pedPos, skullIdentifier, playerID);
        world.playSound(pedPos.getX(), pedPos.getY(), pedPos.getZ(), SoundEvents.BLOCK_BEACON_ACTIVATE,
                SoundCategory.BLOCKS,
                1.0f, 1.0f, true);
    }

    private HashMap<WorldBlockPos, UUID> getActiveAltarsInBox(ServerWorld world, Box box) {
        List<SkullAltarBlockEntity> altars = Util.getBlockEntitiesOfTypeInBox(world, box,
                SkullMagic.SKULL_ALTAR_BLOCK_ENTITY);
        HashMap<WorldBlockPos, UUID> resultAltars = new HashMap<>();
        for (SkullAltarBlockEntity candidate : altars) {
            WorldBlockPos candidatePos = new WorldBlockPos(candidate.getPos(), world.getRegistryKey());
            UUID playerID = getPlayerConnectedToAltar(candidatePos);
            if (playerID != null) {
                resultAltars.put(candidatePos, playerID);
            }
        }
        return resultAltars;
    }

    private UUID getPlayerConnectedToAltar(WorldBlockPos candidatePos) {
        // TODO: this could be more efficient by using data shortcuts.
        UUID foundPlayer = null;
        for (UUID candidateID : this.players.keySet()) {
            if (candidatePos.isEqualTo(this.players.get(candidateID).getEssencePool().getAltarPos())) {
                foundPlayer = candidateID;
                break;
            }
        }
        return foundPlayer;
    }

    public String toJsonString() {
        // TODO: implement this...
        return this.toString();
    }

    public boolean playerHasAltar(UUID playerid) {
        return this.players.containsKey(playerid) && this.players.get(playerid).getEssencePool() != null
                && this.players.get(playerid).getEssencePool().getAltarPos() != null;
    }

    public boolean playerHasAltar(ServerPlayerEntity serverPlayerEntity) {
        UUID id = serverPlayerEntity.getGameProfile().getId();
        return playerHasAltar(id);
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
        return this.players.get(playerID).knowsSpell(spellname);
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
                this.players.get(playerID).learnSpell(spellname, new SpellData(spells.get(spellname)),
                        player.getUuid());
                success = true;
            } else {
                int spellcost = getLevelCost(spellname);
                if (player.experienceLevel >= spellcost) {
                    // player.getServer().level
                    player.addExperienceLevels(-spellcost);
                    this.players.get(playerID).learnSpell(spellname, new SpellData(spells.get(spellname)),
                            player.getUuid());

                    success = true;
                } else {
                    player.sendMessage(Text.translatable("skullmagic.message.missing_required_level")
                            .append(Integer.toString(spellcost)), true);
                }
            }
        }
        return success;
    }

    public static String[] getSpellNames() {
        return Parsing.setToStringArr(spells.keySet());
    }

    public void removeSpellShrine(WorldBlockPos worldBlockPos) {
        for (UUID playerToUpdate : this.players.keySet()) {
            PlayerData data = this.players.get(playerToUpdate);
            if (data.tryRemoveSpellShrine(worldBlockPos, playerToUpdate)) {
                break;
            }
        }
    }

    public void tryRemoveSpellPedestal(ServerWorld world, BlockPos pos, String type) {
        for (UUID playerID : this.players.keySet()) {
            PlayerData data = this.players.get(playerID);
            if (data.tryRemoveSpellPedestal(new WorldBlockPos(pos, world.getRegistryKey()), playerID)) {
                break;
            }
        }
    }

    public boolean tryAddSpellPedestal(ServerWorld world, BlockPos pos, UUID placerID, String spellname,
            ASpellPedestal aSpellPedestal) {
        boolean result = false;

        if (playerKnowsSpell(placerID, spellname)) {
            if (players.get(placerID)
                    .tryAddSpellPedestal(new WorldBlockPos(pos, world.getRegistryKey()), spellname, aSpellPedestal.type,
                            aSpellPedestal.level, placerID)) {
                result = true;
            } else {
                for (UUID candidateID : this.players.keySet()) {
                    if (playerKnowsSpell(candidateID, spellname)
                            && players.get(candidateID).tryAddSpellPedestal(
                                    new WorldBlockPos(pos, world.getRegistryKey()),
                                    spellname, aSpellPedestal.type, aSpellPedestal.level, candidateID)) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    public void learnAllSpellsForPlayer(ServerPlayerEntity player) {
        for (String spellname : spells.keySet()) {
            if (!playerKnowsSpell(player.getUuid(), spellname)) {
                this.players.get(player.getUuid()).learnSpell(spellname, new SpellData(spells.get(spellname)),
                        player.getUuid());
            }
        }
    }

    public boolean tryCastSpell(String spellname, ServerPlayerEntity player, World world) {
        UUID playerID = player.getUuid();
        boolean result = false;
        if (playerKnowsSpell(playerID, spellname)) {
            if (isSpellOffCoolown(playerID, spellname)) {
                int essenceCost = getEssenceCostForSpell(playerID, spellname);
                if (canAffordEssenceCost(playerID, essenceCost)) {
                    result = castSpell(player, spellname);
                }
            }
        }
        return result;
    }

    private boolean canAffordEssenceCost(UUID playerID, int essenceCost) {
        return this.players.get(playerID).canAfford(essenceCost);
    }

    private int getEssenceCostForSpell(UUID playerID, String spellname) {
        return this.players.get(playerID).getEssenceCostForSpell(spellname);
    }

    private boolean isSpellOffCoolown(UUID playerID, String spellname) {
        return players.get(playerID).isSpellOffCoolown(spellname);
    }

    private boolean playerKnowsSpell(UUID playerID, String spellname) {
        return this.players.get(playerID).knowsSpell(spellname);
    }

    public boolean castSpell(ServerPlayerEntity player, String spellname) {
        boolean result = false;
        UUID playerID = player.getUuid();
        double powerLevel = getSpellPowerLevel(playerID, spellname);
        if (spells.get(spellname).action.apply(player, powerLevel)) {
            setSpellOnCooldown(playerID, spellname);
            dischargeSpellcost(playerID, spellname);
            ServerPackageSender.sendEffectPackageToPlayers(player.getWorld().getPlayers(), spellname, powerLevel,
                    player.getWorld().getRegistryKey(),
                    player.getEyePos());
            result = true;
        }
        return result;
    }

    private void dischargeSpellcost(UUID playerID, String spellname) {
        int essenceCost = this.players.get(playerID).getEssenceCostForSpell(spellname);
        this.players.get(playerID).getEssencePool().dischargeEssence(essenceCost, playerID);
    }

    private void setSpellOnCooldown(UUID playerID, String spellname) {
        this.players.get(playerID).setSpellOnCooldown(spellname, playerID);
    }

    private double getSpellPowerLevel(UUID playerID, String spellname) {
        return this.players.get(playerID).getSpellPower(spellname);
    }

    public static SpellData getDefaultSpellData(String spellname) {
        return new SpellData(spells.get(spellname));
    }

    public void removeSpellShrineForPlayer(ServerWorld world, BlockPos pos, UUID playerid) {
        this.players.get(playerid).removeSpellShrine(pos, playerid);
    }

    public void addNewSpellShrineForPlayer(ServerWorld world, BlockPos pos, UUID playerid, String spellname) {
        if (playerKnowsSpell(playerid, spellname) && !playerHasSpellShrine(playerid, spellname)) {
            PlayerData data = this.players.get(playerid);
            Vec3i rangevec = new Vec3i(Config.getConfig().scanWidth, Config.getConfig().scanHeight,
                    Config.getConfig().scanWidth);
            Box box = new Box(pos.subtract(rangevec), pos.add(rangevec));
            ArrayList<Tuple<BlockPos, String>> powerPedestalsList = getUnlinkedPowerSpellPedestals(world, box,
                    spellname);
            ArrayList<Tuple<BlockPos, String>> efficiencyPedestalsList = getUnlinkedEfficiencySpellPedestals(world, box,
                    spellname);
            ArrayList<Tuple<BlockPos, String>> cooldownPedestalsList = getUnlinkedCooldownSpellPedestals(world, box,
                    spellname);

            HashMap<BlockPos, Integer> powerPedestals = new HashMap<>();
            for (Tuple<BlockPos, String> pair : powerPedestalsList) {
                powerPedestals.put(pair.first, Config.getConfig().shrines.get(pair.second));
            }
            HashMap<BlockPos, Integer> efficiencyPedestals = new HashMap<>();
            for (Tuple<BlockPos, String> pair : efficiencyPedestalsList) {
                efficiencyPedestals.put(pair.first, Config.getConfig().shrines.get(pair.second));
            }
            HashMap<BlockPos, Integer> cooldownPedestals = new HashMap<>();
            for (Tuple<BlockPos, String> pair : cooldownPedestalsList) {
                cooldownPedestals.put(pair.first, Config.getConfig().shrines.get(pair.second));
            }

            data.addSpellShrine(spellname, world.getRegistryKey(), pos, powerPedestals, efficiencyPedestals,
                    cooldownPedestals, playerid);
        }
    }

    private ArrayList<Tuple<BlockPos, String>> getUnlinkedCooldownSpellPedestals(ServerWorld world, Box box,
            String spellname) {
        ArrayList<Tuple<BlockPos, String>> results = new ArrayList<>();

        for (CooldownSpellPedestalBlockEntity ent : Util.getBlockEntitiesOfTypeInBox(world, box,
                SkullMagic.COOLDOWN_SPELL_PEDESTAL_BLOCK_ENTITY)) {
            BlockPos pos = ent.getPos();
            if (ent.getSpellName() != null && ent.getSpellName().equals(spellname)
                    && isSpellPedestalUnbound(new WorldBlockPos(pos, world.getRegistryKey()))) {
                results.add(new Tuple<BlockPos, String>(pos,
                        Registries.BLOCK.getId(ent.getCachedState().getBlock()).toString()));
            }
        }

        return results;
    }

    private ArrayList<Tuple<BlockPos, String>> getUnlinkedEfficiencySpellPedestals(ServerWorld world, Box box,
            String spellname) {
        ArrayList<Tuple<BlockPos, String>> results = new ArrayList<>();

        for (EfficiencySpellPedestalBlockEntity ent : Util.getBlockEntitiesOfTypeInBox(world, box,
                SkullMagic.EFFICIENCY_SPELL_PEDESTAL_BLOCK_ENTITY)) {
            BlockPos pos = ent.getPos();
            if (ent.getSpellName() != null && ent.getSpellName().equals(spellname)
                    && isSpellPedestalUnbound(new WorldBlockPos(pos, world.getRegistryKey()))) {
                results.add(new Tuple<BlockPos, String>(pos,
                        Registries.BLOCK.getId(ent.getCachedState().getBlock()).toString()));
            }
        }

        return results;
    }

    private ArrayList<Tuple<BlockPos, String>> getUnlinkedPowerSpellPedestals(ServerWorld world, Box box,
            String spellname) {
        ArrayList<Tuple<BlockPos, String>> results = new ArrayList<>();

        for (PowerSpellPedestalBlockEntity ent : Util.getBlockEntitiesOfTypeInBox(world, box,
                SkullMagic.POWER_SPELL_PEDESTAL_BLOCK_ENTITY)) {
            BlockPos pos = ent.getPos();
            if (ent.getSpellName() != null && ent.getSpellName().equals(spellname)
                    && isSpellPedestalUnbound(new WorldBlockPos(pos, world.getRegistryKey()))) {
                results.add(new Tuple<BlockPos, String>(pos,
                        Registries.BLOCK.getId(ent.getCachedState().getBlock()).toString()));
            }
        }

        return results;
    }

    private boolean isSpellPedestalUnbound(WorldBlockPos worldBlockPos) {
        boolean result = false;
        for (PlayerData data : this.players.values()) {
            if (data.hasSpellPedestal(worldBlockPos)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public boolean playerHasSpellShrine(UUID playerid, String spellname) {
        return this.players.get(playerid).hasSpellShrine(spellname);
    }

    public BlockPos getSpellShrineForPlayer(UUID playerid, String spellname) {
        return this.players.get(playerid).getSpellShrine(spellname);
    }

    public boolean canConsumerApply(WorldBlockPos pos, int essenceCost) {
        EssencePool essencePool = getEssencePoolForConsumer(pos.worldKey, pos);
        if (essencePool != null) {
            return essencePool.canAfford(essenceCost);
        } else {
            return false;
        }
    }

    public void applyConsumer(WorldBlockPos pos, int essenceCost) {
        for (UUID playerID : this.players.keySet()) {
            if (this.players.get(playerID).hasConsumerAtPos(pos)) {
                this.players.get(playerID).applyConsumer(pos, essenceCost, playerID);
            }
        }
    }

    public ServerPlayerEntity getPlayerForConsumerWorldPos(ServerWorld world, WorldBlockPos worldPos) {
        // TODO: this could be alot more efficient with data shortcuts
        ServerPlayerEntity result = null;
        for (UUID playerID : this.players.keySet()) {
            if (this.players.get(playerID).hasConsumerAtPos(worldPos)) {
                result = (ServerPlayerEntity) world.getPlayerByUuid(playerID);
                break;
            }
        }
        return result;
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

    public void updatePlayer(UUID playerToUpdate) {
        if (!this.playersToUpdate.contains(playerToUpdate)) {
            this.playersToUpdate.add(playerToUpdate);
        }
    }

    public void updateSpellPedestal(ASpellPedestal pedestal, ASpellPedestalBlockEntity ent, String type,
            PlayerEntity player, World world,
            BlockPos pos) {

        // check if the socket is empty
        if (ent.getScroll() == null || ent.getScroll().isOf(Items.AIR)) {
            // if empty, check wether the player holds a valid scroll.
            ItemStack scrollItemStack = player.getMainHandStack();
            if (scrollItemStack.getItem() instanceof KnowledgeOrb) {
                String spellname = ((KnowledgeOrb) scrollItemStack.getItem()).spellName;
                if (this.tryAddSpellPedestal((ServerWorld) world, pos,
                        player.getGameProfile().getId(), spellname, pedestal)) {
                    ent.setScroll(scrollItemStack.copy());
                    if (!player.isCreative()) {
                        scrollItemStack.decrement(1);
                    }
                    world.playSound((double) pos.getX(), (double) pos.getY(), (double) pos.getZ(),
                            SoundEvents.BLOCK_END_PORTAL_FRAME_FILL,
                            SoundCategory.BLOCKS, 1.0f, 1.0f, true);
                    player.sendMessage(Text.of("Bound this spell pedestal to your spell shrine."), true);
                } else {
                    player.sendMessage(
                            Text.of("Could not find a spell shrine linked to you and " + spellname
                                    + " in range."),
                            true);
                }
            } else {
                player.sendMessage(Text.of("Not a valid scroll!"), true);
            }
        } else {
            // if not empty, drop the contained item.
            if (!player.isCreative()) {
                ItemEntity itemEnt = new ItemEntity(world, player.getPos().x, player.getPos().y, player.getPos().z,
                        ent.getScroll());
                itemEnt.setPickupDelay(0);
                world.spawnEntity(itemEnt);
            }
            ent.setScroll(null);
            this.tryRemoveSpellPedestal((ServerWorld) world, pos, type);
        }
    }

    public void updateSpellShrine(ServerWorld world, PlayerEntity player, SpellShrineBlockEntity blockEnt,
            ASpellShrine aSpellShrine) {
        UUID playerid = player.getGameProfile().getId();
        if (blockEnt.getScroll() == null || blockEnt.getScroll().isOf(Items.AIR)) {
            // if empty, check wether the player holds a valid scroll.
            ItemStack itemStack = player.getMainHandStack();
            if (itemStack.getItem() instanceof KnowledgeOrb) {
                String spellname = ((KnowledgeOrb) itemStack.getItem()).spellName;
                // only continue if the player knowns the spell
                if (playerKnowsSpell(playerid, spellname)) {
                    // dont do anything if the player already has a shrine for that spell assigned
                    // to him
                    if (SkullMagic.getServerData().playerHasSpellShrine(playerid, spellname)) {
                        player.sendMessage(
                                Text.of("You already have a shrine for the spell " + spellname
                                        + " assigned to you at "
                                        + SkullMagic.getServerData().getSpellShrineForPlayer(playerid, spellname)
                                                .toShortString()),
                                true);
                    } else {
                        SkullMagic.getServerData().addNewSpellShrineForPlayer((ServerWorld) world, blockEnt.getPos(),
                                player.getGameProfile().getId(), spellname);
                        blockEnt.setScroll(itemStack.copy());
                        if (!player.isCreative()) {
                            itemStack.decrement(1);
                        }
                    }
                } else {
                    player.sendMessage(Text.of("You dont know this spell yet."), true);
                }
            }
        } else {
            // if not empty, drop the contained item.
            if (!player.isCreative()) {
                ItemEntity scroll = new ItemEntity(world, player.getPos().x, player.getPos().y, player.getPos().z,
                        blockEnt.getScroll());

                scroll.setPickupDelay(0);
                world.spawnEntity(scroll);
            }
            blockEnt.setScroll(null);
            this.tryRemoveSpellAltar(world, blockEnt.getPos());
        }
    }
}