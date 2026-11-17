package net.industria.industria.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

// Stollen from previous project
public class IndustriaDatagen {
    public static void generate(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new IndustriaBlockModels(packOutput, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new IndustriaItemModels(packOutput, event.getExistingFileHelper()));
    }
}
