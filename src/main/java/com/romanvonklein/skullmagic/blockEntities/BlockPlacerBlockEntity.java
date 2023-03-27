package com.romanvonklein.skullmagic.blockEntities;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.data.WorldBlockPos;
import com.romanvonklein.skullmagic.inventory.IImplementedInventory;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.AutomaticItemPlacementContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class BlockPlacerBlockEntity extends AConsumerBlockEntity
        implements NamedScreenHandlerFactory, IImplementedInventory, InventoryChangedListener {
    private int lastTickRedstonePower = 0;
    private static int essenceCost = 750;
    // private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(3,
    // ItemStack.EMPTY);
    private static final int INVENTORY_SIZE = 9;
    SimpleInventory inventory = new SimpleInventory(INVENTORY_SIZE);

    public BlockPlacerBlockEntity(BlockPos pos, BlockState state) {
        super(SkullMagic.BLOCK_PLACER_BLOCK_ENTITY, pos, state);
        this.inventory.addListener(this);
    }

    public static void tick(World world, BlockPos pos, BlockState state, BlockPlacerBlockEntity be) {
        if (!world.isClient) {
            int power = world.getReceivedRedstonePower(pos);
            if (power > 0 && be.lastTickRedstonePower == 0) {
                WorldBlockPos worldPos = new WorldBlockPos(pos, world.getRegistryKey());
                if (SkullMagic.getServerData().canConsumerApply(worldPos, power)) {
                    Direction target = Direction.UP;
                    if (state.contains(Properties.FACING)) {
                        target = state.get(Properties.FACING);
                    }
                    BlockPos targetPos = pos.add(target.getVector());
                    for (ItemStack stack : be.getItems()) {
                        if (world.getBlockState(targetPos).equals(Blocks.AIR.getDefaultState())) {
                            if (stack.getItem().getClass().isAssignableFrom(BlockItem.class)) {
                                if (((BlockItem) stack.getItem()).place(
                                        new AutomaticItemPlacementContext(world, targetPos, target, stack, target))
                                        .isAccepted()) {
                                    SkullMagic.getServerData().applyConsumer(worldPos, essenceCost);
                                    break;
                                }
                            }
                        }

                    }
                } else {
                    world.playSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_BEACON_DEACTIVATE,
                            SoundCategory.BLOCKS, 1.0f, 1.0f, true);
                }
            }
            be.lastTickRedstonePower = power;
        }
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        Inventories.writeNbt(tag, this.getItems());
        super.writeNbt(tag);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        DefaultedList<ItemStack> items = DefaultedList.ofSize(INVENTORY_SIZE, new ItemStack(Items.AIR));
        Inventories.readNbt(tag, items);
        for (ItemStack stack : items) {
            this.inventory.addStack(stack);
        }
        super.readNbt(tag);
    }

    @Override
    public int size() {
        return INVENTORY_SIZE;
    }

    // @Override
    // protected ScreenHandler createScreenHandler(int syncId, PlayerInventory
    // playerInventory) {
    // return SkullMagic.BLOCK_PLACER_SCREEN_HANDLER.create(syncId,
    // playerInventory);
    // }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity var3) {
        return new Generic3x3ContainerScreenHandler(syncId, playerInventory, inventory);
    }

    @Override
    public Text getDisplayName() {
        // versions 1.18.2 and below
        return new TranslatableText("skullmagic.blockplacer.guiname");
        // versions since 1.19
        // return Text.translatable("container.chest");
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        ItemStack[] stacks = new ItemStack[INVENTORY_SIZE];
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            stacks[i] = this.inventory.getStack(i);
        }
        DefaultedList<ItemStack> list = DefaultedList.copyOf(this.inventory.getStack(0), stacks);
        return list;
    }

    @Override
    public void onInventoryChanged(Inventory var1) {
        world.updateListeners(pos, null, null, Block.NOTIFY_LISTENERS);
    }

    public void dropInventory() {
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            world.spawnEntity(
                    new ItemEntity(world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.getStack(i)));
            this.setStack(i, Items.AIR.getDefaultStack());
        }
    }

}
