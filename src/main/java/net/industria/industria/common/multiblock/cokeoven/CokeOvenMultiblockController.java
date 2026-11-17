package net.industria.industria.common.multiblock.cokeoven;

import net.industria.industria.utils.multiblock.MultiblockController;
import net.minecraft.world.level.Level;

public class CokeOvenMultiblockController extends MultiblockController {
    public CokeOvenMultiblockController(Level level) {
        super(level);
    }

    @Override
    public boolean isMachineWhole() {
        return false;
    }


}
