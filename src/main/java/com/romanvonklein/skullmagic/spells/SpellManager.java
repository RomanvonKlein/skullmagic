package com.romanvonklein.skullmagic.spells;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.blockEntities.CooldownSpellPedestalBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.EfficiencySpellPedestalBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.PowerSpellPedestalBlockEntity;
import com.romanvonklein.skullmagic.blocks.ASPellPedestal;
import com.romanvonklein.skullmagic.config.Config;
import com.romanvonklein.skullmagic.essence.EssencePool;
import com.romanvonklein.skullmagic.networking.ServerPackageSender;
import com.romanvonklein.skullmagic.util.MathUtil;
import com.romanvonklein.skullmagic.util.Parsing;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

public class SpellManager extends PersistentState {
    private int cooldownIntervall = 10;
    private int remainingCooldown = cooldownIntervall;
    public Map<UUID, Map<String, PlayerSpellData>> availableSpells = new HashMap<>();

    public Map<UUID, Map<String, BlockPos>> playerToSpellShrine = new HashMap<>();

    public Map<RegistryKey<World>, Map<BlockPos, SpellShrinePool>> spellShrinePools = new HashMap<>();
    private Map<UUID, SpellShrinePool> playersToSpellPools = new HashMap<>();
    private Map<RegistryKey<World>, Map<BlockPos, SpellShrinePool>> powerPedestalsToSpellShrinePools = new HashMap<>();
    private Map<RegistryKey<World>, Map<BlockPos, SpellShrinePool>> efficiencyPedestalsToSpellShrinePools = new HashMap<>();
    private Map<RegistryKey<World>, Map<BlockPos, SpellShrinePool>> cooldownPedestalsToSpellShrinePools = new HashMap<>();

    public static Map<String, ? extends Spell> SpellDict = SpellInitializer.initSpells();

    @Override
    public boolean isDirty() {
        return true;
    }

    public HashMap<BlockPos, ArrayList<BlockPos>> getAllShrinePools(UUID playerID, RegistryKey<World> currentWorld) {
        HashMap<BlockPos, ArrayList<BlockPos>> result = new HashMap<>();
        if (playerToSpellShrine.containsKey(playerID)) {
            for (BlockPos spellShrinePos : playerToSpellShrine.get(playerID).values()) {
                ArrayList<BlockPos> altarConnections = new ArrayList<BlockPos>();
                // add power pedestals
                if (powerPedestalsToSpellShrinePools.containsKey(currentWorld)) {
                    for (BlockPos pedestalPos : powerPedestalsToSpellShrinePools.get(currentWorld).keySet()) {
                        if (powerPedestalsToSpellShrinePools.get(currentWorld).get(pedestalPos).position
                                .equals(spellShrinePos)) {
                            // if the spell shrine is connected to the spell altar, add it to its list
                            altarConnections.add(pedestalPos);
                        }
                    }
                }
                if (efficiencyPedestalsToSpellShrinePools.containsKey(currentWorld)) {
                    // add efficiency pedestals
                    for (BlockPos pedestalPos : efficiencyPedestalsToSpellShrinePools.get(currentWorld).keySet()) {
                        if (efficiencyPedestalsToSpellShrinePools.get(currentWorld).get(pedestalPos).position
                                .equals(spellShrinePos)) {
                            // if the spell shrine is connected to the spell altar, add it to its list
                            altarConnections.add(pedestalPos);
                        }
                    }
                }
                // add cooldown pedestals
                if (cooldownPedestalsToSpellShrinePools.containsKey(currentWorld)) {
                    for (BlockPos pedestalPos : cooldownPedestalsToSpellShrinePools.get(currentWorld).keySet()) {
                        if (cooldownPedestalsToSpellShrinePools.get(currentWorld).get(pedestalPos).position
                                .equals(spellShrinePos)) {
                            // if the spell shrine is connected to the spell altar, add it to its list
                            altarConnections.add(pedestalPos);
                        }
                    }
                }
                result.put(spellShrinePos, altarConnections);
            }
        }
        return result;
    }

