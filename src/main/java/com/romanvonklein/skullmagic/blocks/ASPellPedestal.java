package com.romanvonklein.skullmagic.blocks;

import java.util.Optional;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.blockEntities.PowerSpellPedestalBlockEntity;
import com.romanvonklein.skullmagic.items.KnowledgeOrb;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

public abstract class ASPellPedestal extends BlockWithEntity {
    public String type = "power";

    public ASPellPedestal(Settings settings, String type) {
        super(settings);
        this.type = type;
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
        return new PowerSpellPedestalBlockEntity(pos, state);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        if (!world.isClient) {
            SkullMagic.spellManager.removeSpellPedestal(world.getRegistryKey(), pos, this.type);
        }
    }

    @Override
    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
        if (!world.isClient) {
            SkullMagic.spellManager.removeSpellPedestal(world.getRegistryKey(), pos, this.type);
        }
        super.onDestroyedByExplosion(world, pos, explosion);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
            BlockHitResult hit) {
        ActionResult result = ActionResult.SUCCESS;
        if (!world.isClient) {
            Optional<PowerSpellPedestalBlockEntity> opt = world.getBlockEntity(pos,
                    SkullMagic.POWER_SPELL_PEDESTAL_BLOCK_ENTITY);
            if (opt.isPresent()) {
                PowerSpellPedestalBlockEntity ent = opt.get();

                // check if the socket is empty
                if (ent.getScroll() == null) {
                    // if empty, check wether the player holds a valid scroll.
                    ItemStack itemStack = player.getMainHandStack();
                    if (itemStack.getItem() instanceof KnowledgeOrb) {
                        String spellname = ((KnowledgeOrb) itemStack.getItem()).spellName;
                        if (SkullMagic.spellManager.tryAddSpellPedestal(world.getRegistryKey(), pos,
                                player.getGameProfile().getId(), spellname, this)) {
                            ent.setScroll(itemStack.copy());
                            itemStack.decrement(1);
                            world.playSound((double) pos.getX(), (double) pos.getY(), (double) pos.getZ(),
                                    SoundEvents.BLOCK_END_PORTAL_FRAME_FILL,
                                    SoundCategory.BLOCKS, 1.0f, 1.0f, true);
                        } else {
                            player.sendMessage(
                                    Text.of("Could not find a spell shrine linked to you and " + spellname
                                            + " in range."),
                                    true);
                            result = ActionResult.FAIL;
                        }
                    } else {
                        player.sendMessage(Text.of("Not a valid scroll!"), true);
                    }
                } else {
                    // if not empty, drop the contained item.
                    ItemEntity itemEnt = new ItemEntity(world, player.getPos().x, player.getPos().y, player.getPos().z,
                            ent.getScroll());
                    itemEnt.setPickupDelay(0);
                    world.spawnEntity(itemEnt);
                    // player.giveItemStack(ent.getScroll());
                    ent.setScroll(null);
                    SkullMagic.spellManager.removeSpellPedestal(world.getRegistryKey(), pos, this.type);
                }
            }
        }
        return result;

    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
            BlockEntityType<T> type) {
        return checkType(type, SkullMagic.POWER_SPELL_PEDESTAL_BLOCK_ENTITY,
                (world1, pos, state1, be) -> PowerSpellPedestalBlockEntity.tick(world1, pos, state1, be));
    }
}