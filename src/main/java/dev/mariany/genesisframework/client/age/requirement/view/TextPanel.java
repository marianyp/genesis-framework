package dev.mariany.genesisframework.client.age.requirement.view;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;

import java.util.ArrayList;
import java.util.List;

record TextPanel(
        int x,
        int y,
        int minimumWidth,
        int horizontalPadding,
        int minimumRowHeight,
        Identifier backgroundSprite,
        Identifier primaryRowSprite,
        Identifier secondaryRowSprite,
        int rowIconWidth,
        int rowIconHeight,
        int rowIconTextGap
) {
    private static final int PANEL_BORDER_SIZE = 1;
    private static final int TEXT_VERTICAL_OFFSET = 1;

    Layout layout(
            List<Row> rows,
            AgeRequirementView.DrawingContext context,
            int availableWidth,
            int availableHeight
    ) {
        int textWidth = rows.stream()
                            .map(Row::text)
                            .mapToInt(context::getTextWidth)
                            .max()
                            .orElse(0);

        int textHeight = rows.stream()
                             .map(Row::text)
                             .mapToInt(context::getTextHeight)
                             .max()
                             .orElse(0);

        int rowHeight = Math.max(this.minimumRowHeight, Math.max(textHeight, this.rowIconHeight));

        int rowContentWidth = this.rowIconWidth + this.rowIconTextGap + textWidth;
        int paddedContentWidth = rowContentWidth + this.horizontalPadding * 2 + PANEL_BORDER_SIZE * 2;
        int width = Math.max(Math.max(this.minimumWidth, availableWidth), paddedContentWidth);
        int rowsHeight = rowHeight * rows.size();
        int height = Math.max(availableHeight, rowsHeight + PANEL_BORDER_SIZE * 2);

        List<TextRow> textRows = new ArrayList<>(rows.size());

        for (int index = 0; index < rows.size(); index++) {
            Row row = rows.get(index);
            Component rowText = row.text();
            int measuredHeight = context.getTextHeight(rowText);
            int rowY = this.y + PANEL_BORDER_SIZE + index * rowHeight;
            int visualTextHeight = Math.max(1, measuredHeight - 1);
            int textY = rowY + Math.floorDiv(rowHeight - visualTextHeight, 2) + TEXT_VERTICAL_OFFSET;
            int iconX = this.x + PANEL_BORDER_SIZE + this.horizontalPadding;
            int iconY = textY + Math.ceilDiv(visualTextHeight - this.rowIconHeight, 2);
            int textX = iconX + this.rowIconWidth + this.rowIconTextGap;

            Identifier backgroundSprite = index % 2 == 0
                    ? this.primaryRowSprite
                    : this.secondaryRowSprite;

            textRows.add(
                    new TextRow(
                            rowY,
                            rowHeight,
                            iconX,
                            iconY,
                            textX,
                            textY,
                            row.iconSprite(),
                            rowText,
                            backgroundSprite
                    )
            );
        }

        return new Layout(this, width, height, List.copyOf(textRows));
    }

    record Row(Identifier iconSprite, Component text) {
    }

    record Layout(TextPanel panel, int width, int height, List<TextRow> rows) {
        int right() {
            return this.panel.x + this.width;
        }

        int bottom() {
            return this.panel.y + this.height;
        }

        void draw(AgeRequirementView.DrawingContext context) {
            context.drawSprite(this.panel.x, this.panel.y, this.width, this.height, this.panel.backgroundSprite);
            this.drawRows(context);
        }

        private void drawRows(AgeRequirementView.DrawingContext context) {
            this.drawRowBackgrounds(context);
            this.drawRowIcons(context);
            this.drawRowText(context);
        }

        private void drawRowBackgrounds(AgeRequirementView.DrawingContext context) {
            int rowX = this.panel.x + PANEL_BORDER_SIZE;
            int rowWidth = this.width - PANEL_BORDER_SIZE * 2;
            int maximumRowBottom = this.panel.y + this.height - PANEL_BORDER_SIZE;

            for (TextRow row : this.rows) {
                int rowHeight = Math.min(row.height, maximumRowBottom - row.y);

                if (rowWidth <= 0 || rowHeight <= 0) {
                    continue;
                }

                context.drawSprite(rowX, row.y, rowWidth, rowHeight, row.backgroundSprite);
            }
        }

        private void drawRowIcons(AgeRequirementView.DrawingContext context) {
            this.rows.forEach(row -> context.drawSprite(
                    row.iconX,
                    row.iconY,
                    this.panel.rowIconWidth,
                    this.panel.rowIconHeight,
                    row.iconSprite
            ));
        }

        private void drawRowText(AgeRequirementView.DrawingContext context) {
            this.rows.forEach(row -> context.drawText(
                    row.textX,
                    row.textY,
                    row.text,
                    CommonColors.WHITE
            ));
        }
    }

    private record TextRow(
            int y,
            int height,
            int iconX,
            int iconY,
            int textX,
            int textY,
            Identifier iconSprite,
            Component text,
            Identifier backgroundSprite
    ) {
    }
}