    public boolean castSpell(String spellName, ServerPlayerEntity player,
            World world) {
        boolean success = false;
        if (SpellDict.containsKey(spellName)) {
            // TODO: make some of these factors configurable?
            UUID playerID = player.getGameProfile().getId();
            if (availableSpells.containsKey(playerID)
                    && availableSpells.get(playerID).containsKey(spellName)) {
                PlayerSpellData spellData = availableSpells.get(playerID).get(spellName);
                if (spellData.cooldownLeft <= 0) {
                    EssencePool pool = SkullMagic.essenceManager.getEssencePoolForPlayer(playerID);
                    Spell spell = SpellDict.get(spellName);

                    int reducedEssenceCost = (int) Math.round(
                            spell.essenceCost * (1 + (spellData.getPowerLevel() - 1) / 4)
                                    * (1 - MathUtil.log2(1 + (spellData.getEfficiencyLevel() - 1) * 0.5)));
                    if (pool.getEssence() >= reducedEssenceCost) {
                        spellData.cooldownLeft = spellData.getMaxCooldown(SpellDict.get(spellName).cooldownTicks);

                        success = spell.action.apply(player, spellData, pool);
                        if (success) {
                            pool.discharge(reducedEssenceCost);
                        }
                    }
                }
            }
        }
        return success;
    }

    public void tick(MinecraftServer server) {
        // reduce all cooldowns
        // TODO: this might feel weird, like the cooldowns are inconsistent...
        if (remainingCooldown > 0) {
            remainingCooldown--;
        } else {
            remainingCooldown = cooldownIntervall;
            availableSpells.values().forEach((map) -> {
                map.entrySet().forEach((entry) -> {
                    if (entry.getValue().cooldownLeft > 0) {
                        entry.getValue().cooldownLeft = entry.getValue().cooldownLeft - cooldownIntervall;
                    }
                });
            });
            availableSpells.keySet().forEach((uuid) -> {
                for (ServerWorld world : server.getWorlds()) {
                    ServerPlayerEntity player = (ServerPlayerEntity) world.getPlayerByUuid(uuid);
                    if (player != null
                            && SkullMagic.essenceManager.playerHasEssencePool(player.getGameProfile().getId())) {
                        ServerPackageSender.sendUpdateSpellListPackage(player);
                        break;
                    }
                }
            });
        }
    }

    /**
     * Create a new entry for the joined player, if he does not have one yet.
     * 
     * @param player
     */
    public void playerJoined(ServerPlayerEntity player) {
        if (!this.availableSpells.containsKey(player.getGameProfile().getId())) {
            this.availableSpells.put(player.getGameProfile().getId(), new HashMap<>());
            for (String spellname : Config.getConfig().defaultSpells) {
                this.availableSpells.get(player.getGameProfile().getId()).put(spellname,
                        new PlayerSpellData(0, 1.0, 1.0, 1.0));
            }
        }
        ServerPackageSender.sendUpdateSpellListPackage(player);
        ServerPackageSender.sendUpdateLinksPackage(player);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        // playerSpells
        NbtCompound playerSpellsNBT = new NbtCompound();
        availableSpells.keySet().forEach((uuid) -> {
            NbtCompound playerSpellDataList = new NbtCompound();
            availableSpells.get(uuid).entrySet().forEach((entry) -> {
                PlayerSpellData spellData = entry.getValue();
                NbtCompound playerSpellData = new NbtCompound();
                spellData.writeNbt(playerSpellData);
                playerSpellDataList.put(entry.getKey(), playerSpellData);
            });
            playerSpellsNBT.put(uuid.toString(), playerSpellDataList);
        });
        nbt.put("playerSpells", playerSpellsNBT);
        // shrines
        NbtCompound spellShrines = new NbtCompound();
        spellShrinePools.keySet().forEach((worldkey) -> {
            NbtCompound worldNbt = new NbtCompound();
            spellShrinePools.get(worldkey).keySet().forEach((posStr) -> {
                NbtCompound poolNbt = new NbtCompound();
                SpellShrinePool pool = spellShrinePools.get(worldkey).get(posStr);
                pool.writeNbt(poolNbt);
                worldNbt.put(pool.position.toShortString(), poolNbt);
            });

            spellShrines.put(worldkey.getValue().toString(), worldNbt);
        });
        nbt.put("spellShrines", spellShrines);
        return nbt;
    }

