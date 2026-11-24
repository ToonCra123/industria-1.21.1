package net.industria.industria.client.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import net.neoforged.neoforge.client.model.obj.ObjModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public record SplitModelGeometry(ObjModel obj, BlockPos size) implements IUnbakedGeometry<SplitModelGeometry> {
    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter, ModelState state, ItemOverrides overrides) {
        BakedModel source = obj.bake(context, baker, spriteGetter, state, overrides);
        RandomSource random = RandomSource.create(0);
        List<BakedQuad> quads = new ArrayList<>(source.getQuads(null, null, random, ModelData.EMPTY, null));
        // The OBJ loader culls relative to a single unit cube. Recover every face and assign
        // cull sides again after clipping, when each fragment has a well-defined owner block.
        for (Direction side : Direction.values()) {
            quads.addAll(source.getQuads(null, side, random, ModelData.EMPTY, null));
        }

        // The baked model owns the frozen cache, so a resource reload rebuilds it with the new atlas.
        return new SplitBakedModel(source, ModelSplitter.split(quads, size, obj.automaticCulling));
    }

    @Override
    public Set<String> getConfigurableComponentNames() {
        return obj.getConfigurableComponentNames();
    }
}
