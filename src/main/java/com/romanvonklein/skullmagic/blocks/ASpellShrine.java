package com.romanvonklein.skullmagic.blocks;

import java.util.Optional;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.blockEntities.SpellShrineBlockEntity;
import com.romanvonklein.skullmagic.items.KnowledgeOrb;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

public abstract class ASpellShrine extends BlockWithEntity {

    protected ASpellShrine(Settings settings) {
        super(settings);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        if (!world.isClient) {
            SkullMagic.spellManager.removeSpellShrine(world.getRegistryKey(), pos);
        }
    }

    @Override
    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
        if (!world.isClient) {
            SkullMagic.spellManager.removeSpellShrine(world.getRegistryKey(), pos);
        }
        super.onDestroyedByExplosion(world, pos, explosion);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        // With inheriting from BlockWithEntity this defaults to INVISIBLE, so we need
        // to change that!
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
            BlockHitResult hit) {
        ActionResult result = ActionResult.SUCCESS;
        if (!world.isClient) {
            Optional<SpellShrineBlockEntity> opt = world.getBlockEntity(pos, SkullMagic.SPELL_SHRINE_BLOCK_ENTITY);
            // check if the socket is empty
            if (opt.isPresent()) {
                SpellShrineBlockEntity blockEnt = opt.get();
                if (blockEnt.scroll == null) {
                    // if empty, check wether the player holds a valid scroll.
                    ItemStack itemStack = player.getMainHandStack();
                    if (itemStack.getItem() instanceof KnowledgeOrb) {
                        String spellname = ((KnowledgeOrb) itemStack.getItem()).spellName;
                        SkullMagic.spellManager.addNewSpellShrine(world, pos,
                                player.getGameProfile().getId(), spellname);
                        blockEnt.scroll = itemStack.copy();
                        itemStack.decrement(1);
                    }
                } else {
                    // if not empty, drop the contained item.
                    player.giveItemStack(blockEnt.scroll);
                    blockEnt.scroll = null;
                    SkullMagic.spellManager.removeSpellShrine(world.getRegistryKey(), pos);
                }
            }
        }
        return result;

    }
}