    public static SpellManager fromNbt(NbtCompound tag) {
        SpellManager spmngr = new SpellManager();
        if (tag.contains("playerSpells")) {
            NbtCompound playerSpells = tag.getCompound("playerSpells");
            playerSpells.getKeys().forEach((uuidStr) -> {
                spmngr.availableSpells.put(UUID.fromString(uuidStr), new HashMap<>());
                playerSpells.getCompound(uuidStr).getKeys().forEach((spellname) -> {
                    spmngr.availableSpells.get(UUID.fromString(uuidStr)).put(spellname,
                            PlayerSpellData.fromNbt(playerSpells.getCompound(uuidStr)));
                });
            });
        }
        // spellshrines
        if (tag.contains("spellShrines")) {
            NbtCompound spellShrines = tag.getCompound("spellShrines");
            spellShrines.getKeys().forEach((worldKey) -> {
                RegistryKey<World> key = RegistryKey.of(net.minecraft.util.registry.Registry.WORLD_KEY,
                        Identifier.tryParse(worldKey));
                spmngr.spellShrinePools.put(key, new HashMap<>());
                NbtCompound worldList = spellShrines.getCompound(worldKey);
                worldList.getKeys().forEach((spellPoolPosString) -> {
                    SpellShrinePool pool = SpellShrinePool.fromNbt(worldList.getCompound(spellPoolPosString));
                    spmngr.spellShrinePools.get(key).put(Parsing.shortStringToBlockPos(spellPoolPosString),
                            pool);
                    if (pool.linkedPlayerID != null) {
                        spmngr.playersToSpellPools.put(pool.linkedPlayerID, pool);
                    }
                    for (BlockPos blockPos : pool.linkedPowerPedestals.keySet()) {
                        if (!spmngr.powerPedestalsToSpellShrinePools.containsKey(key)) {
                            spmngr.powerPedestalsToSpellShrinePools.put(key, new HashMap<>());
                        }
                        spmngr.powerPedestalsToSpellShrinePools.get(key).put(blockPos, pool);
                    }
                    spmngr.spellShrinePools.get(key).put(Parsing.shortStringToBlockPos(spellPoolPosString), pool);

                });
            });
        }
        return spmngr;
    }

    public boolean learnSpell(ServerPlayerEntity player, String spellname, boolean force) {
        UUID playerID = player.getGameProfile().getId();
        boolean success = false;
        if (this.availableSpells.containsKey(playerID) && SpellDict.containsKey(spellname)
                && !this.availableSpells.get(playerID).containsKey(spellname)) {
            // check player level and deduct if sufficient
            if (force) {
                this.availableSpells.get(playerID).put(spellname, new PlayerSpellData(0, 1.0, 1.0, 1.0));
                ServerPackageSender.sendUpdateSpellListPackage(player);
                success = true;
            } else {
                int spellcost = SpellManager.getLevelCost(spellname);
                if (player.experienceLevel >= spellcost) {
                    // player.getServer().level
                    player.addExperienceLevels(-spellcost);
                    this.availableSpells.get(playerID).put(spellname, new PlayerSpellData(0, 1.0, 1.0, 1.0));
                    ServerPackageSender.sendUpdateSpellListPackage(player);
                    success = true;
                } else {
                    player.sendMessage(new TranslatableText("skullmagic.message.missing_required_level")
                            .append(Integer.toString(spellcost)), true);
                }
            }
        }
        return success;
    }

    public static int getLevelCost(String spellName) {
        int result = 0;
        if (SpellDict.containsKey(spellName)) {
            result = SpellDict.get(spellName).learnLevelCost;
        }
        return result;
    }

    public void learnAllSpellsForPlayer(ServerPlayerEntity player) {
        for (String spellname : SpellDict.keySet()) {
            learnSpell(player, spellname, true);
        }
    }

    public void removeSpellShrine(ServerWorld world, BlockPos pos) {
        RegistryKey<World> key = world.getRegistryKey();
        ServerPlayerEntity player = null;
        if (this.spellShrinePools.containsKey(key) && this.spellShrinePools.get(key).containsKey(pos)) {
            UUID playerID = this.spellShrinePools.get(world.getRegistryKey()).get(pos).linkedPlayerID;
            player = (ServerPlayerEntity) world.getPlayerByUuid(playerID);
        }

        removeSpellShrine(world, pos, player);

    }

    public void removeSpellShrine(ServerWorld world, BlockPos pos, ServerPlayerEntity player) {
        RegistryKey<World> registryKey = world.getRegistryKey();
        if (this.spellShrinePools.containsKey(registryKey) && this.spellShrinePools.get(registryKey).containsKey(pos)) {
            SpellShrinePool pool = this.spellShrinePools.get(registryKey).get(pos);
            if (pool.linkedPlayerID != null) {
                this.resetSpellUpgradesForPlayer(pool.linkedPlayerID, pool.getSpellName());
                if (this.playerToSpellShrine.containsKey(pool.linkedPlayerID)
                        && this.playerToSpellShrine.get(pool.linkedPlayerID).containsKey(pool.getSpellName())) {
                    this.playerToSpellShrine.get(pool.linkedPlayerID).remove(pool.getSpellName());
                }
                this.playersToSpellPools.remove(pool.linkedPlayerID);
            }
            // also send the player an update about his spell shrines, if they are online
            if (player != null) {
                ServerPackageSender.sendUpdateLinksPackage(player);
            }
        }

    }

