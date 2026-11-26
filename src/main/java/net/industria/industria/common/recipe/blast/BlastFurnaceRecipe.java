package net.industria.industria.common.recipe.blast;

import net.industria.industria.common.registration.Registration;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public record BlastFurnaceRecipe(
        Ingredient primary,
        Ingredient secondary,
        int primaryCount,
        int secondaryCount,
        ItemStack result,
        int processingTime
) implements Recipe<BlastFurnaceRecipeInput> {

    @Override
    public boolean matches(BlastFurnaceRecipeInput input, Level level) {
        return primary.test(input.primary())
                && secondary.test(input.secondary())
                && input.primary().getCount() >= primaryCount
                && input.secondary().getCount() >= secondaryCount;
    }

    @Override
    public ItemStack assemble(
            BlastFurnaceRecipeInput input,
            HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Registration.BLAST_FURNACE_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return Registration.BLAST_FURNACE_RECIPE_TYPE.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }
}
