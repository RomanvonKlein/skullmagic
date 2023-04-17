package com.romanvonklein.skullmagic.blockEntities;

import org.jetbrains.annotations.Nullable;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.blocks.AdvancedSpellShrine;
import com.romanvonklein.skullmagic.blocks.IntermediateSpellShrine;
import com.romanvonklein.skullmagic.blocks.SimpleSpellShrine;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SpellShrineBlockEntity extends BlockEntity {
    public int level;
    private ItemStack scroll;

    public SpellShrineBlockEntity(BlockPos a, BlockState b, int level) {
        super(SkullMagic.SPELL_SHRINE_BLOCK_ENTITY, a, b);
        this.level = level;
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        nbt.putInt("level", level);
        if (this.scroll != null) {
            NbtCompound scrollCompound = new NbtCompound();
            this.scroll.writeNbt(scrollCompound);
            nbt.put("scroll", scrollCompound);
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        this.level = nbt.getInt("level");
        if (nbt.contains("scroll", NbtType.COMPOUND)) {
            this.scroll = ItemStack.fromNbt(nbt.getCompound("scroll"));
        } else {
            this.scroll = null;
        }
    }

    public SpellShrineBlockEntity(BlockPos a, BlockState b) {
        super(SkullMagic.SPELL_SHRINE_BLOCK_ENTITY, a, b);
        if (this.world != null) {

            this.level = 0;
            Block block = this.world.getBlockState(this.pos).getBlock();
            if (block instanceof SimpleSpellShrine) {
                this.level = 1;

            } else if (block instanceof IntermediateSpellShrine) {
                this.level = 2;

            } else if (block instanceof AdvancedSpellShrine) {
                this.level = 3;

            } else {
                SkullMagic.LOGGER.error("block for spellshrineblockentity does not seem to be valid spell shrine.");
            }
        }
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

    public void setScroll(ItemStack newScroll) {
        this.scroll = newScroll;
        this.markDirty();
        if (newScroll == null) {
            world.playSound(null, this.getPos(),
                    SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1f, 1f);
        } else {
            world.playSound(null, this.getPos(),
                    SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS, 1f, 1f);
        }
        world.updateListeners(pos, this.getCachedState(), world.getBlockState(pos), Block.NOTIFY_LISTENERS);
    }

    public ItemStack getScroll() {
        return this.scroll;
    }

    public static void tick(World world, BlockPos pos, BlockState state, SpellShrineBlockEntity be) {
    }
}
