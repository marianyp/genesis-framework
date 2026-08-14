package dev.mariany.genesisframework.compat.rei;

import dev.mariany.genesisframework.client.GenesisFrameworkClient;
import dev.mariany.genesisframework.client.age.requirement.AgeRequirementData;
import dev.mariany.genesisframework.client.age.requirement.view.AgeRequirementView;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class REIAgeRequirementCategory implements DisplayCategory<REIAgeRequirementDisplay> {
    private final AgeRequirementView ageRequirementView;
    private final CategoryIdentifier<REIAgeRequirementDisplay> categoryIdentifier;

    public REIAgeRequirementCategory(
            AgeRequirementView ageRequirementView,
            CategoryIdentifier<REIAgeRequirementDisplay> categoryIdentifier
    ) {
        this.ageRequirementView = ageRequirementView;
        this.categoryIdentifier = categoryIdentifier;
    }

    @Override
    public CategoryIdentifier<? extends REIAgeRequirementDisplay> getCategoryIdentifier() {
        return this.categoryIdentifier;
    }

    @Override
    public Component getTitle() {
        return this.ageRequirementView.title();
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(this.ageRequirementView.icon());
    }

    @Override
    public int getDisplayWidth(REIAgeRequirementDisplay display) {
        return this.ageRequirementView.getWidth(List.of(display.view()), new REIMeasuringContext());
    }

    @Override
    public int getDisplayHeight() {
        return this.ageRequirementView.getHeight(
                GenesisFrameworkClient.getAgeManager().getAgeRequirements(),
                new REIMeasuringContext()
        );
    }

    @Override
    public List<Widget> setupDisplay(REIAgeRequirementDisplay display, Rectangle bounds) {
        AgeRequirementData view = display.view();

        List<Widget> widgets = new ArrayList<>();

        widgets.add(Widgets.createRecipeBase(bounds));

        this.ageRequirementView.draw(
                view,
                new REIDrawingContext(widgets, bounds),
                bounds.width,
                bounds.height
        );

        return widgets;
    }

    private static Point point(Rectangle bounds, int x, int y) {
        return new Point(bounds.x + x, bounds.y + y);
    }

    private record REIDrawingContext(List<Widget> widgets, Rectangle bounds)
            implements AgeRequirementView.DrawingContext {
        @Override
        public void drawItem(int x, int y, ItemStack stack) {
            Slot itemSlot = Widgets.createSlot(point(this.bounds, x, y));
            itemSlot.entry(EntryStacks.of(stack)).markOutput();
            this.widgets.add(itemSlot);
        }

        @Override
        public int getTextWidth(Component text) {
            return Minecraft.getInstance().font.width(text);
        }

        @Override
        public int getTextHeight(Component text) {
            return Minecraft.getInstance().font.lineHeight;
        }

        @Override
        public void drawSprite(int x, int y, int width, int height, Identifier sprite) {
            this.widgets.add(
                    Widgets.createDrawableWidget(
                            (graphics, _, _, _) -> graphics.blitSprite(
                                    RenderPipelines.GUI_TEXTURED,
                                    sprite,
                                    this.bounds.x + x,
                                    this.bounds.y + y,
                                    width,
                                    height
                            )
                    )
            );
        }

        @Override
        public void drawText(int x, int y, Component text, int color) {
            this.widgets.add(
                    Widgets.createLabel(point(this.bounds, x, y), text)
                           .leftAligned()
                           .noShadow()
                           .color(color)
            );
        }
    }

    private static final class REIMeasuringContext implements AgeRequirementView.DrawingContext {
        @Override
        public void drawItem(int x, int y, ItemStack stack) {
            throw new UnsupportedOperationException("Measuring contexts cannot draw items");
        }

        @Override
        public int getTextWidth(Component text) {
            return Minecraft.getInstance().font.width(text);
        }

        @Override
        public int getTextHeight(Component text) {
            return Minecraft.getInstance().font.lineHeight;
        }

        @Override
        public void drawSprite(int x, int y, int width, int height, Identifier sprite) {
            throw new UnsupportedOperationException("Measuring contexts cannot draw sprites");
        }

        @Override
        public void drawText(int x, int y, Component text, int color) {
            throw new UnsupportedOperationException("Measuring contexts cannot draw text");
        }
    }
}
