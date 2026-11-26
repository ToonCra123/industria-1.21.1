package net.industria.industria.client.compat;

import mcjty.theoneprobe.api.ITheOneProbe;

import net.industria.industria.Industria;
import net.industria.industria.common.registration.Registration;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModList;

import java.util.function.Function;

public class TOPCompat {

    public static void register() {
        if (!ModList.get().isLoaded("theoneprobe")) {
            return;
        }

        InterModComms.sendTo(
                "theoneprobe",
                "getTheOneProbe",
                GetTheOneProbe::new
        );
    }

    public static class GetTheOneProbe
            implements Function<ITheOneProbe, Void> {

        @Override
        public Void apply(ITheOneProbe probe) {

            probe.registerBlockDisplayOverride(
                    (mode, probeInfo, player, level, state, hitData) -> {

                        if (!state.is(Registration.COKE_OVEN_CONTROLLER_BLOCK.get())
                                && !state.is(Registration.COKE_OVEN_BLOCK.get())) {
                            return false;
                        }

                        probeInfo.horizontal()
                                .item(new ItemStack(
                                        Registration.BLAST_FURNACE_DISPLAY.get()
                                ))
                                .vertical()
                                .text(Component.translatable(
                                        "item.industria.blast_furnace_display"
                                ))
                                .text(Component.literal("Industria"));

                        return true;
                    });

            Industria.LOGGER.info("TOP compatibility registered");

            return null;
        }
    }
}