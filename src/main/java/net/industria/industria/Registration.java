package net.industria.industria;

import net.industria.industria.common.block.CokeBrickBlock;
import net.industria.industria.common.container.CokeOvenContainer;
import net.industria.industria.common.item.WrenchItem;
import net.industria.industria.common.multiblock.cokeoven.CokeOvenBlock;
import net.industria.industria.common.multiblock.cokeoven.CokeOvenControllerBlock;
import net.industria.industria.common.multiblock.cokeoven.CokeOvenMultiblockController;
import net.industria.industria.common.multiblock.cokeoven.CokeOvenMultiblockDummy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.*;

import java.util.function.Supplier;

import static net.industria.industria.Industria.MODID;

public class Registration {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, MODID);
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);


    // Blocks
    public static final DeferredBlock<CokeBrickBlock> COKE_BRICK = BLOCKS.registerBlock("coke_brick", CokeBrickBlock::new,
            BlockBehaviour.Properties.of().sound(SoundType.DEEPSLATE_BRICKS).explosionResistance(6.0f).strength(2.0f));

    // Block Items
    public static final DeferredItem<BlockItem> COKE_BRICK_ITEM = ITEMS.register("coke_brick",
            () -> new BlockItem(COKE_BRICK.get(), new Item.Properties()));

    // Block Entities
    /*
    public static final Supplier<BlockEntityType<CokeBrickBlockEntity>> COKE_BRICK_BLOCK_ENTITY = BLOCK_ENTITIES.register("coke_brick_block_entity",
            () -> BlockEntityType.Builder.of(CokeBrickBlockEntity::new, COKE_BRICK.get()).build(null));
    */

    // Multiblock Blocks
    public static final DeferredBlock<CokeOvenControllerBlock> COKE_OVEN_CONTROLLER_BLOCK = BLOCKS.registerBlock("coke_oven_controller", CokeOvenControllerBlock::new,
            BlockBehaviour.Properties.of().strength(2.0f).noOcclusion());
    public static final Supplier<BlockEntityType<CokeOvenMultiblockController>> COKE_OVEN_CONTROLLER_BLOCK_ENTITY = BLOCK_ENTITIES.register("coke_oven_controller_block_entity",
            () -> BlockEntityType.Builder.of(CokeOvenMultiblockController::new, COKE_OVEN_CONTROLLER_BLOCK.get()).build(null));
    public static final DeferredBlock<CokeOvenBlock> COKE_OVEN_BLOCK = BLOCKS.registerBlock("coke_oven_block", CokeOvenBlock::new,
            BlockBehaviour.Properties.of().strength(2.0f).noOcclusion());
    public static final Supplier<BlockEntityType<CokeOvenMultiblockDummy>> COKE_OVEN_BLOCK_ENTITY = BLOCK_ENTITIES.register("coke_oven_dummy_block_entity",
            () -> BlockEntityType.Builder.of(CokeOvenMultiblockDummy::new, COKE_OVEN_BLOCK.get()).build(null));


    // menus
    public static final Supplier<MenuType<CokeOvenContainer>> COKE_OVEN_CONTAINER = MENU_TYPES.register("coke_oven_block",
            () -> IMenuTypeExtension.create((windowId, inv, data) -> new CokeOvenContainer(windowId, inv.player, data.readBlockPos())));



    // Items
    public static final DeferredItem<Item> STEEL_INGOT = ITEMS.register("steel_ingot",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<WrenchItem> WRENCH = ITEMS.register("wrench",
            () -> new WrenchItem(new Item.Properties()));

    // Displays for MB
    public static final DeferredItem<Item> BLAST_FURNACE_DISPLAY =
            ITEMS.register("coke_oven_display",
                    () -> new Item(new Item.Properties()));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> INDUSTRIA_TAB = CREATIVE_MODE_TABS.register("industria_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.industria"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> STEEL_INGOT.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(STEEL_INGOT.get());
                output.accept(WRENCH.get());

                output.accept(COKE_BRICK_ITEM.get());
            }
            ).build());

    public static void init(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        ATTACHMENTS.register(modEventBus);
    }
}
