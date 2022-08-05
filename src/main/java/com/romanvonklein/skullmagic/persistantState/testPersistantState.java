package com.romanvonklein.skullmagic.persistantState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import java.util.Map.Entry;

import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

public class testPersistantState extends PersistentState {

    private final HashMap<UUID, BlockPos> linkedAltars = new HashMap<>();
    private boolean dirty = false;

    public testPersistantState() {
        super();
    }

    public NbtCompound writeNbt(NbtCompound tag) {
        System.out.println("Writing nbt for testPersistentState");
        NbtCompound altarLinks = new NbtCompound();
        linkedAltars.keySet().forEach(uuid -> {
            System.out.println(
                    "Saving link for '" + uuid + "' and '" + parseBlockposToString(this.linkedAltars.get(uuid)) + "'");
            altarLinks.putString(uuid.toString(), parseBlockposToString(linkedAltars.get(uuid)));
        });
        tag.put("altarLinks", altarLinks);
        return tag;
    }

    /**
     * Read the state of all linked altars to players from the world nbt
     * 
     * @param tag
     * @return
     */
    public static PersistentState fromNbt(NbtCompound tag) {
        System.out.println("Reading nbt for testPersistentState");
        testPersistantState tps = new testPersistantState();
        tps.linkedAltars.clear();
        if (tag.contains("altarLinks")) {
            try {
                NbtCompound altarLinks = tag.getCompound("altarLinks");
                SkullMagic.LOGGER.info("Successfully loaded altarLinks from NBT data: ");
                altarLinks.getKeys().forEach(uuid -> {
                    System.out.println("Loaded link for '" + uuid + "' and '" + altarLinks.getString(uuid) + "'");
                    tps.linkedAltars.put(UUID.fromString(uuid),
                            new BlockPos(parseBlockPosFromNBTString(altarLinks.getString(uuid))));
                });
            } catch (Exception e) {
                SkullMagic.LOGGER.error("Failed loading persistentstate from NBT data!");
            }
        }
        return tps;
    }

    private static BlockPos parseBlockPosFromNBTString(String nbtString) {
        int x, y, z;
        // TODO: try - catch in case of corrupt save data?
        String[] coordStrings = nbtString.split(";");
        x = Integer.parseInt(coordStrings[0]);
        y = Integer.parseInt(coordStrings[1]);
        z = Integer.parseInt(coordStrings[2]);
        return new BlockPos(x, y, z);
    }

    private static String parseBlockposToString(BlockPos blockPos) {
        return String.format("%d;%d;%d", blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    @Override
    public boolean isDirty() {
        return this.dirty;
    }

    public void addLink(UUID playerID, BlockPos pos) {
        this.linkedAltars.put(playerID, pos);
        this.dirty = true;
    }

    public void removeLink(UUID playerID, BlockPos pos) {
        this.linkedAltars.remove(playerID, pos);
    }

    public boolean playerHasLink(UUID uuid) {
        return this.linkedAltars.containsKey(uuid) && this.linkedAltars.get(uuid) != null;
    }

    public String getAltarPosLinkedToPlayer(UUID uuid) {
        return parseBlockposToString(this.linkedAltars.get(uuid));
    }

    public BlockPos getLinkedAltarBlockPos(UUID uuid) {
        return this.linkedAltars.get(uuid);
    }

    public void removeAltar(BlockPos pos) {
        UUID toRemove = null;
        for (Entry<UUID, BlockPos> entry : this.linkedAltars.entrySet()) {
            if (pos.equals(entry.getValue())) {
                toRemove = entry.getKey();
            }
        }
        if (toRemove != null) {
            this.linkedAltars.remove(toRemove);
        } else {
            SkullMagic.LOGGER.warn(
                    "Could not remove the altar from linking nbt, as it was not found (pos: " + pos.toString() + ")");
        }
        this.dirty = true;
    }
}
