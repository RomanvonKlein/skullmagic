package com.romanvonklein.skullmagic.spells;

import java.util.ArrayList;
import java.util.HashMap;

import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3f;

public class ClientSpellManager {

    // mostly for visualizations
    private HashMap<BlockPos, ArrayList<BlockPos>> SkullShrinesToAltars;
    private HashMap<BlockPos, ArrayList<BlockPos>> SpellShrinesToAltars;

    public HashMap<String, PlayerSpellData> spellList = new HashMap<>();
    public ArrayList<String> spellnames = new ArrayList<>();
    public String selectedSpellName = null;

    private int sinceLastTick = 0;

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
        sinceLastTick++;
        if (sinceLastTick > 10) {
            sinceLastTick = 0;
            int spellParticles = 0;
            int skullparticles = 0;
            // spawn a particle for each pedestal, flying towards the linked altar
            // SkullAltars
            if (this.SkullShrinesToAltars != null && client.world != null) {

                for (BlockPos altarPos : this.SkullShrinesToAltars.keySet()) {
                    for (BlockPos pedPos : this.SkullShrinesToAltars.get(altarPos)) {
                        Vec3f velocity = new Vec3f(altarPos.getX() - pedPos.getX(), altarPos.getY() - pedPos.getY(),
                                altarPos.getZ() - pedPos.getZ());
                        velocity.scale(0.1f);
                        client.world.addParticle(SkullMagic.LINK_PARTICLE, true, pedPos.getX() + 0.5,
                                pedPos.getY() + 0.5,
                                pedPos.getZ() + 0.5, velocity.getX(),
                                velocity.getY(), velocity.getZ());
                        skullparticles++;
                    }
                }
            }
            // SpellAltars
            if (this.SpellShrinesToAltars != null) {
                for (BlockPos altarPos : this.SpellShrinesToAltars.keySet()) {
                    for (BlockPos pedPos : this.SpellShrinesToAltars.get(altarPos)) {
                        Vec3f velocity = new Vec3f(pedPos.getX() - altarPos.getX(), pedPos.getY() - altarPos.getY(),
                                pedPos.getZ() - altarPos.getZ());
                        velocity.scale(0.1f);
                        client.world.addParticle(SkullMagic.LINK_PARTICLE, true, pedPos.getX() + 0.5,
                                pedPos.getY() + 0.5,
                                pedPos.getZ() + 0.5, velocity.getX(),
                                velocity.getY(), velocity.getZ());
                        spellParticles++;
                    }
                }
            }
            SkullMagic.LOGGER
                    .info("Spawned particles: spellshrines: " + spellParticles + " skullshrines: " + skullparticles);
        }

    }

    private static BlockPos parseBlockPos(String posString) {
        String[] intstrings = posString.split(",");
        int x = Integer.parseInt(intstrings[0]);
        int y = Integer.parseInt(intstrings[1]);
        int z = Integer.parseInt(intstrings[2]);
        return new BlockPos(x, y, z);
    }

    public void updateLinks(String msgString) {
        SkullShrinesToAltars = new HashMap<>();
        SpellShrinesToAltars = new HashMap<>();
        String[] parts = msgString.split(";");
        String skullAltarLinks = parts[0];
        if (!skullAltarLinks.equals("")) {
            String[] skullAltars;
            if (skullAltarLinks.contains("|")) {

                skullAltars = skullAltarLinks.split("|");
            } else {
                skullAltars = new String[1];
                skullAltars[0] = skullAltarLinks;
            }
            for (String singleSkullAltarLinks : skullAltars) {
                String[] singleSkullAltarParts = singleSkullAltarLinks.split(":");
                String skullAltarPosStr = singleSkullAltarParts[0];
                BlockPos altarPos = parseBlockPos(skullAltarPosStr);
                SkullShrinesToAltars.put(altarPos, new ArrayList<BlockPos>());
                String skullPedestalPositions = singleSkullAltarParts[1];
                String[] skullPedestalStrings = skullPedestalPositions.split("\\.");
                for (String pedestalPosString : skullPedestalStrings) {
                    SkullShrinesToAltars.get(altarPos).add(parseBlockPos(pedestalPosString));
                }
            }

        }
        if (parts.length > 1) {
            String spellAltarLinks = parts[1];
            if (!spellAltarLinks.equals("")) {
                String[] spellAltars;
                if (spellAltarLinks.contains("|")) {

                    spellAltars = spellAltarLinks.split("|");
                } else {
                    spellAltars = new String[1];
                    spellAltars[0] = spellAltarLinks;
                }
                for (String singlespellAltarLinks : spellAltars) {
                    String[] singleSpellAltarParts = singlespellAltarLinks.split(":");
                    String spellAltarPosStr = singleSpellAltarParts[0];
                    BlockPos altarPos = parseBlockPos(spellAltarPosStr);
                    SpellShrinesToAltars.put(altarPos, new ArrayList<BlockPos>());
                    if (singleSpellAltarParts.length > 1) {
                        String spellPedestalPositions = singleSpellAltarParts[1];
                        for (String pedestalPosString : spellPedestalPositions.split("\\.")) {
                            SpellShrinesToAltars.get(altarPos).add(parseBlockPos(pedestalPosString));
                        }
                    }
                }

            }
        }
    }
}
