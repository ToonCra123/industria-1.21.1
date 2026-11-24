package net.industria.industria.common.multiblock.cokeoven;

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
import java.util.Collections;
import java.util.HashSet;
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

    public void tickServer() {
        return;
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
