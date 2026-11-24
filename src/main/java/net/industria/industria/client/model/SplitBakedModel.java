package net.industria.industria.client.model;

import net.industria.industria.common.multiblock.MultiblockModelProperties;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public final class SplitBakedModel extends BakedModelWrapper<BakedModel> {
    private static final ModelData ORIGIN = MultiblockModelProperties.forOffset(BlockPos.ZERO);
    private final Map<Direction, Map<BlockPos, ModelSplitter.Section>> sections;

    SplitBakedModel(BakedModel source, Map<Direction, Map<BlockPos, ModelSplitter.Section>> sections) {
        super(source);
        this.sections = sections;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random) {
        return getQuads(state, side, random, ORIGIN, null);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random,
                                   ModelData modelData, @Nullable RenderType renderType) {
        if (state == null) {
            return super.getQuads(null, side, random, modelData, renderType);
        }

        BlockPos offset = modelData.get(MultiblockModelProperties.RELATIVE_POS);
        if (offset == null || (renderType != null && !getRenderTypes(state, random, modelData).contains(renderType))) {
            return List.of();
        }

        Direction facing = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING) : Direction.NORTH;
        ModelSplitter.Section section = sections.get(facing).get(offset);
        return section == null ? List.of() : section.getQuads(side);
    }
}
