package net.industria.industria.common.blockentities;

import net.industria.industria.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CokeBrickBlockEntity extends BlockEntity {
    public CokeBrickBlockEntity(BlockPos pos, BlockState blockState) {
        super(Registration.COKE_BRICK_BLOCK_ENTITY.get(), pos, blockState);
    }

    public void tickServer() {

    }
}
