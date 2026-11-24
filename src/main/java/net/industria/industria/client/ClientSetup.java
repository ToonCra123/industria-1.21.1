package net.industria.industria.client;

import net.industria.industria.Registration;
import net.industria.industria.client.model.SplitModelLoader;
import net.industria.industria.client.screen.CokeOvenScreen;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import static net.industria.industria.Industria.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(Registration.COKE_OVEN_CONTAINER.get(), CokeOvenScreen::new);
    }

    @SubscribeEvent
    public static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(ResourceLocation.fromNamespaceAndPath(MODID, "split"), new SplitModelLoader());
    }

}
