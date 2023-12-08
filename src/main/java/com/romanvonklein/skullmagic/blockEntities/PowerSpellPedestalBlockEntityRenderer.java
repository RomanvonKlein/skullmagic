package com.romanvonklein.skullmagic.blockEntities;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;

@Environment(EnvType.CLIENT)
public class PowerSpellPedestalBlockEntityRenderer
        extends ASpellPedestalBlockEntityRenderer<PowerSpellPedestalBlockEntity> {
    public PowerSpellPedestalBlockEntityRenderer(BlockEntityRendererFactory.Context context) {

    }
}
