package net.industria.industria.client;

import net.industria.industria.Registration;
import net.industria.industria.client.screen.CokeOvenScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import static net.industria.industria.Industria.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(Registration.COKE_OVEN_CONTAINER.get(), CokeOvenScreen::new);
    }

    @SubscribeEvent
    public static void initClient(EntityRenderersEvent.RegisterRenderers event) {

    }

}
