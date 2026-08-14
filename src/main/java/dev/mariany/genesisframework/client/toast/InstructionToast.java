package dev.mariany.genesisframework.client.toast;

import dev.mariany.genesisframework.GenesisFramework;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class InstructionToast implements HideableToast {
    private static final Identifier TEXTURE = GenesisFramework.id("toast/instruction");
    private static final Identifier ICON_TEXTURE = GenesisFramework.id("toast/information");

    private static final int ICON_SIZE = 8;
    private static final int HALF_ICON_SIZE = ICON_SIZE / 2;

    private static final int MAX_TEXT_ROWS = 2;
    private static final int TEXT_COLOR = 0xFF000000;
    private static final int TEXT_LINE_HEIGHT = 11;
    private static final int MAX_WIDTH = 180;
    private static final int MIN_WIDTH = 145;
    private static final int TEXT_X = 30;
    private static final int TOAST_PADDING_RIGHT = 8;
    private static final int TOAST_PADDING_BOTTOM = 3;
    private static final int TOAST_PADDING_TOP = 7;

    private final Font font;
    private final ItemStack icon;
    private final List<Component> components;
    private final List<FormattedCharSequence> text;

    private Toast.Visibility visibility = Toast.Visibility.SHOW;
    private Language language;
    private int width;

    public InstructionToast(
            Font font,
            ItemStackTemplate icon,
            Component title,
            @Nullable Component description
    ) {
        this.font = font;
        this.icon = icon.create();

        this.components = new ArrayList<>(MAX_TEXT_ROWS);
        this.text = new ArrayList<>(MAX_TEXT_ROWS);

        this.components.add(title.copy().withColor(CommonColors.DARK_PURPLE));

        if (description != null) {
            this.components.add(description);
        }

        this.refresh();
    }

    @Override
    public void refresh() {
        HideableToast.super.refresh();
        this.refreshText();
    }

    private void refreshText() {
        if (!this.refreshLanguage()) {
            return;
        }

        this.text.clear();

        int maxTextWidth = MAX_WIDTH - TEXT_X - TOAST_PADDING_RIGHT;

        this.components
                .stream()
                .map(component -> this.font.split(component, maxTextWidth))
                .forEach(this.text::addAll);

        int largestLineWidth = this.text
                .stream()
                .mapToInt(this.font::width)
                .max()
                .orElse(0);

        int desiredWidth = TEXT_X + largestLineWidth + TOAST_PADDING_RIGHT;

        this.width = Math.max(MIN_WIDTH, Math.min(MAX_WIDTH, desiredWidth));
    }

    private boolean refreshLanguage() {
        Language language = Language.getInstance();

        if (Objects.equals(this.language, language)) {
            return false;
        }

        this.language = language;

        return true;
    }

    @Override
    public Toast.Visibility getWantedVisibility() {
        return this.visibility;
    }

    public void hide() {
        this.visibility = Toast.Visibility.HIDE;
    }

    @Override
    public void update(ToastManager manager, long time) {
        this.refreshText();
    }

    @Override
    public int width() {
        return this.width;
    }

    @Override
    public int height() {
        return TOAST_PADDING_TOP + this.getTextHeight() + TOAST_PADDING_BOTTOM;
    }

    private int getTextHeight() {
        return Math.max(this.text.size(), MAX_TEXT_ROWS) * TEXT_LINE_HEIGHT;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, Font textRenderer, long startTime) {
        this.refreshText();

        context.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, this.width(), this.height());

        context.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                ICON_TEXTURE,
                -HALF_ICON_SIZE,
                1,
                ICON_SIZE,
                ICON_SIZE
        );

        int totalTextHeight = this.text.size() * TEXT_LINE_HEIGHT;
        int verticalTextOffset = TOAST_PADDING_TOP + (this.getTextHeight() - totalTextHeight) / 2;

        for (int lineIndex = 0; lineIndex < this.text.size(); lineIndex++) {
            int y = verticalTextOffset + lineIndex * TEXT_LINE_HEIGHT;

            context.text(
                    textRenderer,
                    this.text.get(lineIndex),
                    TEXT_X,
                    y,
                    TEXT_COLOR,
                    false
            );
        }

        context.fakeItem(this.icon, 8, 8);
    }
}
