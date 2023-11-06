package com.romanvonklein.skullmagic.blockEntities;

import org.jetbrains.annotations.Nullable;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.blocks.SkullMagicSpawner;
import com.romanvonklein.skullmagic.networking.ServerPackageSender;
import com.romanvonklein.skullmagic.util.SpawnerEntry;
import com.romanvonklein.skullmagic.util.Util;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class SkullMagicSpawnerBlockEntity extends BlockEntity {
    protected String spawnCommand;
    protected int range;
    protected int maxSpawns;
    protected int maxDelay;
    protected int delayed;
    protected int lastParticled;
    private static final int maxTries = 3;

    public SkullMagicSpawnerBlockEntity(BlockPos pos,
            BlockState state) {
        super(SkullMagic.SKULLMAGIC_SPAWNER_BLOCK_ENTITY, pos, state);
    }

    public void setSpawnSettings(SpawnerEntry spawn) {
        if (spawn != null) {
            this.delayed = 0;
            this.spawnCommand = spawn.command;
            this.maxDelay = spawn.maxDelay;
            this.maxSpawns = spawn.maxSpawns;
            this.range = spawn.range;
        }

    }

    // TODO: dont spawn when there's loads of mobs around already
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
                SkullMagic.LOGGER.info("Attempting Spawn");
                Random rand = Random.createLocal();
                for (int i = 0; i < castedEnt.maxSpawns; i++) {
                    ServerCommandSource src = world.getServer().getCommandSource();
                    String command = "FORMATTING FAILED";
                    for (int tryNo = 0; tryNo < maxTries; tryNo++) {
                        int posX = pos.getX() + rand.nextBetween(-castedEnt.range, +castedEnt.range);
                        int posY = pos.getY() + rand.nextBetween(-castedEnt.range, +castedEnt.range);
                        int posZ = pos.getZ() + rand.nextBetween(-castedEnt.range, +castedEnt.range);
                        if (world.isSpaceEmpty(new Box(posX, posY, posZ, posX + 1, posY + 2, posZ + 1))) {
                            try {
                                SkullMagic.LOGGER.info("preparing spawning command from spawning command: {}",
                                        castedEnt.spawnCommand);
                                command = String.format(castedEnt.spawnCommand,
                                        world.getRegistryKey().getValue().toString(),
                                        posX, posY,
                                        posZ);
                                String.format("%d %s", 5, "test");
                                SkullMagic.LOGGER.info("Using spawning command: {}", command);
                                world.getServer().getCommandManager().executeWithPrefix(src.withSilent(), command);
                            } catch (Exception e) {
                                SkullMagic.LOGGER.warn(
                                        "Failed formatting or executing command: {}, formatted command: {}",
                                        castedEnt.spawnCommand, command);
                                SkullMagic.LOGGER.error(e.getMessage());
                            }
                            break;
                        } else {
                            SkullMagic.LOGGER.info("Could not find any free space at {} {} {}", posX, posY,
                                    posZ);
                            ServerPackageSender.sendParticleEffectPackageToPlayers(((ServerWorld) world).getPlayers(),
                                    "minecraft:flame",
                                    world.getRegistryKey(), new Vec3d(0.5 + posX, 0.5 + posY, 0.5 + posZ));
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
            this.range = tag.getInt("range");
            this.maxSpawns = tag.getInt("maxSpawns");
            this.maxDelay = tag.getInt("maxDelay");
            this.delayed = tag.getInt("delayed");
            this.spawnCommand = tag.getString("spawnCommand");
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
