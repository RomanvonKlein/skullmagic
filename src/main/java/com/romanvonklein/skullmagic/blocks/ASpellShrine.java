package com.romanvonklein.skullmagic.blocks;

import java.util.Optional;
import java.util.UUID;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.blockEntities.SpellShrineBlockEntity;
import com.romanvonklein.skullmagic.items.KnowledgeOrb;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
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
            SkullMagic.spellManager.removeSpellShrine((ServerWorld) world, pos);
        }
    }

    @Override
    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
        if (!world.isClient) {
            SkullMagic.spellManager.removeSpellShrine((ServerWorld) world, pos);
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
                UUID playerid = player.getGameProfile().getId();

                SpellShrineBlockEntity blockEnt = opt.get();
                if (blockEnt.getScroll() == null) {
                    // if empty, check wether the player holds a valid scroll.
                    ItemStack itemStack = player.getMainHandStack();
                    if (itemStack.getItem() instanceof KnowledgeOrb) {
                        String spellname = ((KnowledgeOrb) itemStack.getItem()).spellName;
                        // dont do anything if the player already has a shrine for that spell assigned
                        // to him
                        if (SkullMagic.spellManager.playerToSpellShrine.containsKey(playerid)
                                && SkullMagic.spellManager.playerToSpellShrine.get(playerid).containsKey(spellname)) {
                            player.sendMessage(
                                    Text.of("You already have a shrine for the spell " + spellname
                                            + " assigned to you at "
                                            + SkullMagic.spellManager.playerToSpellShrine.get(playerid).get(spellname)
                                                    .toShortString()),
                                    true);
                        } else {
                            SkullMagic.spellManager.addNewSpellShrine((ServerWorld) world, pos,
                                    player.getGameProfile().getId(), spellname);
                            blockEnt.setScroll(itemStack.copy());
                            itemStack.decrement(1);
                        }
                    }
                } else {
                    // if not empty, drop the contained item.
                    player.giveItemStack(blockEnt.getScroll());
                    blockEnt.setScroll(null);
                    SkullMagic.spellManager.removeSpellShrine((ServerWorld) world, pos, (ServerPlayerEntity) player);
                }

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
