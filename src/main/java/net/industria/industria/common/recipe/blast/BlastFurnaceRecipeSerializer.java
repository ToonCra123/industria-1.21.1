package net.industria.industria.common.recipe.blast;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class BlastFurnaceRecipeSerializer
        implements RecipeSerializer<BlastFurnaceRecipe> {

    public static final MapCodec<BlastFurnaceRecipe> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(

                    Ingredient.CODEC_NONEMPTY
                            .fieldOf("primary")
                            .forGetter(BlastFurnaceRecipe::primary),

                    Ingredient.CODEC_NONEMPTY
                            .fieldOf("secondary")
                            .forGetter(BlastFurnaceRecipe::secondary),

                    Codec.intRange(1, 64)
                            .fieldOf("primary_count")
                            .forGetter(BlastFurnaceRecipe::primaryCount),

                    Codec.intRange(1, 64)
                            .fieldOf("secondary_count")
                            .forGetter(BlastFurnaceRecipe::secondaryCount),

                    ItemStack.CODEC
                            .fieldOf("result")
                            .forGetter(BlastFurnaceRecipe::result),

                    Codec.intRange(1, Integer.MAX_VALUE)
                            .fieldOf("processing_time")
                            .forGetter(BlastFurnaceRecipe::processingTime)

            ).apply(instance, BlastFurnaceRecipe::new));

    public static final StreamCodec<
            RegistryFriendlyByteBuf, BlastFurnaceRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC,
                    BlastFurnaceRecipe::primary,

                    Ingredient.CONTENTS_STREAM_CODEC,
                    BlastFurnaceRecipe::secondary,

                    ByteBufCodecs.VAR_INT,
                    BlastFurnaceRecipe::primaryCount,

                    ByteBufCodecs.VAR_INT,
                    BlastFurnaceRecipe::secondaryCount,

                    ItemStack.STREAM_CODEC,
                    BlastFurnaceRecipe::result,

                    ByteBufCodecs.VAR_INT,
                    BlastFurnaceRecipe::processingTime,

                    BlastFurnaceRecipe::new
            );

    @Override
    public MapCodec<BlastFurnaceRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, BlastFurnaceRecipe>
    streamCodec() {
        return STREAM_CODEC;
    }
}
