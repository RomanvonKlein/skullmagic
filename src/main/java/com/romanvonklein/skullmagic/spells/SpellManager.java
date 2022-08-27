package com.romanvonklein.skullmagic.spells;

import java.util.Map;

import org.apache.commons.lang3.function.TriFunction;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.persistantState.EssencePool;

import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SpellManager {

    static Map<String, ? extends Spell> SpellDict = Map.of("fireball",
            new Spell(100, 100, new TriFunction<ServerPlayerEntity, World, EssencePool, Boolean>() {
                @Override
                public Boolean apply(ServerPlayerEntity player, World world, EssencePool altar) {
                    // TODO: future upgrades for speed and explosion power?
                    Vec3d angle = player.getRotationVector();
                    Vec3d pos = player.getPos();
                    FireballEntity ent = new FireballEntity(world, player, angle.getX(), angle.getY(), angle.getZ(), 1);
                    ent.setPos(pos.x, pos.y + player.getHeight(), pos.z);
                    world.spawnEntity(ent);
                    return true;
                }
            }));

    public static boolean castSpell(String spellName, ServerPlayerEntity player,
            World world) {
        boolean success = false;
        if (SpellDict.containsKey(spellName)) {
            EssencePool pool = SkullMagic.essenceManager.getEssencePoolForPlayer(player.getUuid());
            Spell spell = SpellDict.get(spellName);
            if (pool.getEssence() >= spell.essenceCost) {
                success = spell.action.apply(player, world, pool);
                if (success) {
                    pool.discharge(spell.essenceCost);
                }
            }
        }
        return success;
    }
}
