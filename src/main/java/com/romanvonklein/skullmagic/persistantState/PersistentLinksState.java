package com.romanvonklein.skullmagic.persistantState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.Map.Entry;

import com.romanvonklein.skullmagic.SkullMagic;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

public class PersistentLinksState extends PersistentState {

    private final HashMap<UUID, BlockPos> linkedAltars = new HashMap<>();
    private final HashMap<BlockPos, BlockPos> linkedSkullPedestals = new HashMap<>();
    private boolean dirty = false;

    public PersistentLinksState() {
        super();
    }

    public NbtCompound writeNbt(NbtCompound tag) {
        SkullMagic.LOGGER.info("Writing nbt for PersistentLinksState");
        NbtCompound altarLinks = new NbtCompound();
        linkedAltars.keySet().forEach(uuid -> {
            SkullMagic.LOGGER.info(
                    "Saving link for '" + uuid + "' and '" + parseBlockposToString(this.linkedAltars.get(uuid)) + "'");
            altarLinks.putString(uuid.toString(), parseBlockposToString(linkedAltars.get(uuid)));
        });
        tag.put("altarLinks", altarLinks);

        NbtCompound pedestalLinks = new NbtCompound();
        linkedSkullPedestals.keySet().forEach(pedestalPos -> {
            String pedestalPosString = parseBlockposToString(pedestalPos);
            String altarPosString = parseBlockposToString(linkedSkullPedestals.get(pedestalPos));
            SkullMagic.LOGGER.info("Saving pedestal link for " + pedestalPosString + " and " + altarPosString);
            pedestalLinks.putString(pedestalPosString, altarPosString);
        });
        tag.put("pedestalLinks", pedestalLinks);
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
        PersistentLinksState tps = new PersistentLinksState();
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

        tps.linkedSkullPedestals.clear();
        if (tag.contains("pedestalLinks")) {
            try {
                NbtCompound pedestalLinks = tag.getCompound("pedestalLinks");
                SkullMagic.LOGGER.info("Successfully loaded pedestalLinks from NBT data: ");
                pedestalLinks.getKeys().forEach(pedestalPos -> {
                    System.out.println(
                            "Loaded link for '" + pedestalPos + "' and '" + pedestalLinks.getString(pedestalPos) + "'");
                    tps.linkedSkullPedestals.put(parseBlockPosFromNBTString(pedestalPos),
                            parseBlockPosFromNBTString(pedestalLinks.getString(pedestalPos)));
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

    public void addAltarLink(UUID playerID, BlockPos pos) {
        this.linkedAltars.put(playerID, pos);
        this.dirty = true;
    }

    public void removeAltarLink(UUID playerID, BlockPos pos) {
        this.linkedAltars.remove(playerID, pos);
        this.dirty = true;
    }

    public void addPedestalLink(BlockPos pedestalPos, BlockPos altarPos) {
        this.linkedSkullPedestals.put(pedestalPos, altarPos);
        this.dirty = true;
    }

    public void removePedestalLink(BlockPos pedestalPos, BlockPos altarPos) {
        this.linkedSkullPedestals.remove(pedestalPos, altarPos);
        this.dirty = true;
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
        // remove all player links to altar
        ArrayList<UUID> playerLinksToRemove = new ArrayList<>();
        for (Entry<UUID, BlockPos> entry : this.linkedAltars.entrySet()) {
            if (pos.equals(entry.getValue())) {
                playerLinksToRemove.add(entry.getKey());
            }
        }
        if (playerLinksToRemove.size() != 0) {
            playerLinksToRemove.forEach((uuid) -> {
                this.linkedAltars.remove(uuid);
            });
            // TODO: send update to player whose altar has been unlinked
        } else {
            SkullMagic.LOGGER.warn(
                    "Could not remove the altar from linking nbt, as it was not found (pos: " + pos.toString() + ")");
        }

        // remove all pedestal links to altar
        ArrayList<BlockPos> pedestalLinksToRemove = new ArrayList<>();
        for (Entry<BlockPos, BlockPos> entry : this.linkedSkullPedestals.entrySet()) {
            if (pos.equals(entry.getValue())) {
                pedestalLinksToRemove.add(entry.getKey());
            }
        }
        for (BlockPos blockPos : pedestalLinksToRemove) {
            this.linkedSkullPedestals.remove(blockPos);
        }
        SkullMagic.LOGGER.info("Removed " + pedestalLinksToRemove.size() + " pedestal links.");
        this.dirty = true;
    }

    public boolean isPedestalLinked(BlockPos pos) {
        return this.linkedSkullPedestals.containsKey(pos);
    }

    public BlockPos getPedestalLinkedAltarBlockPos(BlockPos pos) {
        BlockPos result = null;
        if (isPedestalLinked(pos)) {
            result = this.linkedSkullPedestals.get(pos);
        }
        return result;
    }

    public ArrayList<BlockPos> getPedestalsLinkedToAltar(BlockPos altarPos) {
        ArrayList<BlockPos> pedestals = new ArrayList<>();
        for (Entry<BlockPos, BlockPos> entry : this.linkedSkullPedestals.entrySet()) {
            if (entry.getValue().equals(altarPos)) {
                pedestals.add(entry.getKey());
            }
        }
        return pedestals;
    }

    public String getPlayerLinkedToAltar(BlockPos pos) {
        for (Entry<UUID, BlockPos> set : this.linkedAltars.entrySet()) {
            if (set.getValue().equals(pos)) {
                return set.getKey().toString();
            }
        }
        return "";
    }
}
