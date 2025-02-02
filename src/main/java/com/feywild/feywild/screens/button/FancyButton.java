package com.feywild.feywild.screens.button;

import com.feywild.feywild.FeywildMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;
import java.util.function.BooleanSupplier;

public class FancyButton extends Button {

    public static final ResourceLocation TEXTURE_LARGE = FeywildMod.getInstance().resource("textures/gui/quest_button.png");
    public static final ResourceLocation TEXTURE_SMALL = FeywildMod.getInstance().resource("textures/gui/quest_button_small.png");

    private final ResourceLocation texture;
    private final BooleanSupplier enabled;
    private final int width;
    private final int height;

    public FancyButton(ResourceLocation texture, int x, int y, int width, int height, Component message, BooleanSupplier enabled, Button.OnPress onPress) {
        super(x, y, width, height, message, btn -> {
            if (enabled.getAsBoolean()) onPress.onPress(btn);
        });
        this.enabled = enabled;
        this.texture = texture;
        this.width = width;
        this.height = height;
    }

    @Override
    public void renderButton(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (this.enabled.getAsBoolean()) {
            Minecraft minecraft = Minecraft.getInstance();
            Font font = minecraft.font;
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, this.texture);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            this.blit(poseStack, this.x, this.y, 0, 0, this.width, this.height);
            this.renderBg(poseStack, minecraft, mouseX, mouseY);
            drawCenteredString(poseStack, font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, getFGColor() | Mth.ceil(this.alpha * 255) << 24);
            RenderSystem.disableDepthTest();
            RenderSystem.disableBlend();
        }
    }
    
    public static FancyButton makeLarge(int x, int y, Component message, Button.OnPress onPress) {
        return makeLarge(x, y, message, () -> true, onPress);
    }
    
    public static FancyButton makeLarge(int x, int y, Component message, BooleanSupplier enabled, Button.OnPress onPress) {
        return new FancyButton(TEXTURE_LARGE, x, y, 90, 22, message, enabled, onPress);
    }

    public static FancyButton makeSmall(int x, int y, Component message, Button.OnPress onPress) {
        return makeSmall(x, y, message, () -> true, onPress);
    }
    
    public static FancyButton makeSmall(int x, int y, Component message, BooleanSupplier enabled, Button.OnPress onPress) {
        return new FancyButton(TEXTURE_SMALL, x, y, 22, 22, message, enabled, onPress);
    }
}
