package net.industria.industria.datagen;

import net.industria.industria.Industria;
import net.industria.industria.common.registration.Registration;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class IndustriaItemModels extends ItemModelProvider {

    public IndustriaItemModels(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Industria.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // Items
        basicItem(Registration.STEEL_INGOT.get());
        basicItem(Registration.WRENCH.get());
        basicItem(Registration.SILVER_INGOT.get());

        // Block Items
        withExistingParent(Registration.COKE_BRICK.getId().getPath(), modLoc("block/coke_brick"));
        withExistingParent(Registration.STEEL_SCAFFOLDING.getId().getPath(), modLoc("block/steel_scaffolding"));
        withExistingParent(Registration.STEEL_BLOCK.getId().getPath(), modLoc("block/steel_block"));
        withExistingParent(Registration.SILVER_ORE.getId().getPath(), modLoc("block/silver_ore"));
        withExistingParent(Registration.BASIC_CONTROLLER.getId().getPath(), modLoc("block/basic_controller"));
    }
}
