package com.romanvonklein.skullmagic.spells;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.blockEntities.SpellPedestalBlockEntity;
import com.romanvonklein.skullmagic.config.Config;
import com.romanvonklein.skullmagic.essence.EssencePool;
import com.romanvonklein.skullmagic.networking.ServerPackageSender;
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
    private Map<RegistryKey<World>, Map<BlockPos, SpellShrinePool>> pedestalsToSpellShrinePools = new HashMap<>();

    public static Map<String, ? extends Spell> SpellDict = SpellInitializer.initSpells();

    @Override
    public boolean isDirty() {
        return true;
    }

    public boolean castSpell(String spellName, ServerPlayerEntity player,
            World world) {
        boolean success = false;
        if (SpellDict.containsKey(spellName)) {
            // TODO: make some of these factors configurable
            UUID playerID = player.getGameProfile().getId();
            if (availableSpells.containsKey(playerID)
                    && availableSpells.get(playerID).containsKey(spellName)) {
                PlayerSpellData spellData = availableSpells.get(playerID).get(spellName);
                if (spellData.cooldownLeft <= 0) {
                    EssencePool pool = SkullMagic.essenceManager.getEssencePoolForPlayer(playerID);
                    Spell spell = SpellDict.get(spellName);

                    int reducedEssenceCost = (int) Math.round(
                            spell.essenceCost * (1 + spellData.powerLevel / 4)
                                    * (1 - Math.log(1 + (spellData.efficiencyLevel - 1) * 0.5)));
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
                    for (BlockPos blockPos : pool.linkedPedestals.keySet()) {
                        if (!spmngr.pedestalsToSpellShrinePools.containsKey(key)) {
                            spmngr.pedestalsToSpellShrinePools.put(key, new HashMap<>());
                        }
                        spmngr.pedestalsToSpellShrinePools.get(key).put(blockPos, pool);
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

    public void removeSpellShrine(RegistryKey<World> registryKey, BlockPos pos) {
        if (this.spellShrinePools.containsKey(registryKey) && this.spellShrinePools.get(registryKey).containsKey(pos)) {
            SpellShrinePool pool = this.spellShrinePools.get(registryKey).get(pos);
            if (pool.linkedPlayerID != null) {
                this.resetSpellUpgradesForPlayer(pool.linkedPlayerID, pool.getSpellName());
            }
            this.spellShrinePools.get(registryKey).remove(pos);
        }
    }

    public void resetSpellUpgradesForPlayer(UUID linkedPlayerID, String spellName) {
        this.availableSpells.get(linkedPlayerID).put(spellName, new PlayerSpellData());
    }

    public void addNewSpellShrine(World world, BlockPos pos, UUID playerID, String spellname) {
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
    }

    private void addNearbySpellPedestals(World world, BlockPos pos, String spellname) {
        RegistryKey<World> registryKey = world.getRegistryKey();
        SpellShrinePool pool = this.spellShrinePools.get(registryKey).get(pos);

        for (int x = pos.getX() - pool.range; x < pos.getX() + pool.range; x++) {
            for (int y = pos.getY() - pool.range; y < pos.getY() + pool.range; y++) {
                for (int z = pos.getZ() - pool.range; z < pos.getZ() + pool.range; z++) {
                    BlockPos candidatePos = new BlockPos(x, y, z);
                    Optional<SpellPedestalBlockEntity> opt = world.getBlockEntity(candidatePos,
                            SkullMagic.SPELL_PEDESTAL_BLOCK_ENTITY);
                    if (opt.isPresent()) {

                        if (!this.pedestalsToSpellShrinePools.containsKey(registryKey)
                                || !this.pedestalsToSpellShrinePools.get(registryKey).containsKey(pos)) {
                            String spellName = opt.get().getSpellName();
                            if (spellName != null && spellName.equals(spellname)) {
                                addPedestalLink(registryKey, pool, candidatePos);
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean tryAddSpellPedestal(RegistryKey<World> registryKey, BlockPos pos, UUID id, String spellname) {
        boolean result = false;
        if (!this.spellShrinePools.containsKey(registryKey)) {

        } else {
            for (Entry<BlockPos, SpellShrinePool> entry : this.spellShrinePools.get(registryKey).entrySet()) {
                if (canConnectPedestalToPool(pos, spellname, entry.getValue(), entry.getKey(), id)) {
                    addPedestalLink(registryKey, entry.getValue(), pos);
                    result = true;
                    break;
                }
            }

        }
        return result;
    }

    public void addPedestalLink(RegistryKey<World> registryKey, SpellShrinePool pool, BlockPos pedestalPos) {
        if (!this.pedestalsToSpellShrinePools.containsKey(registryKey)) {
            this.pedestalsToSpellShrinePools.put(registryKey, new HashMap<>());
        }
        this.pedestalsToSpellShrinePools.get(registryKey).put(pedestalPos, pool);
        pool.addPedestal(pedestalPos, 1);// TODO: implement pedestal strength here
    }

    private boolean canConnectPedestalToPool(BlockPos pedestalPos, String pedestalSpellName, SpellShrinePool pool,
            BlockPos pos, UUID playerID) {
        BlockPos diff = pedestalPos.subtract(pos);
        return Math.abs(diff.getX()) <= pool.range && Math.abs(diff.getY()) <= pool.range
                && Math.abs(diff.getZ()) <= pool.range && pool.getSpellName().equals(pedestalSpellName)
                && pool.linkedPlayerID.equals(playerID);
    }

    public void removeSpellPedestal(RegistryKey<World> registryKey, BlockPos pos) {
        if (this.pedestalsToSpellShrinePools.containsKey(registryKey)
                && this.pedestalsToSpellShrinePools.get(registryKey).containsKey(pos)) {
            SpellShrinePool pool = this.pedestalsToSpellShrinePools.get(registryKey).get(pos);
            pool.removePedestal(pos);
            this.pedestalsToSpellShrinePools.get(registryKey).remove(pos);
        }
    }

    public PlayerSpellData getSpellData(UUID linkedPlayerID, String spellName) {
        return this.availableSpells.get(linkedPlayerID).get(spellName);
    }
}
