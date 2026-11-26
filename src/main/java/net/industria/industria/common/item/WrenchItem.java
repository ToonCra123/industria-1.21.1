package net.industria.industria.common.item;

import net.industria.industria.common.multiblock.cokeoven.CokeOvenMultiblockFormer;
import net.industria.industria.common.registration.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class WrenchItem extends Item {
    public WrenchItem(Properties properties) {
        super(properties);
    }


    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction dir = context.getClickedFace();

        BlockState clickedBlockState = level.getBlockState(pos);
        if(clickedBlockState.is(Registration.COKE_BRICK.get())) {
            BlockPos origin = CokeOvenMultiblockFormer.FORMER
                    .findOrigin(level, pos);

            if (origin == null) {
                return InteractionResult.PASS;
            }

            if (!level.isClientSide) {

                if (!CokeOvenMultiblockFormer.FORMER.form(level, dir, origin)) {
                    return InteractionResult.FAIL;
                }
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        } else if (clickedBlockState.is(Registration.BASIC_CONTROLLER.get())) {
            // do something
        }
        return InteractionResult.PASS;
    }
}