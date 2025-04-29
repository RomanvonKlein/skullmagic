package com.romanvonklein.skullmagic.spells;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

import org.apache.commons.lang3.function.TriFunction;
import org.joml.Vector3f;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.effects.Effects;
import com.romanvonklein.skullmagic.entities.EffectBall;
import com.romanvonklein.skullmagic.entities.FireBreath;
import com.romanvonklein.skullmagic.entities.WitherBreath;
import com.romanvonklein.skullmagic.mixin.ZombieVillagerEntityMixin;
//import com.romanvonklein.skullmagic.structurefeatures.SkullMagicStructurePoolBasedGenerator;
import com.romanvonklein.skullmagic.tasks.DelayedTask;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity.ZombieData;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.world.World;

public class SpellInitializer {

    public static Map<String, ? extends Spell> initSpells() {

        Map<String, Spell> spellList = new HashMap<>();
        spellList.put("fireball", new FireBallSpell());
        spellList.put("selfheal", new SelfHealSpell());
        spellList.put("meteoritestorm", new MeteoriteStormSpell());
        spellList.put("wolfpack", new WolfPackSpell());
        spellList.put("firebreath", new FireBreathSpell());
        spellList.put("witherbreath", new WitherBreathSpell());
        spellList.put("slowball", new SlowBallSpell());
        spellList.put("lunge", new LungeSpell());
        spellList.put("weakball", new WeakBallSpell());
        spellList.put("speedbuff", new SpeedBuffSpell());
        spellList.put("resistancebuff", new ResistanceBuffSpell());
        spellList.put("strengthbuff", new StrengthBuffSpell());
        spellList.put("fireresistance", new FireResistanceSpell());
        spellList.put("waterbreathing", new WaterBreathingSpell());
        spellList.put("hastebuff", new HasteBuffSpell());
        spellList.put("teleport", new TeleportSpell());
        spellList.put("infect", new InfectSpell());
        spellList.put("cure", new CureSpell());
        spellList.put("poisonball", new PoisonBallSpell());
        spellList.put("shockwave", new ShockwaveSpell());
        spellList.put("dungeonrise", new DungeonRiseSpell());// this one should not be used yet
        spellList.put("lightningstrike", new LightningStrikeSpell());
        spellList.put("excavation", new ExcavationSpell());
        spellList.put("moundsummon", new MoundSummonSpell());
        spellList.put("lightningstorm", new LightningStormSpell());
        return spellList;
    }

}
