package net.industria.industria.common.multiblock.cokeoven;

import net.industria.industria.Registration;
import net.industria.industria.common.multiblock.MultiblockModelProperties;
import net.industria.industria.common.recipe.blast.BlastFurnaceRecipe;
import net.industria.industria.common.recipe.blast.BlastFurnaceRecipeInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class CokeOvenMultiblockController extends BlockEntity {
    private final Set<BlockPos> blocksInMultiblock = new HashSet<>();

    public static final String ITEMS_TAG = "Inventory";

    private ItemStackHandler items = createItemHandler();
    private Lazy<IItemHandler> itemHandler = Lazy.of(() -> items);

    public static final int SLOT_COUNT = 4;
    public static final int IO_1_SLOT = 0;
    public static final int IO_2_SLOT = 1;
    public static final int FUEL_SLOT = 2;
    public static final int OUTPUT_SLOT = 3;

    private int progress = 0;
    private int maxProgress = 0;

    private int burnTime = 0;
    private int maxBurnTime = 0;

    private ResourceLocation activeRecipeId;

    public CokeOvenMultiblockController(
            BlockPos pos,
            BlockState blockState
    ) {
        super(Registration.COKE_OVEN_CONTROLLER_BLOCK_ENTITY.get(), pos, blockState);

        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    blocksInMultiblock.add(pos.offset(x, y, z));
                }
            }
        }
    }

    public Set<BlockPos> getBlocksInMultiblock() {
        return Collections.unmodifiableSet(blocksInMultiblock);
    }

    public BlockPos getRelativePos() {
        return BlockPos.ZERO;
    }

    @Override
    public ModelData getModelData() {
        return MultiblockModelProperties.forOffset(getRelativePos());
    }

    public void tickServer() {

        if (level == null || level.isClientSide) {
            return;
        }

        Optional<RecipeHolder<BlastFurnaceRecipe>> optional =
                getCurrentRecipe();

        if (optional.isEmpty()) {

            resetProgress();

        } else {

            RecipeHolder<BlastFurnaceRecipe> holder = optional.get();
            BlastFurnaceRecipe recipe = holder.value();

            // Reset progress when the recipe changes.
            if (!holder.id().equals(activeRecipeId)) {

                progress = 0;
                activeRecipeId = holder.id();
                maxProgress = recipe.processingTime();

                setChanged();
            }

            if (maxProgress != recipe.processingTime()) {
                progress = 0;
                maxProgress = recipe.processingTime();
                setChanged();
            }

            if (canFitOutput(recipe.result())) {

                // Only consume new fuel when needed.
                if (burnTime <= 0) {
                    consumeFuel();
                }

                if (burnTime > 0) {

                    progress++;

                    if (progress >= maxProgress) {

                        craftRecipe(recipe);

                        progress = 0;
                    }

                    setChanged();
                }
            }
        }

        // Existing fuel continues burning even when idle.
        if (burnTime > 0) {
            burnTime--;
            setChanged();
        }
    }

    @Nonnull
    private ItemStackHandler createItemHandler() {
        return new ItemStackHandler(SLOT_COUNT) {

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                switch(slot) {
                    case IO_1_SLOT, IO_2_SLOT -> {
                        return true;
                    }
                    case FUEL_SLOT -> {
                        return (stack.getBurnTime(RecipeType.SMELTING) > 0);
                    }
                    default -> {
                        return false;
                    }
                }
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
    }

    public ItemStackHandler getItems() {
        return items;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put(ITEMS_TAG, items.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if(tag.contains(ITEMS_TAG)) {
            items.deserializeNBT(registries, tag.getCompound(ITEMS_TAG));
        }
    }

    private Optional<RecipeHolder<BlastFurnaceRecipe>> getCurrentRecipe() {

        if (level == null || level.isClientSide) {
            return Optional.empty();
        }

        BlastFurnaceRecipeInput input =
                new BlastFurnaceRecipeInput(
                        items.getStackInSlot(0),
                        items.getStackInSlot(1)
                );

        return level.getRecipeManager().getRecipeFor(
                Registration.BLAST_FURNACE_RECIPE_TYPE.get(),
                input,
                level
        );
    }

    private boolean canFitOutput(ItemStack result) {

        ItemStack output = items.getStackInSlot(OUTPUT_SLOT);

        int limit = Math.min(
                items.getSlotLimit(OUTPUT_SLOT),
                result.getMaxStackSize()
        );

        if (output.isEmpty()) {
            return result.getCount() <= limit;
        }

        return ItemStack.isSameItemSameComponents(output, result)
                && output.getCount() + result.getCount() <= limit;
    }

    private boolean consumeFuel() {

        ItemStack fuel = items.getStackInSlot(FUEL_SLOT);

        if (fuel.isEmpty()) {
            return false;
        }

        int duration = fuel.getBurnTime(RecipeType.SMELTING);

        if (duration <= 0) {
            return false;
        }

        ItemStack remainder = fuel.getCraftingRemainingItem();

        // Make sure there is room for a remaining container.
        if (!remainder.isEmpty() && fuel.getCount() > 1) {
            return false;
        }

        items.extractItem(FUEL_SLOT, 1, false);

        if (!remainder.isEmpty()) {
            items.setStackInSlot(FUEL_SLOT, remainder);
        }

        burnTime = duration;
        maxBurnTime = duration;

        setChanged();

        return true;
    }

    private void craftRecipe(BlastFurnaceRecipe recipe) {

        ItemStack result = recipe.result().copy();

        items.extractItem(
                IO_1_SLOT,
                recipe.primaryCount(),
                false
        );

        items.extractItem(
                IO_2_SLOT,
                recipe.secondaryCount(),
                false
        );

        ItemStack output = items.getStackInSlot(OUTPUT_SLOT);

        if (output.isEmpty()) {
            items.setStackInSlot(OUTPUT_SLOT, result);
        } else {
            ItemStack combined = output.copy();
            combined.grow(result.getCount());

            items.setStackInSlot(OUTPUT_SLOT, combined);
        }

        setChanged();
    }

    private void resetProgress() {

        if (progress != 0
                || maxProgress != 0
                || activeRecipeId != null) {

            progress = 0;
            maxProgress = 0;
            activeRecipeId = null;

            setChanged();
        }
    }
}