    public void resetSpellUpgradesForPlayer(UUID linkedPlayerID, String spellName) {
        this.availableSpells.get(linkedPlayerID).put(spellName, new PlayerSpellData());
    }

    public void addNewSpellShrine(ServerWorld world, BlockPos pos, UUID playerID, String spellname) {
        RegistryKey<World> registryKey = world.getRegistryKey();
        SpellShrinePool pool = new SpellShrinePool(playerID, pos, spellname,
                world.getBlockEntity(pos, SkullMagic.SPELL_SHRINE_BLOCK_ENTITY).get().level
                        * Config.getConfig().shrineRangePerLevel);
        if (!this.spellShrinePools.containsKey(registryKey)) {
            this.spellShrinePools.put(registryKey, new HashMap<>());
        }
        this.spellShrinePools.get(registryKey).put(pos, pool);
        if (!this.playerToSpellShrine.containsKey(playerID)) {
            playerToSpellShrine.put(playerID, new HashMap<>());
        }
        playerToSpellShrine.get(playerID).put(spellname, pos);
        this.addNearbySpellPedestals(world, pos, spellname);
        // also send the player an update about his spell shrines, if they are online
        ServerPlayerEntity player = (ServerPlayerEntity) world.getPlayerByUuid(playerID);
        if (player != null) {
            ServerPackageSender.sendUpdateLinksPackage(player);
        }
    }

