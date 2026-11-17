package net.industria.industria.client.screen;

import net.industria.industria.Industria;
import net.industria.industria.common.container.CokeOvenContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CokeOvenScreen extends AbstractContainerScreen<CokeOvenContainer> {
    private final ResourceLocation GUI = ResourceLocation.fromNamespaceAndPath(Industria.MODID, "textures/gui/coke_oven_gui.png");

    public CokeOvenScreen(CokeOvenContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 166;
        this.imageWidth = 176;
        this.inventoryLabelY = this.imageHeight - 96;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(GUI, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mousex, int mousey, float partialTick) {
        super.render(graphics, mousex, mousey, partialTick);
        this.renderTooltip(graphics, mousex, mousey);
    }
}
