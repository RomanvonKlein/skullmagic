package com.romanvonklein.skullmagic.blocks;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.blockEntities.SpellShrineBlockEntity;
import com.romanvonklein.skullmagic.items.KnowledgeOrb;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

public class IntermediateSpellShrine extends BlockWithEntity {
    public static final int scanRange = 5;
    public static final int scanHeight = 1;
    private ItemStack containedScroll = null;

    public IntermediateSpellShrine(Settings settings) {
        super(settings);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        // With inheriting from BlockWithEntity this defaults to INVISIBLE, so we need
        // to change that!
        return BlockRenderType.MODEL;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        // TODO: read outline shape from model file?
        VoxelShape shape = VoxelShapes.cuboid(0.125f, 0f, 0.125f, 0.875f, 1.0f, 0.875f);

        return shape;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SpellShrineBlockEntity(pos, state, 2);
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
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
            BlockHitResult hit) {
        ActionResult result = ActionResult.SUCCESS;
        if (!world.isClient) {

            // check if the socket is empty
            if (this.containedScroll == null) {
                // if empty, check wether the player holds a valid scroll.
                ItemStack itemStack = player.getMainHandStack();
                if (itemStack.getItem() instanceof KnowledgeOrb) {
                    String spellname = ((KnowledgeOrb) itemStack.getItem()).spellName;
                    SkullMagic.spellManager.addNewSpellShrine(world, pos,
                            player.getGameProfile().getId(), spellname);
                    this.containedScroll = itemStack.copy();
                    itemStack.decrement(1);
                }
            } else {
                // if not empty, drop the contained item.
                player.giveItemStack(this.containedScroll);
                this.containedScroll = null;
                SkullMagic.spellManager.removeSpellShrine(world.getRegistryKey(), pos);
            }
        }
        return result;

    }
}