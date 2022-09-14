package com.romanvonklein.skullmagic.blockEntities;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.items.KnowledgeOrb;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtTypes;
import net.minecraft.util.math.BlockPos;

public class SpellPedestalBlockEntity extends BlockEntity {

    public ItemStack scroll;

    public SpellPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(SkullMagic.SPELL_PEDESTAL_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (this.scroll != null) {
            NbtCompound scrollCompound = new NbtCompound();
            this.scroll.writeNbt(scrollCompound);
            nbt.put("scroll", scrollCompound);
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
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
}
