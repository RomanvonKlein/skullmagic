package com.romanvonklein.skullmagic.spells;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang3.function.TriFunction;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.config.Config;
import com.romanvonklein.skullmagic.entities.EffectBall;
import com.romanvonklein.skullmagic.essence.EssencePool;
import com.romanvonklein.skullmagic.networking.ServerPackageSender;
import com.romanvonklein.skullmagic.tasks.DelayedTask;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

public class SpellManager extends PersistentState {
    private int cooldownIntervall = 10;
    private int remainingCooldown = cooldownIntervall;
    public Map<UUID, Map<String, Integer>> availableSpells = new HashMap<>();

    public static Map<String, ? extends Spell> SpellDict = Map.of(
            "fireball",
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
            "selfheal",
            new Spell(50, 100, new TriFunction<ServerPlayerEntity, World, EssencePool, Boolean>() {
                @Override
                public Boolean apply(ServerPlayerEntity player, World world, EssencePool altar) {
                    player.heal(4.0f);
                    return true;
                }
            }),
            "meteoritestorm",
            new Spell(100, 100, new TriFunction<ServerPlayerEntity, World, EssencePool, Boolean>() {
                @Override
                public Boolean apply(ServerPlayerEntity player, World world, EssencePool altar) {
                    int meteoriteCount = 100;
                    int minPower = 1;
                    int maxPower = 5;
                    int height = 256;
                    int radius = 7;
                    int maxDelay = 40;
                    // TODO: way too strong. i love it
                    HitResult result = player.raycast(100, 1, false);
                    if (result != null) {
                        Vec3d center = result.getPos();
                        Vec3f angle = Direction.DOWN.getUnitVector();
                        Random rand = new Random();
                        for (int i = 0; i < meteoriteCount; i++) {
                            DelayedTask tsk = new DelayedTask("meteoritestorm_spell_spawn_meteorites",
                                    rand.nextInt(0, maxDelay),
                                    new TriFunction<Object[], Object, Object, Boolean>() {
                                        @Override
                                        public Boolean apply(Object[] data, Object n1, Object n2) {
                                            FireballEntity ent = new FireballEntity(world, player, angle.getX(),
                                                    angle.getY(), angle.getZ(),
                                                    rand.nextInt(minPower, maxPower));
                                            ent.setPos(center.x - radius + 2 * rand.nextFloat() * radius,
                                                    height, center.z - radius + 2 * rand.nextFloat() * radius);
                                            ent.setVelocity(0, -15, 0);
                                            world.spawnEntity(ent);
                                            return true;
                                        }
                                    }, null);
                            SkullMagic.taskManager.queueTask(tsk);
                        }
                    }
                    return true;
                }
            }),
            "wolfpack",
            new Spell(100, 500, new TriFunction<ServerPlayerEntity, World, EssencePool, Boolean>() {
                @Override
                public Boolean apply(ServerPlayerEntity player, World world, EssencePool altar) {
                    int wolfCount = 3;
                    int wolfLifeTime = 20 * 60;// ~one minute of lifetime
                    ArrayList<WolfEntity> wolfesSpawned = new ArrayList<>();
                    for (int i = 0; i < wolfCount; i++) {
                        WolfEntity wolf = new WolfEntity(EntityType.WOLF, world);
                        world.spawnEntity(wolf);
                        wolf.setTamed(true);
                        wolf.setOwner(player);
                        wolf.setPosition(player.getPos());
                        wolfesSpawned.add(wolf);
                    }
                    SkullMagic.taskManager.queueTask(new DelayedTask("wolfpack_spell_kill_wolfes", wolfLifeTime,
                            new TriFunction<Object[], Object, Object, Boolean>() {
                                @Override
                                public Boolean apply(Object[] data, Object n1, Object n2) {
                                    ArrayList<WolfEntity> wolfes = (ArrayList<WolfEntity>) data[0];
                                    for (WolfEntity wolf : wolfes) {
                                        if (wolf.isAlive()) {
                                            wolf.kill();
                                        }
                                    }
                                    return true;
                                }
                            },
                            new Object[] { wolfesSpawned }));
                    return true;
                }
            }),
            "firebreath",
            new Spell(50, 150, new TriFunction<ServerPlayerEntity, World, EssencePool, Boolean>() {
                @Override
                public Boolean apply(ServerPlayerEntity player, World world, EssencePool altar) {

                    return false;
                }
            }),
            "slowball",
            new Spell(50, 150, new TriFunction<ServerPlayerEntity, World, EssencePool, Boolean>() {
                @Override
                public Boolean apply(ServerPlayerEntity player, World world, EssencePool altar) {
                    if (!world.isClient) {
                        Vec3d velocity = player.getRotationVector().multiply(8.0);
                        EffectBall ball = EffectBall.createEffectBall(world, player, velocity.x, velocity.y, velocity.z,
                                StatusEffects.SLOWNESS, 4.0f);
                        ball.setPosition(player.getCameraEntity().getPos().add(player.getRotationVector().normalize()));
                        world.spawnEntity(ball);
                    }
                    return true;
                }
            }),
            "speedbuff",
            new Spell(50, 150, new TriFunction<ServerPlayerEntity, World, EssencePool, Boolean>() {
                @Override
                public Boolean apply(ServerPlayerEntity player, World world, EssencePool altar) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 500, 1));
                    return true;
                }
            }),
            "resistancebuff",
            new Spell(50, 150, new TriFunction<ServerPlayerEntity, World, EssencePool, Boolean>() {
                @Override
                public Boolean apply(ServerPlayerEntity player, World world, EssencePool altar) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 500, 1));
                    return true;
                }
            }),
            "teleport",
            new Spell(100, 800, new TriFunction<ServerPlayerEntity, World, EssencePool, Boolean>() {
                @Override
                public Boolean apply(ServerPlayerEntity player, World world, EssencePool altar) {
                    boolean success = false;
                    HitResult result = player.raycast(100, 1, false);
                    if (result != null) {
                        Vec3d center = result.getPos();
                        world.playSound(center.x, center.y, center.z, SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                                SoundCategory.PLAYERS, 1.0f, 1.0f, true);
                        player.teleport(center.x, center.y, center.z, true);
                    }
                    return success;
                }
            }),
            "poisonball",
            new Spell(50, 150, new TriFunction<ServerPlayerEntity, World, EssencePool, Boolean>() {
                @Override
                public Boolean apply(ServerPlayerEntity player, World world, EssencePool altar) {
                    if (!world.isClient) {
                        Vec3d velocity = player.getRotationVector().multiply(8.0);
                        EffectBall ball = EffectBall.createEffectBall(world, player, velocity.x, velocity.y, velocity.z,
                                StatusEffects.POISON, 4.0f);
                        ball.setPosition(player.getCameraEntity().getPos().add(player.getRotationVector().normalize()));
                        world.spawnEntity(ball);
                    }
                    return true;
                }
            })/*
               * ,
               * "invisibility",
               * new Spell(50, 150, new TriFunction<ServerPlayerEntity, World, EssencePool,
               * Boolean>() {
               * 
               * @Override
               * public Boolean apply(ServerPlayerEntity player, World world, EssencePool
               * altar) {
               * 
               * return false;
               * }
               * })
               */);

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
        if (this.availableSpells.containsKey(playerID) && SpellDict.containsKey(spellname)
                && !this.availableSpells.get(playerID).containsKey(spellname)) {
            this.availableSpells.get(playerID).put(spellname, 0);
            ServerPackageSender.sendUpdateSpellListPackage(player);
            success = true;
        }
        return success;
    }
}
