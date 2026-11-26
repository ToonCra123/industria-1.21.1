package net.industria.industria.common.multiblock.alloyer;

import net.industria.industria.common.registration.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class AlloyerMultiblockFormer {
    public static final AlloyerMultiblockFormer FORMER = new AlloyerMultiblockFormer();

    private int X_SIZE = 3;
    private int Y_SIZE = 2;
    private int Z_SIZE = 2;


    // checks +x -> +y -> +z
    private final Block[] BLOCK_LIST = {
            // 0,0,0
            Registration.STEEL_SCAFFOLDING.get(),
            Registration.BASIC_CONTROLLER.get(),
            Registration.STEEL_SCAFFOLDING.get(),
            // 0,1,0
            Registration.STEEL_SCAFFOLDING.get(),
            Registration.STEEL_SCAFFOLDING.get(),
            Registration.STEEL_SCAFFOLDING.get(),
            // 0,0,1
            Registration.STEEL_SCAFFOLDING.get(),
            Registration.STEEL_SCAFFOLDING.get(),
            Registration.STEEL_SCAFFOLDING.get(),
            // 0,1,1
            Registration.STEEL_SCAFFOLDING.get(),
            Registration.STEEL_BLOCK.get(),
            Registration.STEEL_SCAFFOLDING.get(),
    };

    private AlloyerMultiblockFormer() {}

    private int getIndex(int x, int y, int z) {
        return x + (y * X_SIZE) + (z * X_SIZE * Y_SIZE);
    }

    private boolean isValidStructure(Level world,
                                     Direction facing,
                                     BlockPos origin) {

        for (int z = 0; z < Z_SIZE; z++) {
            for (int y = 0; y < Y_SIZE; y++) {
                for (int x = 0; x < X_SIZE; x++) {

                    BlockPos pos = getWorldPos(
                            origin, facing, x, y, z
                    );

                    Block required = BLOCK_LIST[
                            getIndex(x, y, z)
                            ];

                    if (!world.hasChunkAt(pos)) {
                        return false;
                    }

                    if (!world.getBlockState(pos).is(required)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private BlockPos getWorldPos(BlockPos origin,
                                 Direction facing,
                                 int x, int y, int z) {

        Direction right = facing.getClockWise();
        Direction back = facing.getOpposite();

        return origin
                .relative(right, x)
                .above(y)
                .relative(back, z);
    }

    public boolean form(Level world, BlockPos controllerPos) {
        if (world.isClientSide) return false;

        for (Direction facing : Direction.Plane.HORIZONTAL) {

            BlockPos origin = getOrigin(controllerPos, facing);

            if (!isValidStructure(world, facing, origin)) continue;

            return true;
        }

        return false;
    }

    private BlockPos getOrigin(BlockPos controllerPos,
                               Direction facing) {

        return controllerPos.relative(
                facing.getClockWise().getOpposite()
        );
    }
}
