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

    public SpellPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(SkullMagic.SPELL_PEDESTAL_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        if (this.scroll != null) {
            NbtCompound scrollCompound = new NbtCompound();
            this.scroll.writeNbt(scrollCompound);
            nbt.put("scroll", scrollCompound);
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        this.scroll = null;
        if (nbt.contains("scroll", NbtType.COMPOUND)) {
            this.scroll = ItemStack.fromNbt(nbt.getCompound("scroll"));
        }
    }

    public String getSpellName() {
        String result = "";
        if (this.scroll != null && this.scroll.getItem() instanceof KnowledgeOrb) {
            result = ((KnowledgeOrb) this.scroll.getItem()).spellName;
        }
        return result;
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
        world.updateListeners(pos, this.getCachedState(), world.getBlockState(pos), Block.NOTIFY_LISTENERS);
    }

    public ItemStack getScroll() {
        return this.scroll;
    }

    public static void tick(World world, BlockPos pos, BlockState state, SpellPedestalBlockEntity be) {

    }
}
