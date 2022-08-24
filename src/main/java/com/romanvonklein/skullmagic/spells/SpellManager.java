package com.romanvonklein.skullmagic.spells;

import java.util.Map;

import org.apache.commons.lang3.function.TriFunction;

import com.romanvonklein.skullmagic.blockEntities.SkullAltarBlockEntity;

import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SpellManager {

    static Map<String, ? extends Spell> SpellDict = Map.of("fireball",
            new Spell(100, 100, new TriFunction<ServerPlayerEntity, World, SkullAltarBlockEntity, Boolean>() {
                @Override
                public Boolean apply(ServerPlayerEntity player, World world, SkullAltarBlockEntity altar) {
                    // TODO: future upgrades for speed and explosion power?
                    Vec3d angle = player.getRotationVector();
                    Vec3d pos = player.getPos();
                    FireballEntity ent = new FireballEntity(world, player, angle.getX(), angle.getY(), angle.getZ(), 1);
                    ent.setPos(pos.x, pos.y + player.getHeight(), pos.z);
                    world.spawnEntity(ent);
                    return true;
                }
            }));

    // TODO: replace the alter with a essencemanager!
    public static boolean castSpell(String spellName, SkullAltarBlockEntity altar, ServerPlayerEntity player,
            World world) {
        boolean success = false;
        if (SpellDict.containsKey(spellName)) {
            Spell spell = SpellDict.get(spellName);
            if (altar.getEssence() >= spell.essenceCost) {
                success = spell.action.apply(player, world, altar);
                if (success) {
                    altar.discharge(spell.essenceCost);
                }
            }
        }
        return success;
    }
}
