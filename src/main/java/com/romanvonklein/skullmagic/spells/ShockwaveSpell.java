package com.romanvonklein.skullmagic.spells;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ShockwaveSpell extends Spell{

    protected ShockwaveSpell() {
        super(10000, 100, 15, false);
    }

    @Override
    public boolean cast(ServerPlayerEntity player, Double powerLevel) {
        World world = player.getWorld();
                        if (!world.isClient) {
                            int range = 7 + (int) Math.round(powerLevel);
                            int angle = 30;
                            double powerMin = 2.0 + powerLevel;
                            double powerMax = 5.0 + powerLevel;
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
                                    double power = powerMin + (diffabs.length() / range) * (powerMax - powerMin);
                                    Vec3d vel = diff.multiply(power);
                                    ent.addVelocity(vel.x, vel.y + 4.0, vel.z);
                                }
                            }
                            world.playSound(null, BlockPos.ofFloored(playerpos),
                                    SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.BLOCKS, 1f, 1f);
                        }
                        return true;
    }
    
}
