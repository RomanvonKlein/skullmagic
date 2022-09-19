package com.romanvonklein.skullmagic.spells;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.function.TriFunction;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.entities.EffectBall;
import com.romanvonklein.skullmagic.entities.FireBreath;
import com.romanvonklein.skullmagic.entities.WitherBreath;
import com.romanvonklein.skullmagic.essence.EssencePool;
import com.romanvonklein.skullmagic.tasks.DelayedTask;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class SpellInitializer {

    public static Map<String, ? extends Spell> initSpells() {

        Map<String, Spell> spellList = new HashMap<>();
        spellList.put("fireball",
                new Spell(1000, 100, 15, new TriFunction<ServerPlayerEntity, PlayerSpellData, EssencePool, Boolean>() {
                    @Override
                    public Boolean apply(ServerPlayerEntity player, PlayerSpellData spellData, EssencePool altar) {
                        Vec3d angle = player.getRotationVector().normalize()
                                .multiply(spellData.getPowerLevel() / 2 + 1.5);
                        Vec3d pos = player.getPos();
                        World world = player.world;
                        FireballEntity ent = new FireballEntity(world, player,
                                angle.getX(),
                                angle.getY(),
                                angle.getZ(),
                                Math.max(1, Math.min((int) Math.round(spellData.getPowerLevel()), 5)));
                        ent.setPos(pos.x, pos.y + player.getHeight(), pos.z);
                        world.spawnEntity(ent);
                        return true;
                    }
                }));
        spellList.put("selfheal",
                new Spell(500, 100, 15, new TriFunction<ServerPlayerEntity, PlayerSpellData, EssencePool, Boolean>() {
                    @Override
                    public Boolean apply(ServerPlayerEntity player, PlayerSpellData spellData, EssencePool altar) {
                        player.heal(2.0f + (float) (2 * spellData.getPowerLevel()));
                        return true;
                    }
                }));
        spellList.put(
                "meteoritestorm",
                new Spell(6500, 600, 45, new TriFunction<ServerPlayerEntity, PlayerSpellData, EssencePool, Boolean>() {
                    @Override
                    public Boolean apply(ServerPlayerEntity player, PlayerSpellData spellData, EssencePool altar) {
                        int meteoriteCount = 10 + (int) Math.round(2.0 * spellData.getPowerLevel());
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

                            World world = player.world;
                            for (int i = 0; i < meteoriteCount; i++) {
                                DelayedTask tsk = new DelayedTask("meteoritestorm_spell_spawn_meteorites",
                                        rand.nextInt(0, maxDelay),
                                        new TriFunction<Object[], Object, Object, Boolean>() {
                                            @Override
                                            public Boolean apply(Object[] data, Object n1, Object n2) {
                                                FireballEntity ent = new FireballEntity(world, player, angle.getX(),
                                                        angle.getY(), angle.getZ(),
                                                        (int) Math.round(
                                                                Math.max(1.0,
                                                                        Math.min(rand.nextInt(minPower, maxPower)
                                                                                + (spellData.getPowerLevel() - 1) * 0.5,
                                                                                5.0))));
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
                }));
        spellList.put(
                "wolfpack",
                new Spell(2500, 500, 25, new TriFunction<ServerPlayerEntity, PlayerSpellData, EssencePool, Boolean>() {
                    @Override
                    public Boolean apply(ServerPlayerEntity player, PlayerSpellData spellData, EssencePool altar) {
                        int wolfCount = 2 + (int) Math.round((spellData.getPowerLevel() - 1));
                        int wolfLifeTime = 20 * 60;// ~one minute of lifetime
                        ArrayList<WolfEntity> wolfesSpawned = new ArrayList<>();
                        for (int i = 0; i < wolfCount; i++) {
                            World world = player.world;
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
                }));
        spellList.put(
                "firebreath",
                new Spell(500, 150, 15, new TriFunction<ServerPlayerEntity, PlayerSpellData, EssencePool, Boolean>() {
                    @Override
                    public Boolean apply(ServerPlayerEntity player, PlayerSpellData spellData, EssencePool altar) {
                        int shotsPerTick = 2;
                        int tickDuration = 30;
                        int breathLife = 20 + (int) Math.round(spellData.getPowerLevel() * 4);
                        int burnDuration = 40 + (int) Math.round(spellData.getPowerLevel() * 10);
                        for (int i = 0; i < tickDuration; i++) {// TODO: making this one single task may make it more
                                                                // memory
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

                                            World world = player.world;
                                            world.playSound(null, player.getBlockPos(),
                                                    SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.BLOCKS, 1f, 1f);
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
                }));
        spellList.put(
                "witherbreath",
                new Spell(750, 150, 25, new TriFunction<ServerPlayerEntity, PlayerSpellData, EssencePool, Boolean>() {
                    @Override
                    public Boolean apply(ServerPlayerEntity player, PlayerSpellData spellData, EssencePool altar) {
                        int shotsPerTick = 2 + (int) Math.floor(spellData.getPowerLevel() / 2);
                        int tickDuration = 30;
                        int breathLife = 20 + (int) Math.round(spellData.getPowerLevel() * 10);
                        int witherDuration = 120 + (int) Math.round(spellData.getPowerLevel() * 80);
                        int damage = 1 + (int) Math.round(spellData.getPowerLevel() * 2);
                        for (int i = 0; i < tickDuration; i++) {// TODO: making this one single task may make it more
                                                                // memory
                                                                // efficient.
                            SkullMagic.taskManager.queueTask(new DelayedTask("spawn_wither_breath_task", i,
                                    new TriFunction<Object[], Object, Object, Boolean>() {
                                        @Override
                                        public Boolean apply(Object[] data, Object n1, Object n2) {
                                            int shotsPerTick = ((int[]) data[0])[0];
                                            int breathLife = ((int[]) data[0])[1];
                                            int witherDuration = ((int[]) data[0])[2];
                                            int damage = ((int[]) data[0])[3];
                                            Random rand = new Random();
                                            Vec3d dir = player.getRotationVector().normalize();

                                            World world = player.world;
                                            world.playSound(null, player.getBlockPos(),
                                                    SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.BLOCKS, 1f, 1f);
                                            for (int i = 0; i < shotsPerTick; i++) {
                                                WitherBreath entity = WitherBreath.createWitherBreath(world, player,
                                                        dir.x + rand.nextFloat() * 0.5,
                                                        dir.y + rand.nextFloat() * 0.5, dir.z + rand.nextFloat() * 0.5,
                                                        witherDuration, breathLife, damage);
                                                entity.setPosition(
                                                        player.getPos().add(dir.multiply(0.5))
                                                                .add(0, player.getEyeHeight(player.getPose()), 0));
                                                world.spawnEntity(entity);
                                            }
                                            return true;
                                        }
                                    },
                                    new Object[] { new int[] { shotsPerTick, breathLife, witherDuration, damage } }));
                        }
                        return true;
                    }
                }));
        spellList.put(
                "slowball",
                new Spell(500, 150, 5, new TriFunction<ServerPlayerEntity, PlayerSpellData, EssencePool, Boolean>() {
                    @Override
                    public Boolean apply(ServerPlayerEntity player, PlayerSpellData spellData, EssencePool altar) {

                        World world = player.world;
                        if (!world.isClient) {
                            Vec3d velocity = player.getRotationVector().multiply(8.0 + spellData.getPowerLevel() * 2);
                            EffectBall ball = EffectBall.createEffectBall(world, player, velocity.x, velocity.y,
                                    velocity.z,
                                    StatusEffects.SLOWNESS, 4.0f,
                                    (int) Math.round(Math.max(1.0, spellData.getPowerLevel() / 3)));
                            ball.setPosition(
                                    player.getCameraEntity().getPos().add(player.getRotationVector().normalize()));
                            world.spawnEntity(ball);
                        }
                        return true;
                    }
                }));
        spellList.put(
                "weakball",
                new Spell(500, 150, 10, new TriFunction<ServerPlayerEntity, PlayerSpellData, EssencePool, Boolean>() {
                    @Override
                    public Boolean apply(ServerPlayerEntity player, PlayerSpellData spellData, EssencePool altar) {

                        World world = player.world;
                        if (!world.isClient) {
                            Vec3d velocity = player.getRotationVector().multiply(8.0 + spellData.getPowerLevel() * 2);
                            EffectBall ball = EffectBall.createEffectBall(world, player, velocity.x, velocity.y,
                                    velocity.z,
                                    StatusEffects.WEAKNESS, 4.0f,
                                    (int) Math.round(Math.max(1.0, spellData.getPowerLevel() / 3)));
                            ball.setPosition(
                                    player.getCameraEntity().getPos().add(player.getRotationVector().normalize()));
                            world.spawnEntity(ball);
                        }
                        return true;
                    }
                }));
        spellList.put(
                "speedbuff",
                new Spell(500, 150, 5, new TriFunction<ServerPlayerEntity, PlayerSpellData, EssencePool, Boolean>() {
                    @Override
                    public Boolean apply(ServerPlayerEntity player, PlayerSpellData spellData, EssencePool altar) {
                        player.addStatusEffect(
                                new StatusEffectInstance(StatusEffects.SPEED,
                                        (int) Math.round(500 * (1 + (spellData.getPowerLevel() - 1) * 0.25)),
                                        (int) Math.round(Math.max(1.0, spellData.getPowerLevel() / 2))));
                        return true;
                    }
                }));
        spellList.put(
                "resistancebuff",
                new Spell(500, 150, 10, new TriFunction<ServerPlayerEntity, PlayerSpellData, EssencePool, Boolean>() {
                    @Override
                    public Boolean apply(ServerPlayerEntity player, PlayerSpellData spellData, EssencePool altar) {
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE,
                                (int) Math.round(500 * (1 + (spellData.getPowerLevel() - 1) * 0.25)),
                                (int) Math.round(Math.max(1.0, spellData.getPowerLevel() / 2))));
                        return true;
                    }
                }));
        spellList.put(
                "strengthbuff",
                new Spell(750, 150, 15, new TriFunction<ServerPlayerEntity, PlayerSpellData, EssencePool, Boolean>() {
                    @Override
                    public Boolean apply(ServerPlayerEntity player, PlayerSpellData spellData, EssencePool altar) {
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH,
                                (int) Math.round(500 * (1 + (spellData.getPowerLevel() - 1) * 0.25)),
                                (int) Math.round(Math.max(1.0, spellData.getPowerLevel() / 2))));
                        return true;
                    }
                }));
        spellList.put(
                "fireresistance",
                new Spell(500, 150, 10, new TriFunction<ServerPlayerEntity, PlayerSpellData, EssencePool, Boolean>() {
                    @Override
                    public Boolean apply(ServerPlayerEntity player, PlayerSpellData spellData, EssencePool altar) {
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE,
                                (int) Math.round(500 * (1 + (spellData.getPowerLevel() - 1) * 0.25)),
                                (int) Math.round(Math.max(1.0, spellData.getPowerLevel() / 2))));
                        return true;
                    }
                }));
        spellList.put(
                "waterbreathing",
                new Spell(500, 150, 10, new TriFunction<ServerPlayerEntity, PlayerSpellData, EssencePool, Boolean>() {
                    @Override
                    public Boolean apply(ServerPlayerEntity player, PlayerSpellData spellData, EssencePool altar) {
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING,
                                (int) Math.round(500 * (1 + (spellData.getPowerLevel() - 1) * 0.25)),
                                (int) Math.round(Math.max(1.0, spellData.getPowerLevel() / 3))));
                        return true;
                    }
                }));
        spellList.put(
                "hastebuff",
                new Spell(1500, 150, 20, new TriFunction<ServerPlayerEntity, PlayerSpellData, EssencePool, Boolean>() {
                    @Override
                    public Boolean apply(ServerPlayerEntity player, PlayerSpellData spellData, EssencePool altar) {
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE,
                                (int) Math.round(500 * (1 + (spellData.getPowerLevel() - 1) * 0.25)),
                                (int) Math.round(Math.max(1.0, spellData.getPowerLevel() / 3))));
                        return true;
                    }
                }));
        spellList.put(
                "teleport",
                new Spell(1000, 800, 30, new TriFunction<ServerPlayerEntity, PlayerSpellData, EssencePool, Boolean>() {
                    @Override
                    public Boolean apply(ServerPlayerEntity player, PlayerSpellData spellData, EssencePool altar) {
                        boolean success = false;
                        HitResult result = player.raycast(100, 1, false);
                        if (result != null) {
                            Vec3d center = result.getPos();

                            World world = player.world;
                            world.playSound(null, new BlockPos(center),
                                    SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1f, 1f);
                            player.teleport(center.x, center.y, center.z, true);
                            success = true;
                        }
                        return success;
                    }
                }));
        spellList.put(
                "poisonball",
                new Spell(500, 150, 10, new TriFunction<ServerPlayerEntity, PlayerSpellData, EssencePool, Boolean>() {
                    @Override
                    public Boolean apply(ServerPlayerEntity player, PlayerSpellData spellData, EssencePool altar) {

                        World world = player.world;
                        if (!world.isClient) {
                            Vec3d velocity = player.getRotationVector().multiply(8.0 + spellData.getPowerLevel() * 2);
                            EffectBall ball = EffectBall.createEffectBall(world, player, velocity.x, velocity.y,
                                    velocity.z,
                                    StatusEffects.POISON, 4.0f,
                                    (int) Math.round(Math.max(1.0, spellData.getPowerLevel() / 3)));
                            ball.setPosition(
                                    player.getCameraEntity().getPos().add(player.getRotationVector().normalize()));
                            world.spawnEntity(ball);
                        }
                        return true;
                    }
                }));
        spellList.put("shockwave",
                new Spell(1000, 100, 15, new TriFunction<ServerPlayerEntity, PlayerSpellData, EssencePool, Boolean>() {
                    @Override
                    public Boolean apply(ServerPlayerEntity player, PlayerSpellData spellData, EssencePool altar) {

                        World world = player.world;
                        if (!world.isClient) {
                            int range = 7 + (int) Math.round(spellData.getPowerLevel());
                            int angle = 30;
                            double power_min = 2.0 + spellData.getPowerLevel();
                            double power_max = 5.0 + spellData.getPowerLevel();
                            Vec3d playerpos = player.getPos().add(0, player.getEyeHeight(player.getPose()), 0);
                            Box box = new Box(playerpos.x - range, playerpos.y - range, playerpos.z - range,
                                    playerpos.x + range, playerpos.y + range, playerpos.z + range);
                            List<Entity> targetCandidates = world.getOtherEntities(player, box);
                            for (Entity ent : targetCandidates) {
                                Vec3d diffabs = ent.getPos().subtract(playerpos);
                                Vec3d diff = diffabs.normalize();
                                double diffangle = Math.acos(diff.dotProduct(player.getRotationVector())) * Math.PI
                                        / 180;
                                if (diffangle < angle && diffangle > -angle) {
                                    double power = power_min + (diffabs.length() / range) * (power_max - power_min);
                                    Vec3d vel = diff.multiply(power);
                                    ent.addVelocity(vel.x, vel.y + 4.0, vel.z);
                                }
                            }
                            world.playSound(null, new BlockPos(playerpos),
                                    SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.BLOCKS, 1f, 1f);
                        }
                        return true;
                    }
                }));
        spellList.put("lightningstrike",
                new Spell(1500, 100, 20, new TriFunction<ServerPlayerEntity, PlayerSpellData, EssencePool, Boolean>() {
                    @Override
                    public Boolean apply(ServerPlayerEntity player, PlayerSpellData spellData, EssencePool altar) {
                        HitResult result = player.raycast(100, 1, false);
                        if (result != null) {
                            Vec3d center = result.getPos();
                            World world = player.world;
                            LightningEntity bolt = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
                            bolt.setPos(center.x, center.y, center.z);

                            world.spawnEntity(bolt);
                        }
                        return true;
                    }
                }));
        spellList.put("excavation",
                new Spell(1500, 100, 20, new TriFunction<ServerPlayerEntity, PlayerSpellData, EssencePool, Boolean>() {
                    @Override
                    public Boolean apply(ServerPlayerEntity player, PlayerSpellData spellData, EssencePool altar) {
                        int radius = (int) (1 + spellData.getPowerLevel() * 2);
                        HitResult result = player.raycast(100, 1, false);
                        Vec3d center = result.getPos();
                        World world = player.world;
                        boolean placedAnything = false;
                        for (int x = (int) Math.round(center.getX() - radius); x < center.getX() + radius; x++) {
                            for (int y = (int) Math.round(center.getY() - radius); y < center.getY()
                                    + radius; y++) {
                                for (int z = (int) Math.round(center.getZ() - radius); z < center.getZ()
                                        + radius; z++) {
                                    BlockPos candidate = new BlockPos(x, y, z);
                                    if (candidate.isWithinDistance(center, radius)) {
                                        BlockState targetBlockState = world.getBlockState(candidate);
                                        if (!targetBlockState.getBlock().equals(Blocks.AIR)) {

                                            ItemStack toolStack = player.getMainHandStack();
                                            Item tool = toolStack.getItem();
                                            // is the position legaol
                                            boolean canBreakBlock = world.canPlayerModifyAt(player, candidate)
                                                    && tool.canMine(targetBlockState, world, candidate, player)
                                                    && targetBlockState.getBlock().getHardness() >= 0;
                                            // is a tool required
                                            if (canBreakBlock && targetBlockState.isToolRequired()) {

                                                // does the player have the tool required?
                                                if (tool instanceof MiningToolItem miningTool
                                                        && miningTool.isSuitableFor(targetBlockState)) {
                                                    // does the tool have enough durability?
                                                    if (!tool.isDamageable() || toolStack.getMaxDamage() > 1
                                                            + toolStack.getDamage()) {
                                                        toolStack.damage(1, new Random(), player);
                                                    } else {
                                                        canBreakBlock = false;
                                                    }
                                                } else {
                                                    canBreakBlock = false;
                                                }
                                            }
                                            if (canBreakBlock) {
                                                world.breakBlock(candidate, true, player);
                                            }

                                        }
                                    }
                                }
                            }
                        }
                        return true;
                    }
                }));
        spellList.put("moundsummon",
                new Spell(1500, 100, 20, new TriFunction<ServerPlayerEntity, PlayerSpellData, EssencePool, Boolean>() {

                    @Override
                    public Boolean apply(ServerPlayerEntity player, PlayerSpellData spellData, EssencePool altar) {
                        HitResult result = player.raycast(100, 1, false);
                        if (result != null) {
                            int radius = (int) (1 + spellData.getPowerLevel() * 2);
                            Vec3d center = result.getPos();
                            World world = player.world;
                            boolean placedAnything = false;
                            for (int x = (int) Math.round(center.getX() - radius); x < center.getX() + radius; x++) {
                                for (int y = (int) Math.round(center.getY() - radius); y < center.getY()
                                        + radius; y++) {
                                    for (int z = (int) Math.round(center.getZ() - radius); z < center.getZ()
                                            + radius; z++) {
                                        BlockPos candidate = new BlockPos(x, y, z);
                                        if (candidate.isWithinDistance(center, radius)) {
                                            if (world.canPlayerModifyAt(player, candidate)
                                                    && world.getBlockState(candidate).isAir()) {
                                                BlockState state = Blocks.DIRT.getDefaultState();// TODO: grass on top,
                                                                                                 // stone at the bottom?
                                                if (world.canPlace(state, candidate, null)) {
                                                    world.setBlockState(candidate, state);
                                                    placedAnything = true;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (placedAnything) {
                                world.playSound(null, new BlockPos(center),
                                        SoundEvents.BLOCK_ROOTED_DIRT_PLACE, SoundCategory.BLOCKS, 1.5f, 1f);
                            }
                        }
                        return true;
                    }
                }));
        spellList.put("lightningstorm",
                new Spell(5000, 450, 40, new TriFunction<ServerPlayerEntity, PlayerSpellData, EssencePool, Boolean>() {
                    @Override
                    public Boolean apply(ServerPlayerEntity player, PlayerSpellData spellData, EssencePool altar) {
                        int lightningCount = (int) Math.round(Math.min(10 + spellData.getPowerLevel() * 3, 50));
                        int radius = 7;
                        int maxDelay = 40;
                        // TODO: way too strong. i love it
                        HitResult result = player.raycast(100, 1, false);
                        if (result != null) {
                            Vec3d center = result.getPos();
                            Random rand = new Random();
                            for (int i = 0; i < lightningCount; i++) {
                                DelayedTask tsk = new DelayedTask("meteoritestorm_spell_spawn_meteorites",
                                        rand.nextInt(0, maxDelay),
                                        new TriFunction<Object[], Object, Object, Boolean>() {
                                            @Override
                                            public Boolean apply(Object[] data, Object n1, Object n2) {

                                                World world = player.world;
                                                LightningEntity bolt = new LightningEntity(EntityType.LIGHTNING_BOLT,
                                                        world);
                                                bolt.setPos(center.x - radius + 2 * rand.nextFloat() * radius,
                                                        center.y, center.z - radius + 2 * rand.nextFloat() * radius);
                                                bolt.setVelocity(0, -15, 0);
                                                world.spawnEntity(bolt);
                                                return true;
                                            }
                                        }, null);
                                SkullMagic.taskManager.queueTask(tsk);
                            }
                        }
                        return true;
                    }
                }));
        /*
         * ,
         * "invisibility",
         * new Spell(500, 150, new TriFunction<ServerPlayerEntity, World, EssencePool,
         * Boolean>() {
         * 
         * @Override
         * public Boolean apply(ServerPlayerEntity player, World world, EssencePool
         * altar) {
         * 
         * return false;
         * }
         * })
         */
        return spellList;
    }

}
