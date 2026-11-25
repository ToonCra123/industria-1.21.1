package net.industria.industria.common.multiblock.cokeoven;

import net.industria.industria.common.container.CokeOvenContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class CokeOvenBlock extends Block implements EntityBlock {
    public static final String SCREEN_COKE_OVEN = "industria.screen.coke_oven";

    public CokeOvenBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CokeOvenMultiblockDummy(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CokeOvenMultiblockDummy dum) {
                BlockPos controllerPos = dum.getControllerPos();
                MenuProvider containerProvider = new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return Component.translatable(SCREEN_COKE_OVEN);
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
                        return new CokeOvenContainer(windowId, playerEntity, controllerPos);
                    }
                };
                player.openMenu(containerProvider, buf -> buf.writeBlockPos(controllerPos));
            } else {
                throw new IllegalStateException("Our named container provider is missing!");
            }
        }
        return InteractionResult.SUCCESS;
    }


    @Override
    public void onRemove(BlockState state, Level level,
                         BlockPos pos, BlockState newState,
                         boolean movedByPiston) {

        if (!state.is(newState.getBlock()) && !level.isClientSide) {

            if (level.getBlockEntity(pos)
                    instanceof CokeOvenMultiblockDummy dummy) {

                BlockPos controllerPos = dummy.getControllerPos();

                if (controllerPos != null
                        && level.getBlockEntity(controllerPos)
                        instanceof CokeOvenMultiblockController controller) {

                    controller.dismantle(pos);
                }
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }


}
