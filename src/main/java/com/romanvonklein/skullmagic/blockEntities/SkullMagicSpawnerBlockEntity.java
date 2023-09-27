package com.romanvonklein.skullmagic.blockEntities;

import org.jetbrains.annotations.Nullable;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.blocks.SkullMagicSpawner;
import com.romanvonklein.skullmagic.networking.ServerPackageSender;
import com.romanvonklein.skullmagic.util.SpawnerEntry;
import com.romanvonklein.skullmagic.util.Util;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class SkullMagicSpawnerBlockEntity extends BlockEntity {
    protected String spawnCommand;
    protected int range;
    protected int maxSpawns;
    protected int maxDelay;
    protected int delayed;
    protected int lastParticled;

    public SkullMagicSpawnerBlockEntity(BlockPos pos,
            BlockState state) {
        super(SkullMagic.SKULLMAGIC_SPAWNER_BLOCK_ENTITY, pos, state);
    }

    public void setSpawnSettings(SpawnerEntry spawn) {
        if (spawn != null) {
            this.spawnCommand = spawn.command;
            this.maxDelay = spawn.maxDelay;
            this.maxSpawns = spawn.maxSpawns;
            this.range = spawn.range;
        }

    }

    public static void tick(World world, BlockPos pos, BlockState state, BlockEntity ent) {
        if (!world.isClient && ent instanceof SkullMagicSpawnerBlockEntity) {
            SkullMagicSpawnerBlockEntity castedEnt = (SkullMagicSpawnerBlockEntity) ent;
            if (castedEnt.spawnCommand == null) {
                castedEnt.setSpawnSettings(
                        Util.getSpawnForSpawnerWithClass(((SkullMagicSpawner) state.getBlock()).getSpawnClass()));
            }
            castedEnt.delayed++;
            if (castedEnt.delayed >= castedEnt.maxDelay) {
                castedEnt.delayed = 0;
                castedEnt.lastParticled = 0;
                Random rand = Random.createLocal();
                for (int i = 0; i < castedEnt.maxSpawns; i++) {
                    int posX = pos.getX() + rand.nextBetween(-castedEnt.range, +castedEnt.range);
                    int posY = pos.getY() + rand.nextBetween(-castedEnt.range, +castedEnt.range);
                    int posZ = pos.getZ() + rand.nextBetween(-castedEnt.range, +castedEnt.range);
                    ServerCommandSource src = world.getServer().getCommandSource();
                    String command = "FORMATTING FAILED";
                    if (world.isSpaceEmpty(new Box(posX, posY, posZ, 1, 2, 1))) {

                        try {
                            command = String.format(castedEnt.spawnCommand,
                                    world.getRegistryKey().getValue().toString(),
                                    posX, posY,
                                    posZ);
                            world.getServer().getCommandManager().executeWithPrefix(src.withSilent(), command);
                        } catch (Exception e) {
                            SkullMagic.LOGGER.warn("Failed formatting or executing command: %s, formatted command: %s",
                                    castedEnt.spawnCommand, command);
                        }
                    }
                }
                world.playSound(null, pos,
                        SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.BLOCKS, 0.5f, 1f);
            } else {
                int remainingTicks = castedEnt.maxDelay - castedEnt.delayed;
                int siceLastTick = castedEnt.delayed - castedEnt.lastParticled;
                if (siceLastTick >= remainingTicks / 10) {
                    Random rand = Random.createLocal();
                    castedEnt.lastParticled = castedEnt.delayed;
                    ServerPackageSender.sendParticleEffectPackageToPlayers(((ServerWorld) world).getPlayers(),
                            "minecraft:flame",
                            world.getRegistryKey(), pos.toCenterPos().add((rand.nextDouble() - 0.5) * 0.25,
                                    (rand.nextDouble() - 0.5) * 0.2, (rand.nextDouble() - 0.5) * 0.25));
                }
            }
        }
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        if (this.spawnCommand != null) {
            tag.putString("spawnCommand", this.spawnCommand);
            tag.putInt("range", this.range);
            tag.putInt("maxSpawns", this.maxSpawns);
            tag.putInt("maxDelay", this.maxDelay);
            tag.putInt("delayed", this.delayed);
        }
        super.writeNbt(tag);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        if (tag.contains("spawnCommand")) {
            try {
                this.range = tag.getInt("range");
                this.maxSpawns = tag.getInt("maxSpawns");
                this.maxDelay = tag.getInt("maxDelay");
                this.delayed = tag.getInt("delayed");
                this.spawnCommand = tag.getString("spawnCommand");
            } catch (Exception e) {
                SkullMagic.LOGGER.error("Failed loading spawnser data from NBT data.");
            }

        }
        super.readNbt(tag);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
}
