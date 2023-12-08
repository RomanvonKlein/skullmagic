package com.romanvonklein.skullmagic.blockEntities;

import com.romanvonklein.skullmagic.SkullMagic;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

import java.util.Set;

import com.google.common.collect.Sets;
@Environment(value=EnvType.CLIENT)
public class SkullMagicEntityModelLayers extends EntityModelLayers {
    private static final String MAIN = "main";
    private static final Set<EntityModelLayer> LAYERS = Sets.newHashSet();
    public static final EntityModelLayer BLAZE_HEAD = SkullMagicEntityModelLayers.registerMain("blaze_head");
    public static final EntityModelLayer ENDERMAN_HEAD = SkullMagicEntityModelLayers.registerMain("enderman_head");
    public static final EntityModelLayer SPIDER_HEAD = SkullMagicEntityModelLayers.registerMain("spider_head");

    private static EntityModelLayer registerMain(String id) {
        return SkullMagicEntityModelLayers.register(id, MAIN);
    }

    private static EntityModelLayer register(String id, String layer) {
        EntityModelLayer entityModelLayer = SkullMagicEntityModelLayers.create(id, layer);
        if (!LAYERS.add(entityModelLayer)) {
            throw new IllegalStateException("Duplicate registration for " + entityModelLayer);
        }
        return entityModelLayer;
    }

    private static EntityModelLayer create(String id, String layer) {
        return new EntityModelLayer(new Identifier(SkullMagic.MODID, id), layer);
    }
}
