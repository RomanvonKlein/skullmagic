package com.romanvonklein.skullmagic.items;

import java.util.ArrayList;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.spells.SpellManager;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class KnowledgeOrb extends Item {

    public String spellName;

    public KnowledgeOrb(Settings settings, String spellName) {
        super(settings.maxCount(1));
        this.spellName = spellName;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        TypedActionResult<ItemStack> result = super.use(world, user, hand);
        // should the itemstack be checked first??
        if (!world.isClient) {
            if (SkullMagic.spellManager.learnSpell((ServerPlayerEntity) user, this.spellName)) {
                result = new TypedActionResult<ItemStack>(ActionResult.SUCCESS, ItemStack.EMPTY);

                world.playSound(null, new BlockPos(user.getBlockX(), user.getBlockY(), user.getBlockZ()),
                        SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 1f, 1f);
            }
        }
        return result;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(new TranslatableText("tooltip.skullmagic.level_cost")
                .append(Integer.toString(SpellManager.getLevelCost(this.spellName))));
        super.appendTooltip(stack, world, tooltip, context);
    }

    public static ArrayList<KnowledgeOrb> generateKnowledgeOrbs() {
        ArrayList<KnowledgeOrb> orbs = new ArrayList<>();
        for (String spellName : SpellManager.SpellDict.keySet()) {
            orbs.add(new KnowledgeOrb(new FabricItemSettings().group(ItemGroup.MISC), spellName));
        }
        return orbs;
    }
}
