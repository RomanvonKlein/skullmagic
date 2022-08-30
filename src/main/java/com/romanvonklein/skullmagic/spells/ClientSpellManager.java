package com.romanvonklein.skullmagic.spells;

import java.util.HashMap;

public class ClientSpellManager {
    public HashMap<String, Integer> spellList = new HashMap<>();
    public String selectedSpellName = "fireball";

    public void cycleSpell() {
        String[] allSpells = (String[]) spellList.keySet().toArray();
        int currentIndex = 0;
        for (int i = 0; i < allSpells.length; i++) {
            if (allSpells[i].equals(selectedSpellName)) {
                currentIndex = i;
                break;
            }
        }
        currentIndex++;
        if (currentIndex >= allSpells.length) {
            currentIndex = 0;
        }
        selectedSpellName = allSpells[currentIndex];
    }
}