    private void addNearbySpellPedestals(ServerWorld world, BlockPos pos, String spellname) {
        RegistryKey<World> registryKey = world.getRegistryKey();
        SpellShrinePool pool = this.spellShrinePools.get(registryKey).get(pos);

        for (int x = pos.getX() - pool.range; x < pos.getX() + pool.range; x++) {
            for (int y = pos.getY() - pool.range; y < pos.getY() + pool.range; y++) {
                for (int z = pos.getZ() - pool.range; z < pos.getZ() + pool.range; z++) {
                    BlockPos candidatePos = new BlockPos(x, y, z);
                    Optional<PowerSpellPedestalBlockEntity> powerOpt = world.getBlockEntity(candidatePos,
                            SkullMagic.POWER_SPELL_PEDESTAL_BLOCK_ENTITY);
                    if (powerOpt.isPresent()) {
                        if (!this.powerPedestalsToSpellShrinePools.containsKey(registryKey)
                                || !this.powerPedestalsToSpellShrinePools.get(registryKey).containsKey(pos)) {
                            String spellName = powerOpt.get().getSpellName();
                            if (spellName != null && spellName.equals(spellname)) {
                                addPedestalLink(world, pool, candidatePos,
                                        PowerSpellPedestalBlockEntity.type);
                            }
                        }
                    } else {
                        Optional<EfficiencySpellPedestalBlockEntity> efficiencyOpt = world.getBlockEntity(candidatePos,
                                SkullMagic.EFFICIENCY_SPELL_PEDESTAL_BLOCK_ENTITY);
                        if (efficiencyOpt.isPresent()) {
                            if (!this.efficiencyPedestalsToSpellShrinePools.containsKey(registryKey)
                                    || !this.efficiencyPedestalsToSpellShrinePools.get(registryKey).containsKey(pos)) {
                                String spellName = efficiencyOpt.get().getSpellName();
                                if (spellName != null && spellName.equals(spellname)) {
                                    addPedestalLink(world, pool, candidatePos,
                                            EfficiencySpellPedestalBlockEntity.type);
                                }
                            }
                        } else {
                            Optional<CooldownSpellPedestalBlockEntity> cooldownOpt = world.getBlockEntity(
                                    candidatePos,
                                    SkullMagic.COOLDOWN_SPELL_PEDESTAL_BLOCK_ENTITY);
                            if (cooldownOpt.isPresent()) {
                                if (!this.cooldownPedestalsToSpellShrinePools.containsKey(registryKey)
                                        || !this.cooldownPedestalsToSpellShrinePools.get(registryKey)
                                                .containsKey(pos)) {
                                    String spellName = cooldownOpt.get().getSpellName();
                                    if (spellName != null && spellName.equals(spellname)) {
                                        addPedestalLink(world, pool, candidatePos,
                                                CooldownSpellPedestalBlockEntity.type);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean tryAddSpellPedestal(ServerWorld world, BlockPos pos, UUID id, String spellname,
            ASPellPedestal pedestal) {
        RegistryKey<World> registryKey = world.getRegistryKey();
        boolean result = false;
        if (this.spellShrinePools.containsKey(registryKey)) {
            for (Entry<BlockPos, SpellShrinePool> entry : this.spellShrinePools.get(registryKey).entrySet()) {
                if (canConnectPedestalToPool(pos, spellname, entry.getValue(), entry.getKey(), id)) {
                    addPedestalLink(world, entry.getValue(), pos, pedestal.type);
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public void addPedestalLink(ServerWorld world, SpellShrinePool pool, BlockPos pedestalPos,
            String type) {
        RegistryKey<World> registryKey = world.getRegistryKey();
        if (type.equals("power")) {
            if (!this.powerPedestalsToSpellShrinePools.containsKey(registryKey)) {
                this.powerPedestalsToSpellShrinePools.put(registryKey, new HashMap<>());
            }
            this.powerPedestalsToSpellShrinePools.get(registryKey).put(pedestalPos, pool);
            pool.addPowerPedestal(world, pedestalPos, 1);// TODO: implement pedestal strength here
        } else if (type.equals("efficiency")) {
            if (!this.efficiencyPedestalsToSpellShrinePools.containsKey(registryKey)) {
                this.efficiencyPedestalsToSpellShrinePools.put(registryKey, new HashMap<>());
            }
            this.efficiencyPedestalsToSpellShrinePools.get(registryKey).put(pedestalPos, pool);
            pool.addEfficiencyPedestal(world, pedestalPos, 1);// TODO: implement pedestal strength here

        } else if (type.equals("cooldown")) {
            if (!this.cooldownPedestalsToSpellShrinePools.containsKey(registryKey)) {
                this.cooldownPedestalsToSpellShrinePools.put(registryKey, new HashMap<>());
            }
            this.cooldownPedestalsToSpellShrinePools.get(registryKey).put(pedestalPos, pool);
            pool.addCooldownPedestal(world, pedestalPos, 1);// TODO: implement pedestal strength here

        } else {
            SkullMagic.LOGGER.warn("unknown type of spellpedestal: '" + type + "'");
        }
    }

    private boolean canConnectPedestalToPool(BlockPos pedestalPos, String pedestalSpellName, SpellShrinePool pool,
            BlockPos pos, UUID playerID) {
        BlockPos diff = pedestalPos.subtract(pos);
        return Math.abs(diff.getX()) <= pool.range && Math.abs(diff.getY()) <= pool.range
                && Math.abs(diff.getZ()) <= pool.range && pool.getSpellName().equals(pedestalSpellName)
                && pool.linkedPlayerID.equals(playerID);
    }

    public void removeSpellPedestal(ServerWorld world, BlockPos pos, String type) {

        RegistryKey<World> registryKey = world.getRegistryKey();
        if (type.equals("power")) {
            if (this.powerPedestalsToSpellShrinePools.containsKey(registryKey)
                    && this.powerPedestalsToSpellShrinePools.get(registryKey).containsKey(pos)) {
                SpellShrinePool pool = this.powerPedestalsToSpellShrinePools.get(registryKey).get(pos);
                pool.removePedestal(world, pos, type);
                this.powerPedestalsToSpellShrinePools.get(registryKey).remove(pos);
            }
        } else if (type.equals("efficiency")) {
            if (this.efficiencyPedestalsToSpellShrinePools.containsKey(registryKey)
                    && this.efficiencyPedestalsToSpellShrinePools.get(registryKey).containsKey(pos)) {
                SpellShrinePool pool = this.efficiencyPedestalsToSpellShrinePools.get(registryKey).get(pos);
                pool.removePedestal(world, pos, type);
                this.efficiencyPedestalsToSpellShrinePools.get(registryKey).remove(pos);
            }
        } else if (type.equals("cooldown")) {
            if (this.cooldownPedestalsToSpellShrinePools.containsKey(registryKey)
                    && this.cooldownPedestalsToSpellShrinePools.get(registryKey).containsKey(pos)) {
                SpellShrinePool pool = this.cooldownPedestalsToSpellShrinePools.get(registryKey).get(pos);
                pool.removePedestal(world, pos, type);
                this.cooldownPedestalsToSpellShrinePools.get(registryKey).remove(pos);
            }
        } else {
            SkullMagic.LOGGER.warn("unknown type of spellpedestal: '" + type + "'");
        }
    }

    public PlayerSpellData getSpellData(UUID linkedPlayerID, String spellName) {
        return this.availableSpells.get(linkedPlayerID).get(spellName);
    }
}
