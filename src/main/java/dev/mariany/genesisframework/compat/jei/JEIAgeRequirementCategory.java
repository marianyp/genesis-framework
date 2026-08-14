package dev.mariany.genesisframework.compat.jei;

import dev.mariany.genesisframework.client.GenesisFrameworkClient;
import dev.mariany.genesisframework.client.age.requirement.AgeRequirementData;
import dev.mariany.genesisframework.client.age.requirement.view.AgeRequirementView;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class JEIAgeRequirementCategory implements IRecipeCategory<AgeRequirementData> {
    private final AgeRequirementView ageRequirementView;
    private final IRecipeType<AgeRequirementData> recipeType;
    private final IDrawable icon;

    public JEIAgeRequirementCategory(
            IGuiHelper guiHelper,
            IRecipeType<AgeRequirementData> recipeType,
            AgeRequirementView ageRequirementView
    ) {
        this.ageRequirementView = ageRequirementView;
        this.recipeType = recipeType;
        this.icon = guiHelper.createDrawableItemLike(ageRequirementView.icon());
    }

    @Override
    public IRecipeType<AgeRequirementData> getRecipeType() {
        return this.recipeType;
    }

    @Override
    public Component getTitle() {
        return this.ageRequirementView.title();
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public int getWidth() {
        return this.ageRequirementView.getWidth(
                GenesisFrameworkClient.getAgeManager().getAgeRequirements(),
                new JEIMeasuringContext(Minecraft.getInstance().font)
        );
    }

    @Override
    public int getHeight() {
        return this.ageRequirementView.getHeight(
                GenesisFrameworkClient.getAgeManager().getAgeRequirements(),
                new JEIMeasuringContext(Minecraft.getInstance().font)
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AgeRequirementData view, IFocusGroup focuses) {
        this.ageRequirementView.drawItem(view, new JEIRecipeDrawingContext(builder));
    }

    @Override
    public void draw(
            AgeRequirementData view,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphicsExtractor graphics,
            double mouseX,
            double mouseY
    ) {
        this.ageRequirementView.draw(
                view,
                new JEIDrawingContext(graphics, Minecraft.getInstance().font),
                this.getWidth(),
                this.getHeight()
        );
    }

    @Override
    public Identifier getIdentifier(AgeRequirementData view) {
        Identifier categoryId = this.ageRequirementView.id();
        Identifier itemId = BuiltInRegistries.ITEM.getKey(view.stack().getItem());

        String namespace = categoryId.getNamespace();
        String path = categoryId.getPath() + "/" + itemId.getNamespace() + "/" + itemId.getPath();

        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    private record JEIDrawingContext(GuiGraphicsExtractor graphics, Font font)
            implements AgeRequirementView.DrawingContext {
        @Override
        public void drawItem(int x, int y, ItemStack stack) {
        }

        @Override
        public int getTextWidth(Component text) {
            return this.font.width(text);
        }

        @Override
        public int getTextHeight(Component text) {
            return this.font.lineHeight;
        }

        @Override
        public void drawSprite(int x, int y, int width, int height, Identifier sprite) {
            this.graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height);
        }

        @Override
        public void drawText(int x, int y, Component text, int color) {
            this.graphics.text(this.font, text, x, y, color, false);
        }
    }

    private record JEIRecipeDrawingContext(IRecipeLayoutBuilder builder) implements AgeRequirementView.DrawingContext {
        @Override
        public void drawItem(int x, int y, ItemStack stack) {
            this.builder.addOutputSlot(x, y).setStandardSlotBackground().add(stack);
        }

        @Override
        public int getTextWidth(Component text) {
            throw new UnsupportedOperationException("Recipe layout contexts cannot measure text");
        }

        @Override
        public int getTextHeight(Component text) {
            throw new UnsupportedOperationException("Recipe layout contexts cannot measure text");
        }

        @Override
        public void drawSprite(int x, int y, int width, int height, Identifier sprite) {
            throw new UnsupportedOperationException("Recipe layout contexts cannot draw sprites");
        }

        @Override
        public void drawText(int x, int y, Component text, int color) {
            throw new UnsupportedOperationException("Recipe layout contexts cannot draw text");
        }
    }

    private record JEIMeasuringContext(Font font) implements AgeRequirementView.DrawingContext {
        @Override
        public void drawItem(int x, int y, ItemStack stack) {
            throw new UnsupportedOperationException("Measuring contexts cannot draw items");
        }

        @Override
        public int getTextWidth(Component text) {
            return this.font.width(text);
        }

        @Override
        public int getTextHeight(Component text) {
            return this.font.lineHeight;
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
