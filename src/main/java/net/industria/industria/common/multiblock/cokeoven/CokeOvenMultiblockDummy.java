
package net.industria.industria.common.multiblock.cokeoven;

import net.industria.industria.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CokeOvenMultiblockDummy extends BlockEntity {

    private BlockPos controllerPos;

    public CokeOvenMultiblockDummy(
            BlockPos pos,
            BlockState blockState
    ) {
        super(Registration.COKE_OVEN_BLOCK_ENTITY.get(), pos, blockState);
    }

    public BlockPos getControllerPos() {
        return controllerPos;
    }

    public void setControllerPos(BlockPos controllerPos) {
        this.controllerPos = controllerPos.immutable();
        setChanged();
    }

    public CokeOvenMultiblockController getController() {

        if (level == null || controllerPos == null) {
            return null;
        }

        if (!level.hasChunkAt(controllerPos)) {
            return null;
        }

        if (level.getBlockEntity(controllerPos)
                instanceof CokeOvenMultiblockController controller) {
            return controller;
        }

        return null;
    }

    @Override
    protected void saveAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.saveAdditional(tag, registries);

        if (controllerPos != null) {
            tag.putLong("ControllerPos", controllerPos.asLong());
        }
    }

    @Override
    protected void loadAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.loadAdditional(tag, registries);

        controllerPos = tag.contains("ControllerPos")
                ? BlockPos.of(tag.getLong("ControllerPos"))
                : null;
    }

    public BlockPos getRelativePos() {
        return controllerPos == null
                ? BlockPos.ZERO
                : getBlockPos().subtract(controllerPos);
    }
}
