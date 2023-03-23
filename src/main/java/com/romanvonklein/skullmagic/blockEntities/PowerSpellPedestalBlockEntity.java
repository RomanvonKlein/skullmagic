package com.romanvonklein.skullmagic.blockEntities;


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
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PowerSpellPedestalBlockEntity extends BlockEntity {

    private ItemStack scroll;
    public static String type = "power";

    public PowerSpellPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(SkullMagic.POWER_SPELL_PEDESTAL_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        NbtCompound scrollCompound = new NbtCompound();
        if (this.scroll != null) {
            this.scroll.writeNbt(scrollCompound);
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
        world.updateListeners(this.getPos(), this.getCachedState(), world.getBlockState(this.getPos()),
                Block.NOTIFY_LISTENERS);
    }

    public ItemStack getScroll() {
        return this.scroll;
    }

    public static void tick(World world, BlockPos pos, BlockState state, PowerSpellPedestalBlockEntity be) {

    }
}
