package com.romanvonklein.skullmagic.blockEntities;

import org.jetbrains.annotations.Nullable;

import com.romanvonklein.skullmagic.items.KnowledgeOrb;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;

public abstract class ASpellPedestalBlockEntity extends BlockEntity {
    protected String type = "none";
    protected ItemStack scroll = null;

    public ASpellPedestalBlockEntity(BlockEntityType<? extends ASpellPedestalBlockEntity> type, BlockPos pos,
            BlockState state) {
        super(type, pos, state);
    }

    public ItemStack getScroll() {
        return this.scroll;
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        NbtCompound scrollCompound = new NbtCompound();
        if (this.getScroll() != null) {
            this.getScroll().writeNbt(scrollCompound);
        }
        nbt.put("scroll", scrollCompound);
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
        world.updateListeners(this.getPos(), this.getCachedState(), world.getBlockState(this.getPos()),
                Block.NOTIFY_LISTENERS);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

}
