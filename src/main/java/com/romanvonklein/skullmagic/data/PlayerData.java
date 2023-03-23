package com.romanvonklein.skullmagic.data;

import java.util.HashMap;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;

class PlayerData extends PersistentState {
    protected HashMap<String, SpellData> spells;
    protected EssencePool essencePool;
    protected String selectedSpell;

    public PlayerData() {
        this.spells = new HashMap<>();
        this.essencePool = new EssencePool();
        this.selectedSpell = "";
    }

    PlayerData(HashMap<String, SpellData> spells, EssencePool essencePool, String selectedSpell) {
        this.spells = spells;
        this.essencePool = essencePool;
        this.selectedSpell = selectedSpell;
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        // spells
        NbtCompound spellsCompound = new NbtCompound();
        for (String spellName : spells.keySet()) {
            NbtCompound spellDataCompound = new NbtCompound();
            spells.get(spellName).writeNbt(spellDataCompound);
            spellsCompound.put(spellName, spellDataCompound);
        }
        tag.put("spells", spellsCompound);

        // essencePool
        NbtCompound essencePoolCompound = new NbtCompound();
        tag.put("essencePool", essencePool.writeNbt(essencePoolCompound));

        // selectedSpell
        tag.putString("selectedSpell", selectedSpell);

        return tag;
    }

    public static PlayerData fromNbt(NbtCompound tag) {
        // spells
        HashMap<String, SpellData> spells = new HashMap<>();
        for (String spellname : tag.getCompound("spells").getKeys()) {
            spells.put(spellname, SpellData.fromNbt(tag.getCompound("spells").getCompound(spellname)));
        }

        // essencePool
        EssencePool essencePool = EssencePool.fromNbt(tag.getCompound("essencePool"));

        // selectedSpell
        String selectedSpell = tag.getString("selectedSpell");
        return new PlayerData(spells, essencePool, selectedSpell);
    }

    EssencePool getEssencePool() {
        return this.essencePool;
    }

}
