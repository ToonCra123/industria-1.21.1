package net.industria.industria.common.multiblock.cokeoven;

import net.industria.industria.utils.multiblock.MultiblockController;
import net.minecraft.world.level.Level;

public class CokeOvenMultiblockController extends MultiblockController {
    private CokeOvenMultiblockLogic logic;

    public CokeOvenMultiblockController(Level level) {
        super(level);
        logic = new CokeOvenMultiblockLogic();
    }

    @Override
    public boolean isMachineWhole() {
        return false;
    }


}
