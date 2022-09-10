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
import com.romanvonklein.skullmagic.entities.FireBreath;
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
import net.minecraft.text.TranslatableText;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
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
            new Spell(100, 100, 15, new TriFunction<ServerPlayerEntity, World, EssencePool, Boolean>() {
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
            new Spell(50, 100, 15, new TriFunction<ServerPlayerEntity, World, EssencePool, Boolean>() {
                @Override
                public Boolean apply(ServerPlayerEntity player, World world, EssencePool altar) {
                    player.heal(4.0f);
                    return true;
                }
            }),
            "meteoritestorm",
            new Spell(100, 100, 40, new TriFunction<ServerPlayerEntity, World, EssencePool, Boolean>() {
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
            new Spell(100, 500, 25, new TriFunction<ServerPlayerEntity, World, EssencePool, Boolean>() {
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
            new Spell(50, 150, 15, new TriFunction<ServerPlayerEntity, World, EssencePool, Boolean>() {
                @Override
                public Boolean apply(ServerPlayerEntity player, World world, EssencePool altar) {
                    int shotsPerTick = 2;
                    int tickDuration = 30;
                    int breathLife = 20;
                    int burnDuration = 30;
                    for (int i = 0; i < tickDuration; i++) {// TODO: making this one single task may make it more memory
                                                            // efficient.
                        SkullMagic.taskManager.queueTask(new DelayedTask("spawn_fire_breath_task", i,
                                new TriFunction<Object[], Object, Object, Boolean>() {
                                    @Override
                                    public Boolean apply(Object[] data, Object n1, Object n2) {
                                        int shotsPerTick = ((int[]) data[0])[0];
                                        int breathLife = ((int[]) data[0])[1];
                                        int burnDuration = ((int[]) data[0])[2];

                                        Random rand = new Random();
                                        Vec3d dir = player.getRotationVector().normalize();

                                        for (int i = 0; i < shotsPerTick; i++) {
                                            FireBreath entity = FireBreath.createFireBreath(world, player,
                                                    dir.x + rand.nextFloat() * 0.5,
                                                    dir.y + rand.nextFloat() * 0.5, dir.z + rand.nextFloat() * 0.5,
                                                    burnDuration, breathLife);
                                            entity.setPosition(
                                                    player.getPos().add(dir.multiply(0.5))
                                                            .add(0, player.getEyeHeight(player.getPose()), 0));
                                            world.spawnEntity(entity);
                                        }
                                        return true;
                                    }
                                },
                                new Object[] { new int[] { shotsPerTick, breathLife, burnDuration } }));
                    }
                    return true;
                }
            }),
            "slowball",
            new Spell(50, 150, 5, new TriFunction<ServerPlayerEntity, World, EssencePool, Boolean>() {
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
            new Spell(50, 150, 5, new TriFunction<ServerPlayerEntity, World, EssencePool, Boolean>() {
                @Override
                public Boolean apply(ServerPlayerEntity player, World world, EssencePool altar) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 500, 1));
                    return true;
                }
            }),
            "resistancebuff",
            new Spell(50, 150, 10, new TriFunction<ServerPlayerEntity, World, EssencePool, Boolean>() {
                @Override
                public Boolean apply(ServerPlayerEntity player, World world, EssencePool altar) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 500, 1));
                    return true;
                }
            }),
            "teleport",
            new Spell(100, 800, 30, new TriFunction<ServerPlayerEntity, World, EssencePool, Boolean>() {
                @Override
                public Boolean apply(ServerPlayerEntity player, World world, EssencePool altar) {
                    boolean success = false;
                    HitResult result = player.raycast(100, 1, false);
                    if (result != null) {
                        Vec3d center = result.getPos();
                        world.playSound(null, new BlockPos(center),
                                SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1f, 1f);
                        player.teleport(center.x, center.y, center.z, true);
                        success = true;
                    }
                    return success;
                }
            }),
            "poisonball",
            new Spell(50, 150, 10, new TriFunction<ServerPlayerEntity, World, EssencePool, Boolean>() {
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
                    if (player != null && SkullMagic.essenceManager.playerHasEssencePool(player.getUuid())) {
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
            this.availableSpells.put(player.getUuid(), new HashMap<>());
            for (String spellname : Config.getConfig().defaultSpells) {
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
            // check player level and deduct if sufficient
            int spellcost = SpellManager.getLevelCost(spellname);
            if (player.experienceLevel >= spellcost) {
                // player.getServer().level
                player.addExperienceLevels(-spellcost);
                this.availableSpells.get(playerID).put(spellname, 0);
                ServerPackageSender.sendUpdateSpellListPackage(player);
                success = true;
            } else {
                player.sendMessage(new TranslatableText("skullmagic.message.missing_required_level")
                        .append(Integer.toString(spellcost)), true);
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
}
