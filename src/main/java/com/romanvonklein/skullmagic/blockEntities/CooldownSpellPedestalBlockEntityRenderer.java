package com.romanvonklein.skullmagic.blockEntities;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;

@Environment(EnvType.CLIENT)
public class CooldownSpellPedestalBlockEntityRenderer
        extends ASpellPedestalBlockEntityRenderer<CooldownSpellPedestalBlockEntity> {
    public CooldownSpellPedestalBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

}
