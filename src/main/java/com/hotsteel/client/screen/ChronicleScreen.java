package com.hotsteel.client.screen;

import java.util.List;

import com.hotsteel.lore.Chronicle;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * 「热钢手册」阅读界面 — a two-pane chronicle viewer: the chapter list on the
 * left, the selected chapter's prose on the right. Scrolling moves the body
 * text; ESC or a click outside closes it.
 */
public class ChronicleScreen extends Screen {

    private static final int OUTER_MARGIN = 18;
    private static final int LIST_WIDTH = 132;
    private static final int LINE_HEIGHT = 11;
    private static final int TITLE_COLOR = 0xFFB020;
    private static final int SELECTED_COLOR = 0xFFD060;
    private static final int IDLE_COLOR = 0xA0A0A0;
    private static final int BODY_COLOR = 0xD8D8D8;
    private static final int PANEL_BG = 0xC8100A04;
    private static final int PANEL_BORDER = 0xFFB020;

    private int selected = 0;
    private double scroll = 0.0;

    public ChronicleScreen() {
        super(Component.translatable("gui.hotsteel.codex.title"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int panelTop() {
        return OUTER_MARGIN + 24;
    }

    private int panelBottom() {
        return this.height - OUTER_MARGIN;
    }

    private int bodyLeft() {
        return OUTER_MARGIN + LIST_WIDTH + 12;
    }

    private int bodyRight() {
        return this.width - OUTER_MARGIN - 12;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        Font font = this.font;
        int top = panelTop();
        int bottom = panelBottom();

        graphics.drawCenteredString(font, this.title, this.width / 2, OUTER_MARGIN, TITLE_COLOR);

        // --- Left pane: chapter list ---
        graphics.fill(OUTER_MARGIN, top, OUTER_MARGIN + LIST_WIDTH, bottom, PANEL_BG);
        graphics.renderOutline(OUTER_MARGIN, top, LIST_WIDTH, bottom - top, PANEL_BORDER);

        List<Chronicle.Chapter> chapters = Chronicle.CHAPTERS;
        for (int i = 0; i < chapters.size(); i++) {
            int y = top + 8 + i * (LINE_HEIGHT + 6);
            boolean hovered = mouseX >= OUTER_MARGIN + 4 && mouseX <= OUTER_MARGIN + LIST_WIDTH - 4
                && mouseY >= y - 3 && mouseY <= y + LINE_HEIGHT;
            int color = i == this.selected ? SELECTED_COLOR : (hovered ? 0xFFFFFF : IDLE_COLOR);
            String label = (i == this.selected ? "▶ " : "  ")
                + Component.translatable(chapters.get(i).titleKey()).getString();
            graphics.drawString(font, label, OUTER_MARGIN + 8, y, color, false);
        }

        // --- Right pane: chapter body ---
        int bodyTop = top + 8;
        graphics.fill(bodyLeft() - 6, top, bodyRight() + 6, bottom, PANEL_BG);
        graphics.renderOutline(bodyLeft() - 6, top, bodyRight() - bodyLeft() + 12, bottom - top,
            PANEL_BORDER);

        Chronicle.Chapter chapter = chapters.get(this.selected);
        graphics.drawString(font, Component.translatable(chapter.titleKey()),
            bodyLeft(), bodyTop, SELECTED_COLOR, false);

        int textTop = bodyTop + LINE_HEIGHT + 6;
        List<net.minecraft.util.FormattedCharSequence> lines = font.split(
            Component.translatable(chapter.bodyKey()), bodyRight() - bodyLeft());
        int visibleHeight = bottom - textTop - 8;
        int totalHeight = lines.size() * LINE_HEIGHT;
        double maxScroll = Math.max(0, totalHeight - visibleHeight);
        this.scroll = Math.max(0.0, Math.min(this.scroll, maxScroll));

        int scissorBottom = bottom - 4;
        graphics.enableScissor(bodyLeft() - 4, textTop - 2, bodyRight() + 4, scissorBottom);
        int offset = (int) this.scroll;
        for (int i = 0; i < lines.size(); i++) {
            int y = textTop + i * LINE_HEIGHT - offset;
            if (y < textTop - LINE_HEIGHT || y > scissorBottom) {
                continue;
            }
            graphics.drawString(font, lines.get(i), bodyLeft(), y, BODY_COLOR, false);
        }
        graphics.disableScissor();

        if (maxScroll > 0) {
            String hint = Component.translatable("gui.hotsteel.codex.scroll_hint").getString();
            graphics.drawString(font, hint, bodyLeft(), bottom - 12, IDLE_COLOR, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int top = panelTop();
        if (mouseX >= OUTER_MARGIN + 4 && mouseX <= OUTER_MARGIN + LIST_WIDTH - 4) {
            for (int i = 0; i < Chronicle.CHAPTERS.size(); i++) {
                int y = top + 8 + i * (LINE_HEIGHT + 6);
                if (mouseY >= y - 3 && mouseY <= y + LINE_HEIGHT) {
                    this.selected = i;
                    this.scroll = 0.0;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.scroll -= scrollY * LINE_HEIGHT;
        return true;
    }
}
