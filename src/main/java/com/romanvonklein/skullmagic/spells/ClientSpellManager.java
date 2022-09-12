package com.romanvonklein.skullmagic.spells;

import java.util.ArrayList;
import java.util.HashMap;

public class ClientSpellManager {
    public HashMap<String, Integer> spellList = new HashMap<>();
    public ArrayList<String> spellnames = new ArrayList<>();
    public String selectedSpellName = "fireball";

    public void loadSpells(String message) {
        this.spellnames.clear();
        this.spellList.clear();
        for (String valuePair : message.split(";")) {
            String[] parts = valuePair.split(":");
            if (parts.length > 1) {
                this.spellList.put(parts[0], Integer.parseInt(parts[1]));
                spellnames.add(parts[0]);
            }
        }
    }

    public void cycleSpell() {
        if (spellList.size() > 0) {

            int currentIndex = spellnames.indexOf(selectedSpellName);
            currentIndex++;
            currentIndex = adjustForListsize(currentIndex);
            selectedSpellName = spellnames.get(currentIndex);
        }
    }

    public String getPrevSpellname() {
        String result = null;
        if (spellnames.size() > 0) {
            int currentIndex = adjustForListsize(spellnames.indexOf(selectedSpellName) - 1);
            result = spellnames.get(currentIndex);
        }
        return result;
    }

    public String getNextSpellname() {
        String result = null;
        if (spellnames.size() > 0) {
            int currentIndex = adjustForListsize(spellnames.indexOf(selectedSpellName) + 1);
            result = spellnames.get(currentIndex);
        }
        return result;
    }

    private int adjustForListsize(int index) {
        return index >= spellnames.size() ? 0 : index < 0 ? spellnames.size() - 1 : index;
    }
}
