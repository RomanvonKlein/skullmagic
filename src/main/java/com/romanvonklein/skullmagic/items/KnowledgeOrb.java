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

                world.playSound(
                        null, // Player - if non-null, will play sound for every nearby player *except* the
                        // specified player
                        new BlockPos(user.getBlockX(), user.getBlockY(), user.getBlockZ()), // The position of where the
                        // sound will come from
                        SoundEvents.ENTITY_PLAYER_LEVELUP, // The sound that will play, in this case, the sound the
                                                           // anvil
                        // plays when it lands.
                        SoundCategory.BLOCKS, // This determines which of the volume sliders affect this sound
                        1f, // Volume multiplier, 1 is normal, 0.5 is half volume, etc
                        1f // Pitch multiplier, 1 is normal, 0.5 is half pitch, etc
                );

                // world.playSound(user.getBlockX(), user.getBlockY(), user.getBlockZ(),
                // SoundEvents.ENTITY_PLAYER_LEVELUP,
                // SoundCategory.PLAYERS, 1.0f, 1.0f, true);
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
