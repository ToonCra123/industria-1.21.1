package net.industria.industria.datagen;

import net.industria.industria.Industria;
import net.industria.industria.common.registration.Registration;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class IndustriaBlockModels extends BlockStateProvider {
    public IndustriaBlockModels(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Industria.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlock(Registration.COKE_BRICK.get());
        var cokeOvenModel = models().getExistingFile(modLoc("block/coke_oven"));
        simpleBlock(Registration.COKE_OVEN_BLOCK.get(), cokeOvenModel);
        simpleBlock(Registration.COKE_OVEN_CONTROLLER_BLOCK.get(), cokeOvenModel);
        simpleBlock(Registration.STEEL_BLOCK.get());
        simpleBlock(Registration.SILVER_ORE.get());
        simpleBlock(Registration.BASIC_CONTROLLER.get());
        simpleBlockWithItem(
                Registration.STEEL_SCAFFOLDING.get(),
                models().cubeBottomTop(
                        "steel_scaffolding",
                        modLoc("block/steel_scaffolding_side"),
                        modLoc("block/steel_scaffolding_top"),
                        modLoc("block/steel_scaffolding_top")
                ).renderType("minecraft:cutout")
        );

    }
}
