package com.romanvonklein.skullmagic.spells;

import java.util.ArrayList;
import java.util.HashMap;

public class ClientSpellManager {
    public HashMap<String, Integer> spellList = new HashMap<>();
    public String selectedSpellName = "fireball";

    public void cycleSpell() {
        if (spellList.size() > 0) {
            ArrayList<String> allSpells = new ArrayList<>();
            spellList.keySet().forEach((spellName) -> {
                allSpells.add(spellName);
            });
            int currentIndex = 0;
            for (int i = 0; i < allSpells.size(); i++) {
                if (allSpells.get(i).equals(selectedSpellName)) {
                    currentIndex = i;
                    break;
                }
            }
            currentIndex++;
            if (currentIndex >= allSpells.size()) {
                currentIndex = 0;
            }
            selectedSpellName = allSpells.get(currentIndex);
        }

    }
}
