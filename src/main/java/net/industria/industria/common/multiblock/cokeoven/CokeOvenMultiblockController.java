package net.industria.industria.common.multiblock.cokeoven;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;

public class CokeOvenMultiblockController extends BlockEntity {
    public HashSet<BlockPos> blocksInMultiblock;

    public CokeOvenMultiblockController(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }
}
