
package net.industria.industria.common.multiblock.cokeoven;

import net.industria.industria.Registration;
import net.industria.industria.common.multiblock.MultiblockModelProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.Objects;

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
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
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

        BlockPos previous = controllerPos;
        controllerPos = tag.contains("ControllerPos")
                ? BlockPos.of(tag.getLong("ControllerPos"))
                : null;
        if (level != null && level.isClientSide && !Objects.equals(previous, controllerPos)) {
            // Both the chunk tag and live update packet reach this method.
            requestModelDataUpdate();
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public ModelData getModelData() {
        // The saved controller position is sufficient even when its chunk is absent.
        return controllerPos == null ? ModelData.EMPTY : MultiblockModelProperties.forOffset(getRelativePos());
    }

    public BlockPos getRelativePos() {
        return controllerPos == null
                ? BlockPos.ZERO
                : getBlockPos().subtract(controllerPos);
    }
}
