package com.romanvonklein.skullmagic.blocks;

import java.util.Optional;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.blockEntities.ASpellPedestalBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.CooldownSpellPedestalBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.EfficiencySpellPedestalBlockEntity;
import com.romanvonklein.skullmagic.blockEntities.PowerSpellPedestalBlockEntity;
import com.romanvonklein.skullmagic.networking.ServerPackageSender;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

public abstract class ASpellPedestal extends BlockWithEntity {
    public String type = "none";
    public int level;

    public ASpellPedestal(Settings settings, String type, int level) {
        super(settings);
        this.type = type;
        this.level = level;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
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
        if (this.type.equals("power")) {
            return new PowerSpellPedestalBlockEntity(pos, state);
        } else if (this.type.equals("efficiency")) {
            return new EfficiencySpellPedestalBlockEntity(pos, state);
        } else {
            return new CooldownSpellPedestalBlockEntity(pos, state);
        }
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient) {
            SkullMagic.getServerData().tryRemoveSpellPedestal((ServerWorld) world, pos, this.type);
        }
        dropScroll(world, pos);
        super.onBreak(world, pos, state, player);
    }

    private void dropScroll(World world, BlockPos pos) {
        BlockEntity ent = world.getBlockEntity(pos);
        if (ent instanceof ASpellPedestalBlockEntity) {
            ((ASpellPedestalBlockEntity) ent).dropScroll();
        }
    }

    @Override
    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
        if (!world.isClient) {
            SkullMagic.getServerData().tryRemoveSpellPedestal((ServerWorld) world, pos, this.type);
        }
        dropScroll(world, pos);
        super.onDestroyedByExplosion(world, pos, explosion);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
            BlockHitResult hit) {
        ActionResult result = ActionResult.SUCCESS;
        // TODO: terrible code. absolutely horrendous. fix later
        if (!world.isClient) {
            SkullMagic.getServerData();
            // all of these should propably use an interface to save all this redundancy...
            Optional<PowerSpellPedestalBlockEntity> power_opt = world.getBlockEntity(pos,
                    SkullMagic.POWER_SPELL_PEDESTAL_BLOCK_ENTITY);
            Optional<CooldownSpellPedestalBlockEntity> cooldown_opt = world.getBlockEntity(pos,
                    SkullMagic.COOLDOWN_SPELL_PEDESTAL_BLOCK_ENTITY);
            Optional<EfficiencySpellPedestalBlockEntity> efficiency_opt = world.getBlockEntity(pos,
                    SkullMagic.EFFICIENCY_SPELL_PEDESTAL_BLOCK_ENTITY);
            boolean updateNeeded = true;
            if (power_opt.isPresent()) {
                PowerSpellPedestalBlockEntity ent = power_opt.get();
                SkullMagic.getServerData().updateSpellPedestal(this, ent, this.type, player, world, pos);
            } else if (efficiency_opt.isPresent()) {
                EfficiencySpellPedestalBlockEntity ent = efficiency_opt.get();
                SkullMagic.getServerData().updateSpellPedestal(this, ent, this.type, player, world, pos);
            } else if (cooldown_opt.isPresent()) {
                CooldownSpellPedestalBlockEntity ent = cooldown_opt.get();
                SkullMagic.getServerData().updateSpellPedestal(this, ent, this.type, player, world, pos);
            } else {
                updateNeeded = false;
            }
            if (updateNeeded) {
                ServerPackageSender.sendUpdatePlayerDataPackageForPlayer((ServerPlayerEntity) player);
            }
        }
        return result;

    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
            BlockEntityType<T> type) {
        if (this.type.equals("power")) {
            return validateTicker(type, SkullMagic.POWER_SPELL_PEDESTAL_BLOCK_ENTITY,
                    (world1, pos, state1, be) -> {
                    });
            // (world1, pos, state1, be) -> PowerSpellPedestalBlockEntity.tick(world1, pos,
            // state1, be));

        } else if (this.type.equals("efficiency")) {
            return validateTicker(type, SkullMagic.EFFICIENCY_SPELL_PEDESTAL_BLOCK_ENTITY,
                    (world1, pos, state1, be) -> {
                    });
            // (world1, pos, state1, be) -> EfficiencySpellPedestalBlockEntity.tick(world1,
            // pos, state1, be));

        } else {
            return validateTicker(type, SkullMagic.COOLDOWN_SPELL_PEDESTAL_BLOCK_ENTITY,
                    (world1, pos, state1, be) -> {
                    });
            // (world1, pos, state1, be) -> CooldownSpellPedestalBlockEntity.tick(world1,
            // pos, state1, be));
        }
    }
}