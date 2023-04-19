package com.romanvonklein.skullmagic.items;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.romanvonklein.skullmagic.ClientInitializer;
import com.romanvonklein.skullmagic.SkullMagic;
import com.romanvonklein.skullmagic.data.ServerData;
import com.romanvonklein.skullmagic.util.CreativeTabLists;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
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
            if (SkullMagic.getServerData().learnSpell((ServerPlayerEntity) user, this.spellName, false)) {
                result = new TypedActionResult<ItemStack>(ActionResult.SUCCESS, ItemStack.EMPTY);

                world.playSound(null, new BlockPos(user.getBlockX(), user.getBlockY(), user.getBlockZ()),
                        SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 1f, 1f);
            }
        }
        return result;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (world != null && world.isClient) {
            if (ClientInitializer.getClientData().knowsSpell(this.spellName)) {

                tooltip.add(Text.translatable("tooltip.skullmagic.spell_learned","Learned Spell",null).formatted(Formatting.GREEN));
                // current spell levels
                double powerlevel = ClientInitializer.getClientData().getPowerLevel(this.spellName);
                double efficiencylevel = ClientInitializer.getClientData().getEfficiencyLevel(this.spellName);
                double cooldownLevel = ClientInitializer.getClientData().getCooldownLevel(this.spellName);

                tooltip.add(Text.translatable("tooltip.skullmagic.spelldata_powerlevel")
                        .append(Text.of(Double.toString(powerlevel))).formatted(Formatting.GRAY));
                tooltip.add(Text.translatable("tooltip.skullmagic.spelldata_efficiencylevel")
                        .append(Text.of(Double.toString(efficiencylevel))).formatted(Formatting.GRAY));
                tooltip.add(Text.translatable("tooltip.skullmagic.spelldata_cooldownreductionlevel")
                        .append(Text.of(Double.toString(cooldownLevel))).formatted(Formatting.GRAY));
            } else {
                if (ServerData.getLevelCost(this.spellName) > MinecraftClient.getInstance().player.experienceLevel) {

                    tooltip.add(Text.translatable("tooltip.skullmagic.level_cost")
                            .append(Integer.toString(ServerData.getLevelCost(this.spellName)))
                            .formatted(Formatting.RED));
                } else {
                    tooltip.add(Text.translatable("tooltip.skullmagic.level_cost")
                            .append(Integer.toString(ServerData.getLevelCost(this.spellName)))
                            .formatted(Formatting.GREEN));

                }
            }
        }

        super.appendTooltip(stack, world, tooltip, context);
    }

    public static ArrayList<KnowledgeOrb> generateKnowledgeOrbs() {
        ArrayList<KnowledgeOrb> orbs = new ArrayList<>();
        for (String spellName : ServerData.getSpellNames()) {
            KnowledgeOrb orb =new KnowledgeOrb(new FabricItemSettings(), spellName);
            CreativeTabLists.addItemToTabs(orb,CreativeTabLists.miscTabList);
            orbs.add(orb);
        }
        return orbs;
    }
}
