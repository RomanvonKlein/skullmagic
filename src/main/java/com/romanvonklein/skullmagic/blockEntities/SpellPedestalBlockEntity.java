package com.romanvonklein.skullmagic.blockEntities;

import org.jetbrains.annotations.Nullable;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.items.KnowledgeOrb;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SpellPedestalBlockEntity extends BlockEntity {

    private ItemStack scroll;
    private boolean scrollWasNullLastTick = true;

    public SpellPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(SkullMagic.SPELL_PEDESTAL_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (!world.isClient) {

            if (this.scroll != null) {
                NbtCompound scrollCompound = new NbtCompound();
                this.scroll.writeNbt(scrollCompound);
                nbt.put("scroll", scrollCompound);
            }
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        if (nbt.contains("scroll", NbtType.COMPOUND)) {
            this.scroll = ItemStack.fromNbt(nbt.getCompound("scroll"));
        } else {
            this.scroll = null;
        }
    }

    public String getSpellName() {
        String result = "";
        if (this.scroll != null && this.scroll.getItem() instanceof KnowledgeOrb) {
            result = ((KnowledgeOrb) this.scroll.getItem()).spellName;
        }
        return result;
    }

    public void setScroll(ItemStack stack) {
        if (stack != null && !(stack.getItem() instanceof KnowledgeOrb)) {
            SkullMagic.LOGGER.error("Something went wrong - cannot set scroll for pedestal to non-knowledgeorb item!");
        }
        this.scroll = stack;
        if (!world.isClient) {
            world.updateListeners(pos, this.getCachedState(), world.getBlockState(pos), Block.NOTIFY_LISTENERS);
        }
        this.markDirty();
    }

    public ItemStack getScroll() {
        return this.scroll;
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

    public static void tick(World world, BlockPos pos, BlockState state, SpellPedestalBlockEntity be) {

    }
}
