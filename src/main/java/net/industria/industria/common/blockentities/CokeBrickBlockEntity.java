package net.industria.industria.common.blockentities;

import net.industria.industria.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class CokeBrickBlockEntity extends BlockEntity {
    public static final String ITEMS_TAG = "Inventory";

    private ItemStackHandler items = createItemHandler();
    private Lazy<IItemHandler> itemHandler = Lazy.of(() -> items);

    public static final int SLOT_COUNT = 4;
    public static final int IO_1_SLOT = 0;
    public static final int IO_2_SLOT = 1;
    public static final int FUEL_SLOT = 2;
    public static final int OUTPUT_SLOT = 3;

    public CokeBrickBlockEntity(BlockPos pos, BlockState blockState) {
        super(Registration.COKE_BRICK_BLOCK_ENTITY.get(), pos, blockState);
    }

    public void tickServer() {
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
}
