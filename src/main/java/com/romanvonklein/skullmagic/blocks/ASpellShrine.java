package com.romanvonklein.skullmagic.blocks;

import java.util.Optional;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.blockEntities.SpellShrineBlockEntity;
import com.romanvonklein.skullmagic.data.WorldBlockPos;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
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
        if (!world.isClient) {
            SkullMagic.getServerData().removeSpellShrine(new WorldBlockPos(pos, world.getRegistryKey()));
        }
        super.onBreak(world, pos, state, player);
    }

    @Override
    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
        if (!world.isClient) {
            SkullMagic.getServerData().removeSpellShrine(new WorldBlockPos(pos, world.getRegistryKey()));
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
                SkullMagic.getServerData().updateSpellShrine((ServerWorld)world, player, blockEnt, this);
            }
        }
        return result;

    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
            BlockEntityType<T> type) {
        return checkType(type, SkullMagic.SPELL_SHRINE_BLOCK_ENTITY,
                (world1, pos, state1, be) -> SpellShrineBlockEntity.tick(world1, pos, state1, be));
    }
}
