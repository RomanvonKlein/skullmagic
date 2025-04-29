package com.romanvonklein.skullmagic.spells;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LightningStrikeSpell extends Spell {

    protected LightningStrikeSpell() {
        super(10000, 100, 20, true);
    }

    @Override
    public boolean cast(ServerPlayerEntity player, Double powerLevel) {
      HitResult result = player.raycast(100, 1, false);
                        if (result != null) {
                            Vec3d center = result.getPos();
                            World world = player.getWorld();
                            LightningEntity bolt = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
                            bolt.setPos(center.x, center.y, center.z);

                            world.spawnEntity(bolt);
                        }
                        return true;
    }

}
