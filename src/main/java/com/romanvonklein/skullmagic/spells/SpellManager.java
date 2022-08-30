package com.romanvonklein.skullmagic.spells;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.function.TriFunction;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.config.Config;
import com.romanvonklein.skullmagic.networking.ServerPackageSender;
import com.romanvonklein.skullmagic.persistantState.EssencePool;

import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

public class SpellManager extends PersistentState {
    private int cooldownIntervall = 10;
    private int remainingCooldown = cooldownIntervall;
    public Map<UUID, Map<String, Integer>> availableSpells = new HashMap<>();

    public static Map<String, ? extends Spell> SpellDict = Map.of("fireball",
            new Spell(100, 100, new TriFunction<ServerPlayerEntity, World, EssencePool, Boolean>() {
                @Override
                public Boolean apply(ServerPlayerEntity player, World world, EssencePool altar) {
                    Vec3d angle = player.getRotationVector();
                    Vec3d pos = player.getPos();
                    FireballEntity ent = new FireballEntity(world, player, angle.getX(), angle.getY(), angle.getZ(), 1);
                    ent.setPos(pos.x, pos.y + player.getHeight(), pos.z);
                    world.spawnEntity(ent);
                    return true;
                }
            }),
            "selfheal", new Spell(50, 100, new TriFunction<ServerPlayerEntity, World, EssencePool, Boolean>() {
                @Override
                public Boolean apply(ServerPlayerEntity player, World world, EssencePool altar) {
                    player.heal(1.0f);
                    return true;
                }
            }));

    public boolean castSpell(String spellName, ServerPlayerEntity player,
            World world) {
        boolean success = false;
        if (SpellDict.containsKey(spellName)) {
            UUID playerID = player.getUuid();
            if (availableSpells.containsKey(playerID)
                    && availableSpells.get(playerID).containsKey(spellName)) {
                if (availableSpells.get(playerID).get(spellName) <= 0) {
                    EssencePool pool = SkullMagic.essenceManager.getEssencePoolForPlayer(playerID);
                    Spell spell = SpellDict.get(spellName);
                    if (pool.getEssence() >= spell.essenceCost) {
                        availableSpells.get(playerID).put(spellName, SpellDict.get(spellName).cooldownTicks);
                        success = spell.action.apply(player, world, pool);
                        if (success) {
                            pool.discharge(spell.essenceCost);
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
                    if (entry.getValue() > 0) {
                        entry.setValue(entry.getValue() - cooldownIntervall);
                    }
                });
            });
            availableSpells.keySet().forEach((uuid) -> {
                for (ServerWorld world : server.getWorlds()) {
                    ServerPlayerEntity player = (ServerPlayerEntity) world.getPlayerByUuid(uuid);
                    if (player != null) {
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
        if (!this.availableSpells.containsKey(player.getUuid())) {
            SkullMagic.LOGGER.info("Creating new entry for player with id '" + player.getUuidAsString() + "'");
            this.availableSpells.put(player.getUuid(), new HashMap<>());
            for (String spellname : Config.getConfig().defaultSpells) {
                SkullMagic.LOGGER.info("adding Spell '" + spellname + "'");
                this.availableSpells.get(player.getUuid()).put(spellname, 0);
            }
        }
        ServerPackageSender.sendUpdateSpellListPackage(player);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound playerSpellsNBT = new NbtCompound();
        availableSpells.keySet().forEach((uuid) -> {
            NbtCompound playerCooldownsList = new NbtCompound();
            availableSpells.get(uuid).entrySet().forEach((entry) -> {
                playerCooldownsList.putInt(entry.getKey(), entry.getValue());
            });
            playerSpellsNBT.put(uuid.toString(), playerCooldownsList);
        });
        nbt.put("playerSpells", playerSpellsNBT);
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
                            playerSpells.getCompound(uuidStr).getInt(spellname));
                });
            });
        }
        return spmngr;
    }

    public boolean learnSpell(ServerPlayerEntity player, String spellname) {
        UUID playerID = player.getUuid();
        boolean success = false;
        if (this.availableSpells.containsKey(playerID) && SpellDict.containsKey(spellname)) {
            this.availableSpells.get(playerID).put(spellname, 0);
            ServerPackageSender.sendUpdateSpellListPackage(player);
            success = true;
        }
        return success;
    }
}
