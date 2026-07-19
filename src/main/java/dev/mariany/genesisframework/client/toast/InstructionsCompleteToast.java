package dev.mariany.genesisframework.client.toast;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.sound.GFSoundEvents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.CommonColors;

@Environment(EnvType.CLIENT)
public class InstructionsCompleteToast implements HideableToast {
    private static final Identifier TEXTURE = GenesisFramework.id("toast/instruction");
    private static final Identifier INFORMATION_ICON_TEXTURE = GenesisFramework.id("toast/information");
    private static final Identifier COMPLETE_ICON_TEXTURE = GenesisFramework.id("toast/complete");

    private static final int BAR_COLOR = 0xFF00AA00;

    private static final int INFORMATION_ICON_SIZE = 8;
    private static final int HALF_INFORMATION_ICON_SIZE = INFORMATION_ICON_SIZE / 2;

    private static final int COMPLETE_ICON_SIZE = 16;
    private static final int HALF_COMPLETE_ICON_SIZE = COMPLETE_ICON_SIZE / 2;

    private static final int TITLE_X = COMPLETE_ICON_SIZE * 2;
    private static final int TITLE_Y = 12;

    private static final Component TITLE = Component.translatable("instruction.genesisframework.complete.title");

    private final int displayDuration;
    private Visibility visibility = Visibility.SHOW;

    public InstructionsCompleteToast() {
        this(8000);
    }

    public InstructionsCompleteToast(int displayDuration) {
        this.displayDuration = displayDuration;
    }

    @Override
    public SoundEvent getSoundEvent() {
        return GFSoundEvents.UI_TOAST_INSTRUCTIONS_COMPLETE;
    }

    @Override
    public Visibility getWantedVisibility() {
        return this.visibility;
    }

    @Override
    public void hide() {
        this.visibility = Visibility.HIDE;
    }

    @Override
    public void update(ToastManager manager, long time) {
        if (time >= this.displayDuration) {
            this.visibility = Toast.Visibility.HIDE;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, Font textRenderer, long time) {
        context.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, this.width(), this.height());

        context.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                INFORMATION_ICON_TEXTURE,
                -HALF_INFORMATION_ICON_SIZE,
                1,
                INFORMATION_ICON_SIZE,
                INFORMATION_ICON_SIZE
        );

        context.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                COMPLETE_ICON_TEXTURE,
                HALF_COMPLETE_ICON_SIZE,
                HALF_COMPLETE_ICON_SIZE,
                COMPLETE_ICON_SIZE,
                COMPLETE_ICON_SIZE
        );

        context.text(textRenderer, TITLE, TITLE_X, TITLE_Y, CommonColors.DARK_PURPLE, false);

        this.drawProgressBar(context, time);
    }

    private void drawProgressBar(GuiGraphicsExtractor context, long time) {
        float progress = Math.min((float) time / this.displayDuration, 1);
        int barY = this.height() - 4;
        context.fill(3, barY, 157, barY + 1, CommonColors.WHITE);
        context.fill(3, barY, (int) (3 + 154 * progress), barY + 1, BAR_COLOR);
    }
}
