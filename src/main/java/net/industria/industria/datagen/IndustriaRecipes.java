package net.industria.industria.datagen;

import net.industria.industria.common.registration.Registration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;

import java.util.concurrent.CompletableFuture;

public class IndustriaRecipes extends RecipeProvider {
    public IndustriaRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(
                        RecipeCategory.BUILDING_BLOCKS,
                        Registration.STEEL_BLOCK.get()
                )
                .pattern("SSS")
                .pattern("SSS")
                .pattern("SSS")
                .define('S', Registration.STEEL_INGOT.get())
                .unlockedBy("has_steel", has(Registration.STEEL_INGOT.get()))
                .save(output);
    }
}
