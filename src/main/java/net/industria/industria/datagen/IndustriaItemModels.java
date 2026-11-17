package net.industria.industria.datagen;

import net.industria.industria.Industria;
import net.industria.industria.Registration;
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

        // Block Items
        withExistingParent(Registration.COKE_BRICK.getId().getPath(), modLoc("block/coke_brick"));
    }
}
