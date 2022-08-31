package com.romanvonklein.skullmagic.blockEntities;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.essence.EssencePool;

import net.minecraft.block.BlockState;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class BlockPlacerBlockEntity extends LootableContainerBlockEntity implements InventoryProvider {
    private int lastTickRedstonePower = 0;
    private int essenceCost = 75;
    private DefaultedList<ItemStack> inventory;
    private static final int INVENTORY_SIZE = 9;

    public BlockPlacerBlockEntity(BlockPos pos, BlockState state) {
        super(SkullMagic.BLOCK_PLACER_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, BlockPlacerBlockEntity be) {
        if (!world.isClient) {
            // TODO: move this to abstract base class, similar to spells?
            int power = world.getReceivedRedstonePower(pos);
            if (power > 0 && be.lastTickRedstonePower == 0) {
                EssencePool pool = SkullMagic.essenceManager.getEssencePoolForConsumer(world.getRegistryKey(), pos);
                if (pool != null && pool.linkedPlayerID != null) {

                } else {
                    world.playSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_BEACON_DEACTIVATE,
                            SoundCategory.BLOCKS, 1.0f, 1.0f, true);
                }
            }
            be.lastTickRedstonePower = power;
        }
    }

    @Override
    protected Text getContainerName() {
        // versions 1.18.2 and below
        return new TranslatableText("container.chest");
        // versions since 1.19
        // return Text.translatable("container.chest");
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return null;// new BlockPlacerScreenHandler(syncId, playerInventory);
    }

    @Override
    protected DefaultedList<ItemStack> getInvStackList() {
        return this.inventory;
    }

    @Override
    protected void setInvStackList(DefaultedList<ItemStack> list) {
        this.inventory = list;
    }

    @Override
    public int size() {
        return INVENTORY_SIZE;
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if (!this.deserializeLootTable(tag)) {
            Inventories.readNbt(tag, this.inventory);
        }
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        if (!this.serializeLootTable(tag)) {
            Inventories.writeNbt(tag, this.inventory);
        }
    }

    @Override
    public SidedInventory getInventory(BlockState var1, WorldAccess var2, BlockPos var3) {
        // TODO Auto-generated method stub
        return null;
    }
}
