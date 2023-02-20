package com.romanvonklein.skullmagic.spells;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public class ClientSpellManager {

    // mostly for visualizations
    private HashMap<BlockPos, ArrayList<BlockPos>> SkullShrinesToAltars;
    private HashMap<BlockPos, ArrayList<BlockPos>> SpellShrinesToAltars;

    public HashMap<String, PlayerSpellData> spellList = new HashMap<>();
    public ArrayList<String> spellnames = new ArrayList<>();
    public String selectedSpellName = null;

    public void loadSpells(String message) {
        this.spellnames.clear();
        this.spellList.clear();
        for (String valuePair : message.split(";")) {
            String[] parts = valuePair.split(":");
            if (parts.length > 1) {
                String[] valueParts = parts[1].split(",");
                this.spellList.put(parts[0], new PlayerSpellData(Integer.parseInt(valueParts[0]),
                        Double.parseDouble(valueParts[1]), Double.parseDouble(valueParts[2]),
                        Double.parseDouble(valueParts[3])));
                spellnames.add(parts[0]);
            }
        }
        if (this.selectedSpellName == null && this.spellnames.size() > 0) {
            this.selectedSpellName = this.spellnames.get(0);
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

    // Creates and moves particles to visualize links
    public void tickParticles(MinecraftClient client) {
       
    }

    private static BlockPos parseBlockPos(String posString) {
        String[] intstrings = posString.split(",");
        int x = Integer.parseInt(intstrings[0]);
        int y = Integer.parseInt(intstrings[1]);
        int z = Integer.parseInt(intstrings[2]);
        return new BlockPos(x, y, z);
    }

    // 212,121,122:123,123,123.111,111,111|212,121,122:123,123,123.111,111,111;1231,123123,12311:123,1231,1231.12311,1231,121|
    public void updateLinks(String msgString) {
        SkullShrinesToAltars = new HashMap<>();
        SpellShrinesToAltars = new HashMap<>();
        String[] parts = msgString.split(";");
        String skullAltarLinks = parts[0];
        if (!skullAltarLinks.equals("")) {
            if (skullAltarLinks.contains("|")) {
                String[] skullAltars = skullAltarLinks.split("|");
                for (String singleSkullAltarLinks : skullAltars) {
                    String[] singleSkullAltarParts = singleSkullAltarLinks.split(":");
                    String skullAltarPosStr = singleSkullAltarParts[0];
                    BlockPos altarPos = parseBlockPos(skullAltarPosStr);
                    SkullShrinesToAltars.put(altarPos, new ArrayList<BlockPos>());
                    String skullPedestalPositions = singleSkullAltarParts[1];
                    for (String pedestalPosString : skullPedestalPositions.split(".")) {
                        SkullShrinesToAltars.get(altarPos).add(parseBlockPos(pedestalPosString));
                    }
                }
            }
        }
        String spellAltarLinks = parts[1];
        if (!spellAltarLinks.equals("")) {
            if (spellAltarLinks.contains("|")) {
                String[] spellAltars = spellAltarLinks.split("|");
                for (String singlespellAltarLinks : spellAltars) {
                    String[] singleSpellAltarParts = singlespellAltarLinks.split(":");
                    String spellAltarPosStr = singleSpellAltarParts[0];
                    BlockPos altarPos = parseBlockPos(spellAltarPosStr);
                    SpellShrinesToAltars.put(altarPos, new ArrayList<BlockPos>());
                    String spellPedestalPositions = singleSpellAltarParts[1];
                    for (String pedestalPosString : spellPedestalPositions.split(".")) {
                        SpellShrinesToAltars.get(altarPos).add(parseBlockPos(pedestalPosString));
                    }
                }
            }
        }
    }
}
