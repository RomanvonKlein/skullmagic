package com.romanvonklein.skullmagic.blockEntities;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.data.WorldBlockPos;

import net.minecraft.block.BlockState;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;

public class FireCannonBlockEntity extends AConsumerBlockEntity {

    private int lastTickRedstonePower = 0;
    private static int essenceCost = 500;

    public FireCannonBlockEntity(BlockPos pos, BlockState state) {
        super(SkullMagic.FIRE_CANNON_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, FireCannonBlockEntity be) {
        if (!world.isClient) {
            int power = world.getReceivedRedstonePower(pos);
            if (power > 0 && be.lastTickRedstonePower == 0) {
                WorldBlockPos worldPos = new WorldBlockPos(pos, world.getRegistryKey());
                if (SkullMagic.getServerData().canConsumerApply(worldPos, power)) {
                    Direction target = Direction.UP;
                    if (state.contains(Properties.FACING)) {
                        target = state.get(Properties.FACING);
                    }
                    Vec3f angle = target.getUnitVector();
                    ServerPlayerEntity player = SkullMagic.getServerData().getPlayerForConsumerWorldPos((ServerWorld)world,
                            worldPos);
                    FireballEntity ent = new FireballEntity(world, player, angle.getX(), angle.getY(), angle.getZ(),
                            1);
                    ent.setPos(pos.getX() + angle.getX(), pos.getY() + angle.getY(), pos.getZ() + angle.getZ());
                    ent.setVelocity(new Vec3d(angle.getX(), angle.getY(), angle.getZ()));
                    world.spawnEntity(ent);
                    SkullMagic.getServerData().applyConsumer(worldPos, essenceCost);
                    world.playSound(null, new BlockPos(pos),
                            SoundEvents.ENTITY_GHAST_SHOOT, SoundCategory.BLOCKS, 1f, 1f);
                } else {
                    SkullMagic.LOGGER.info("Insufficient essence in pool!");
                }
            } else {
                world.playSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_BEACON_DEACTIVATE,
                        SoundCategory.BLOCKS, 1.0f, 1.0f, true);
            }

            be.lastTickRedstonePower = power;
        }
    }

}
