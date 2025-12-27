package net.andrew.mineciv.client.gui;
import com.mojang.blaze3d.systems.RenderSystem;
import net.andrew.mineciv.block.custom.CannonMenu;
import net.andrew.mineciv.block.entity.custom.CannonBlockEntity;
import net.andrew.mineciv.network.CannonFirePacket;
import net.andrew.mineciv.network.CannonUpdatePacket;
import net.andrew.mineciv.network.ModNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CannonScreen extends AbstractContainerScreen<CannonMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/furnace.png");

    private CannonBlockEntity cannonEntity;
    private float currentPitch;
    private float currentPower;

    public CannonScreen(CannonMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 166;
        this.cannonEntity = menu.getCannonEntity();
        this.currentPitch = cannonEntity.getPitch();
        this.currentPower = cannonEntity.getPower();
    }

    @Override
    protected void init() {
        super.init();

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Fire button
        this.addRenderableWidget(Button.builder(
                        Component.literal("FIRE!"),
                        button -> {
                            ModNetwork.sendToServer(new CannonFirePacket(cannonEntity.getBlockPos()));
                        })
                .bounds(x + 70, y + 55, 40, 20)
                .build());

        // Pitch up button
        this.addRenderableWidget(Button.builder(
                        Component.literal("▲"),
                        button -> adjustPitch(5))
                .bounds(x + 20, y + 55, 20, 20)
                .build());

        // Pitch down button
        this.addRenderableWidget(Button.builder(
                        Component.literal("▼"),
                        button -> adjustPitch(-5))
                .bounds(x + 20, y + 77, 20, 20)
                .build());

        // Power up button
        this.addRenderableWidget(Button.builder(
                        Component.literal("+"),
                        button -> adjustPower(0.1f))
                .bounds(x + 140, y + 55, 20, 20)
                .build());

        // Power down button
        this.addRenderableWidget(Button.builder(
                        Component.literal("-"),
                        button -> adjustPower(-0.1f))
                .bounds(x + 140, y + 77, 20, 20)
                .build());
    }


    private void adjustPitch(float amount) {
        currentPitch = Math.max(0, Math.min(90, currentPitch + amount));
        ModNetwork.sendToServer(new CannonUpdatePacket(
                cannonEntity.getBlockPos(), currentPitch, currentPower));
    }

    private void adjustPower(float amount) {
        currentPower = Math.max(0.5f, Math.min(2.0f, currentPower + amount));
        ModNetwork.sendToServer(new CannonUpdatePacket(
                cannonEntity.getBlockPos(), currentPitch, currentPower));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        // Display current settings
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphics.drawString(this.font,
                "Pitch: " + String.format("%.0f°", currentPitch),
                x + 8, y + 60, 0x404040, false);

        guiGraphics.drawString(this.font,
                "Power: " + String.format("%.1fx", currentPower),
                x + 8, y + 82, 0x404040, false);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        // Don't render inventory label to save space
    }
}