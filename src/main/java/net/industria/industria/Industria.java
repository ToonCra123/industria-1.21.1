package net.industria.industria;

import net.industria.industria.datagen.IndustriaDatagen;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(Industria.MODID)
public class Industria {
    public static final String MODID = "industria";
    public static final Logger LOGGER = LogUtils.getLogger();


    public Industria(IEventBus modEventBus, ModContainer modContainer) {
        Registration.init(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(IndustriaDatagen::generate);
    }


    // これは分っかない
    private void commonSetup(FMLCommonSetupEvent event) {
    }

}
