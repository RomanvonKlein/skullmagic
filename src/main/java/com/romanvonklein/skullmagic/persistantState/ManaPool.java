package com.romanvonklein.skullmagic.persistantState;

public class ManaPool {
    int essence;
    int essenceChargeRate;
    int maxEssence;

    // TODO: integrate the functionality from skullAltarBlockEntity into this
    public ManaPool(int essence, int essenceChargeRate, int maxEssence) {
        this.essence = essence;
        this.essenceChargeRate = essenceChargeRate;
        this.maxEssence = maxEssence;
    }
}
