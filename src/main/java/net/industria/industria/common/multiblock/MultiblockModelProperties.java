package net.industria.industria.common.multiblock;

import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

/** Shared model data contract. These NeoForge data containers are available on both sides. */
public final class MultiblockModelProperties {
    public static final ModelProperty<BlockPos> RELATIVE_POS = new ModelProperty<>();

    private MultiblockModelProperties() {}

    public static ModelData forOffset(BlockPos offset) {
        return ModelData.builder().with(RELATIVE_POS, offset.immutable()).build();
    }
}
