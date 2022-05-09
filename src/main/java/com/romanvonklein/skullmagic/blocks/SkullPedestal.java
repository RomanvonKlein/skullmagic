package com.romanvonklein.skullmagic.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class SkullPedestal extends Block {
    public SkullPedestal(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        // TODO: read outline shape from model file?
        VoxelShape shape = VoxelShapes.cuboid(0.125f, 0f, 0.125f, 0.875f, 1.0f, 0.875f);
        return shape;
    }
    /*
     * @Override
     * public void neighborUpdate(BlockState state, World world, BlockPos pos, Block
     * block, BlockPos fromPos,
     * boolean notify) {
     * SkullMagic.LOGGER.info("neighborupdate triggered!");
     * SkullMagic.LOGGER.info("my pos: " + pos);
     * SkullMagic.LOGGER.info("neighbor: " + fromPos);
     * SkullMagic.LOGGER.info("neighborid: " +
     * Registry.BLOCK.getId(block).toString());
     * 
     * if (fromPos.getX() == pos.getX() && fromPos.getY() == pos.getY() + 1 &&
     * fromPos.getZ() == pos.getZ()
     * &&
     * Config.getConfig().skulls.containsKey(Registry.BLOCK.getId(block).toString())
     * ) {
     * SkullMagic.LOGGER.info("valid skull placed on pedestal!");
     * }
     * super.neighborUpdate(state, world, pos, block, fromPos, notify);
     * }
     */
    // @Override
    // public void onBlockAdded(state, world, pos, oldState, notify){

    // }
}