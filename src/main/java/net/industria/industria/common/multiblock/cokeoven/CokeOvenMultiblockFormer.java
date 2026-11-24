package net.industria.industria.common.multiblock.cokeoven;

import net.industria.industria.Industria;
import net.industria.industria.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CokeOvenMultiblockFormer {

    public static final CokeOvenMultiblockFormer FORMER =
            new CokeOvenMultiblockFormer();

    private static final int SIZE = 2;

    private CokeOvenMultiblockFormer() {
    }

    /**
     * Finds the origin of a valid 2x2x2 coke oven.
     *
     * @param world The world
     * @param dir   Direction the multiblock faces
     * @param pos   Any block position within the structure
     * @return The origin, or null if no valid structure exists
     */
    public BlockPos findOrigin(Level world, BlockPos pos) {

        for (int offsetX = 0; offsetX < SIZE; offsetX++) {
            for (int offsetY = 0; offsetY < SIZE; offsetY++) {
                for (int offsetZ = 0; offsetZ < SIZE; offsetZ++) {

                    BlockPos origin = pos.offset(
                            -offsetX,
                            -offsetY,
                            -offsetZ
                    );

                    if (isValidStructure(world, origin)) {
                        return origin;
                    }
                }
            }
        }

        return null;
    }

    private boolean isValidStructure(Level world, BlockPos origin) {

        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                for (int z = 0; z < SIZE; z++) {

                    BlockPos checkPos = origin.offset(x, y, z);

                    if (!world.hasChunkAt(checkPos)) {
                        return false;
                    }

                    if (!world.getBlockState(checkPos)
                            .is(Registration.COKE_BRICK.get())) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public boolean isFormed(Level world, BlockPos pos) {
        return findOrigin(world, pos) != null;
    }




    public boolean form(Level world, Direction dir, BlockPos origin) {

        if (world.isClientSide) {
            return false;
        }

        if (dir.getAxis().isVertical()) {
            Industria.LOGGER.warn("Invalid direction");
            return false;
        }

        if (!isValidStructure(world, origin)) {
            Industria.LOGGER.warn("Invalid structure at {}", origin);
            return false;
        }

        Industria.LOGGER.info("Forming coke oven at {}", origin);

        // Place controller
        boolean placed = world.setBlock(
                origin,
                Registration.COKE_OVEN_CONTROLLER_BLOCK.get()
                        .defaultBlockState(),
                Block.UPDATE_ALL
        );

        if (!placed) {
            Industria.LOGGER.error("Failed to place controller");
            return false;
        }

        BlockEntity controllerBE = world.getBlockEntity(origin);

        if (!(controllerBE instanceof CokeOvenMultiblockController controller)) {
            Industria.LOGGER.error(
                    "Expected controller BE, found: {}", controllerBE
            );
            return false;
        }

        Industria.LOGGER.info("Controller created successfully");

        // Place dummy blocks
        for (BlockPos pos : controller.getBlocksInMultiblock()) {

            if (pos.equals(origin)) {
                continue;
            }

            boolean dummyPlaced = world.setBlock(
                    pos,
                    Registration.COKE_OVEN_BLOCK.get()
                            .defaultBlockState(),
                    Block.UPDATE_ALL
            );

            if (!dummyPlaced) {
                Industria.LOGGER.error("Failed to place dummy at {}", pos);
                return false;
            }

            BlockEntity dummyBE = world.getBlockEntity(pos);

            if (!(dummyBE instanceof CokeOvenMultiblockDummy dummy)) {
                Industria.LOGGER.error(
                        "Expected dummy BE at {}, found: {}", pos, dummyBE
                );
                return false;
            }

            dummy.setControllerPos(origin);

            Industria.LOGGER.info("Dummy created at {}", pos);
        }

        controller.setChanged();

        Industria.LOGGER.info("Coke oven formed successfully");

        return true;
    }




}
