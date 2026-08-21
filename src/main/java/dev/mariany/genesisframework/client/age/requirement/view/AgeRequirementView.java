package dev.mariany.genesisframework.client.age.requirement.view;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.age.AgeFormatter;
import dev.mariany.genesisframework.client.age.requirement.AgeRequirementData;
import dev.mariany.genesisframework.item.GFItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.List;

@Environment(EnvType.CLIENT)
public class AgeRequirementView {
    private static final int HEADER_X = 30;
    private static final int HEADER_Y = 8;

    private static final int DESCRIPTION_Y = 20;

    private static final int PANEL_MARGIN = 5;
    private static final int PANEL_Y = 36;
    private static final int PANEL_HORIZONTAL_PADDING = 4;
    private static final int MINIMUM_ROW_HEIGHT = 15;

    private static final int ROW_ICON_WIDTH = 7;
    private static final int ROW_ICON_HEIGHT = 8;
    private static final int ROW_ICON_TEXT_GAP = 4;

    private static final Identifier PANEL_BACKGROUND_SPRITE = GenesisFramework.id("age_requirement/panel");

    private static final Identifier PANEL_PRIMARY_ROW_SPRITE = GenesisFramework.id(
            "age_requirement/primary_row"
    );

    private static final Identifier PANEL_SECONDARY_ROW_SPRITE = GenesisFramework.id(
            "age_requirement/secondary_row"
    );

    private static final Identifier LOCKED_ICON_SPRITE = GenesisFramework.id("age_requirement/x");
    private static final Identifier UNLOCKED_ICON_SPRITE = GenesisFramework.id("age_requirement/checkmark");

    private static final int HEADER_COLOR = CommonColors.DARK_GRAY;
    private static final int SUBHEADER_COLOR = CommonColors.GRAY;

    private final Identifier id;

    private final int minimumWidth;
    private final int minimumHeight;

    private final int itemX;
    private final int itemY;
    private final boolean stretchPanel;

    private final TextPanel textPanel;

    public AgeRequirementView(boolean stretchPanel) {
        this("age_requirements", 156, 57, 6, 10, stretchPanel);
    }

    public AgeRequirementView(
            String id,
            int minimumWidth,
            int minimumHeight,
            int itemX,
            int itemY,
            boolean stretchPanel
    ) {
        this(GenesisFramework.id(id), minimumWidth, minimumHeight, itemX, itemY, stretchPanel);
    }

    public AgeRequirementView(
            Identifier id,
            int width,
            int minimumHeight,
            int itemX,
            int itemY,
            boolean stretchPanel
    ) {
        this.id = id;
        this.minimumWidth = width;
        this.minimumHeight = minimumHeight;
        this.itemX = itemX;
        this.itemY = itemY;
        this.stretchPanel = stretchPanel;

        this.textPanel = new TextPanel(
                PANEL_MARGIN,
                PANEL_Y,
                width - PANEL_MARGIN * 2,
                PANEL_HORIZONTAL_PADDING,
                MINIMUM_ROW_HEIGHT,
                PANEL_BACKGROUND_SPRITE,
                PANEL_PRIMARY_ROW_SPRITE,
                PANEL_SECONDARY_ROW_SPRITE,
                ROW_ICON_WIDTH,
                ROW_ICON_HEIGHT,
                ROW_ICON_TEXT_GAP
        );
    }

    public Identifier id() {
        return this.id;
    }

    public Component title() {
        return Component.translatable("category.genesisframework.age_requirements");
    }

    public Item icon() {
        return GFItems.AGE_BOOK;
    }

    public Component itemName(AgeRequirementData view) {
        return view.stack().getHoverName();
    }

    public Component description() {
        return Component.translatable("gui.genesisframework.age_requirements.description");
    }

    public Component ageName(Identifier ageId) {
        return AgeFormatter.format(ageId);
    }

    public void draw(
            AgeRequirementData data,
            DrawingContext context,
            int availableWidth,
            int availableHeight
    ) {
        TextPanel.Layout panelLayout = this.layoutPanel(data, context, availableWidth, availableHeight);

        context.drawText(HEADER_X, HEADER_Y, this.itemName(data), HEADER_COLOR);
        context.drawText(HEADER_X, DESCRIPTION_Y, this.description(), SUBHEADER_COLOR);
        panelLayout.draw(context);
        this.drawItem(data, context);
    }

    public void drawItem(AgeRequirementData data, DrawingContext context) {
        context.drawItem(this.itemX, this.itemY, data.stack());
    }

    public int getWidth(Collection<AgeRequirementData> data, DrawingContext context) {
        return data.stream()
                   .mapToInt(requirement -> this.getWidth(requirement, context))
                   .max()
                   .orElse(this.minimumWidth);
    }

    public int getHeight(Collection<AgeRequirementData> data, DrawingContext context) {
        return data.stream()
                   .mapToInt(requirement -> this.getHeight(requirement, context))
                   .max()
                   .orElse(this.minimumHeight);
    }

    private int getWidth(AgeRequirementData data, DrawingContext context) {
        TextPanel.Layout panelLayout = this.layoutPanel(data, context);
        return Math.max(this.minimumWidth, panelLayout.right() + PANEL_MARGIN);
    }

    private int getHeight(AgeRequirementData data, DrawingContext context) {
        TextPanel.Layout panelLayout = this.layoutPanel(data, context);
        return Math.max(this.minimumHeight, panelLayout.bottom() + PANEL_MARGIN);
    }

    private TextPanel.Layout layoutPanel(AgeRequirementData data, DrawingContext context) {
        return this.layoutPanel(data, context, this.minimumWidth);
    }

    private TextPanel.Layout layoutPanel(AgeRequirementData data, DrawingContext context, int availableWidth) {
        return this.layoutPanel(data, context, availableWidth, this.minimumHeight);
    }

    private TextPanel.Layout layoutPanel(
            AgeRequirementData data,
            DrawingContext context,
            int availableWidth,
            int availableHeight
    ) {
        List<TextPanel.Row> rows = data
                .requiredAgeIds()
                .stream()
                .map(ageId -> this.createRow(data, ageId))
                .toList();

        int availablePanelWidth = availableWidth - PANEL_MARGIN * 2;

        int availablePanelHeight = this.stretchPanel
                ? availableHeight - PANEL_Y - PANEL_MARGIN
                : 0;

        return this.textPanel.layout(rows, context, availablePanelWidth, availablePanelHeight);
    }

    private TextPanel.Row createRow(AgeRequirementData data, Identifier ageId) {
        boolean unlocked = data.isUnlocked(ageId);
        Identifier iconSprite = unlocked ? UNLOCKED_ICON_SPRITE : LOCKED_ICON_SPRITE;
        return new TextPanel.Row(iconSprite, this.ageName(ageId));
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (!(object instanceof AgeRequirementView other)) {
            return false;
        }

        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public String toString() {
        return "AgeRequirementView[id=" + this.id + ']';
    }

    public interface DrawingContext {
        void drawItem(int x, int y, ItemStack stack);

        int getTextWidth(Component text);

        int getTextHeight(Component text);

        void drawSprite(int x, int y, int width, int height, Identifier sprite);

        void drawText(int x, int y, Component text, int color);
    }

}
