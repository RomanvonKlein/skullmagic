package com.romanvonklein.skullmagic.spells;

import org.apache.commons.lang3.function.TriFunction;

import com.romanvonklein.skullmagic.blockEntities.SkullAltarBlockEntity;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class Spell {

    public int essenceCost;
    // TODO: implement cooldown
    public int cooldownTicks;

    public TriFunction<ServerPlayerEntity, World, SkullAltarBlockEntity, Boolean> action;

    public Spell(int essenceCost, int cooldownTicks,
            TriFunction<ServerPlayerEntity, World, SkullAltarBlockEntity, Boolean> action) {
        this.essenceCost = essenceCost;
        this.cooldownTicks = cooldownTicks;
        this.action = action;
    }

    // TODO: replace mana management within the block-entity so it
    // can work even when not loaded!
    public boolean tryCast(ServerPlayerEntity player, World world, SkullAltarBlockEntity altar) {
        boolean success = false;
        if (this.essenceCost <= altar.getEssence()) {
            altar.discharge(this.essenceCost);
            success = this.action.apply(player, world, altar);// this.action(player, world, altar);
        }
        return success;
    }

}
