/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package baritone.utils.gui;

import baritone.Baritone;
import baritone.api.Settings;
import baritone.api.command.ICommand;
import baritone.api.pathing.goals.Goal;
import baritone.api.process.IBaritoneProcess;
import baritone.api.utils.Helper;
import baritone.api.utils.SettingsUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class HypervisionMenuScreen extends Screen implements Helper {

    private static final SessionState SESSION = new SessionState();

    private static final int COLOR_BACKGROUND_TOP = 0xE012171E;
    private static final int COLOR_BACKGROUND_BOTTOM = 0xE0151A22;
    private static final int COLOR_PANEL = 0xEE1B232E;
    private static final int COLOR_PANEL_ALT = 0xEE202A36;
    private static final int COLOR_PANEL_SOFT = 0xCC141B23;
    private static final int COLOR_BORDER = 0xFF344150;
    private static final int COLOR_ACCENT = 0xFFC62828;
    private static final int COLOR_ACCENT_SOFT = 0x992E0F12;
    private static final int COLOR_TEXT = 0xFFF3F4F6;
    private static final int COLOR_TEXT_MUTED = 0xFFB9C0CA;
    private static final int COLOR_TEXT_SOFT = 0xFF7B8694;
    private static final int COLOR_SUCCESS = 0xFFD1D5DB;
    private static final int COLOR_LIST_SELECTED = 0xFF303B49;
    private static final int COLOR_BUTTON = 0xFF1A222C;
    private static final int COLOR_BUTTON_HOVER = 0xFF273342;

    private static final int BUTTON_HEIGHT = 28;
    private static final int NAV_BUTTON_HEIGHT = 30;
    private static final int LIST_ROW_HEIGHT = 24;
    private static final int FIELD_HEIGHT = 22;
    private static final int ACTION_BUTTON_HEIGHT = 22;
    private static final int ACTION_FIELD_HEIGHT = 18;

    private final Baritone baritone;
    private final List<ICommand> commands;

    private Section section = Section.DASHBOARD;
    private ActionPane actionPane = ActionPane.GOTO;
    private FollowMode followMode = FollowMode.PLAYERS;

    private Settings.Setting<?> selectedSetting;
    private ICommand selectedCommand;
    private int settingsScroll;
    private int commandScroll;

    private String gotoX = "";
    private String gotoY = "";
    private String gotoZ = "";
    private String mineTarget = "diamond_ore";
    private String mineCount = "";
    private String followTarget = "";
    private String buildFile = "";
    private String buildX = "";
    private String buildY = "";
    private String buildZ = "";
    private String settingsSearch = "";
    private String settingsDraftValue = "";
    private String commandSearch = "";
    private String commandArgs = "";
    private String bannerMessage = "Press Esc or use the Hypervision keybind to close the menu";
    private long bannerDeadline;

    private EditBox gotoXField;
    private EditBox gotoYField;
    private EditBox gotoZField;
    private EditBox mineTargetField;
    private EditBox mineCountField;
    private EditBox followTargetField;
    private EditBox buildFileField;
    private EditBox buildXField;
    private EditBox buildYField;
    private EditBox buildZField;
    private EditBox settingsSearchField;
    private EditBox settingsValueField;
    private EditBox commandSearchField;
    private EditBox commandArgsField;

    public HypervisionMenuScreen(Baritone baritone) {
        super(Component.literal("Hypervision"));
        this.baritone = baritone;
        this.commands = baritone.getCommandManager().getRegistry().entries.stream()
                .filter(command -> !command.hiddenFromHelp())
                .sorted(Comparator.comparing(command -> command.getNames().get(0)))
                .collect(Collectors.toCollection(ArrayList::new));
        this.section = SESSION.section;
        this.actionPane = SESSION.actionPane;
        this.followMode = SESSION.followMode;
        this.gotoX = SESSION.gotoX;
        this.gotoY = SESSION.gotoY;
        this.gotoZ = SESSION.gotoZ;
        this.mineTarget = SESSION.mineTarget;
        this.mineCount = SESSION.mineCount;
        this.followTarget = SESSION.followTarget;
        this.buildFile = SESSION.buildFile;
        this.buildX = SESSION.buildX;
        this.buildY = SESSION.buildY;
        this.buildZ = SESSION.buildZ;
        this.selectedCommand = this.findCommand("goto");
    }

    @Override
    protected void init() {
        clearWidgets();
        rebuildWidgets();
    }

    @Override
    protected void rebuildWidgets() {
        clearWidgets();
        synchronizeSelections();
        gotoXField = null;
        gotoYField = null;
        gotoZField = null;
        mineTargetField = null;
        mineCountField = null;
        followTargetField = null;
        buildFileField = null;
        buildXField = null;
        buildYField = null;
        buildZField = null;
        settingsSearchField = null;
        settingsValueField = null;
        commandSearchField = null;
        commandArgsField = null;

        Layout layout = layout();
        if (section == Section.ACTIONS) {
            addActionFields(layout);
            return;
        }
        if (section == Section.SETTINGS) {
            Rect list = settingsListRect(layout);
            Rect detail = settingsDetailRect(layout);
            settingsSearchField = addField(insetTop(list, 16, 28, list.width() - 32, FIELD_HEIGHT), settingsSearch, value -> {
                settingsSearch = value;
                settingsScroll = 0;
                synchronizeSelections();
            });
            if (selectedSetting != null && selectedSetting.getValueClass() != Boolean.class) {
                Rect valueRect = new Rect(detail.x() + 18, detail.bottom() - 92, Math.max(180, detail.width() - 150), FIELD_HEIGHT);
                settingsValueField = addField(valueRect, settingsDraftValue, value -> settingsDraftValue = value);
            }
            return;
        }
        if (section == Section.CONSOLE) {
            Rect list = consoleListRect(layout);
            Rect detail = consoleDetailRect(layout);
            commandSearchField = addField(insetTop(list, 16, 28, list.width() - 32, FIELD_HEIGHT), commandSearch, value -> {
                commandSearch = value;
                commandScroll = 0;
                synchronizeSelections();
            });
            Rect argsRect = new Rect(detail.x() + 18, detail.y() + 188, Math.max(180, detail.width() - 164), FIELD_HEIGHT);
            commandArgsField = addField(argsRect, commandArgs, value -> commandArgs = value);
        }
    }

    private void addActionFields(Layout layout) {
        Rect content = actionsContentRect(layout);
        int contentX = content.x();
        int contentWidth = content.width();
        int fieldY = content.y();
        int gap = 8;
        switch (actionPane) {
            case GOTO -> {
                int fieldTop = fieldY + 20;
                int colWidth = Math.max(60, (contentWidth - gap * 2) / 3);
                gotoXField = addField(new Rect(contentX, fieldTop, colWidth, ACTION_FIELD_HEIGHT), gotoX, value -> {
                    gotoX = value;
                    SESSION.gotoX = value;
                });
                gotoYField = addField(new Rect(contentX + colWidth + gap, fieldTop, colWidth, ACTION_FIELD_HEIGHT), gotoY, value -> {
                    gotoY = value;
                    SESSION.gotoY = value;
                });
                gotoZField = addField(new Rect(contentX + (colWidth + gap) * 2, fieldTop, colWidth, ACTION_FIELD_HEIGHT), gotoZ, value -> {
                    gotoZ = value;
                    SESSION.gotoZ = value;
                });
            }
            case MINE -> {
                int countWidth = Math.min(88, Math.max(68, contentWidth / 4));
                mineTargetField = addField(new Rect(contentX, fieldY + 16, contentWidth - countWidth - gap, ACTION_FIELD_HEIGHT), mineTarget, value -> {
                    mineTarget = value;
                    SESSION.mineTarget = value;
                });
                mineCountField = addField(new Rect(contentX + contentWidth - countWidth, fieldY + 16, countWidth, ACTION_FIELD_HEIGHT), mineCount, value -> {
                    mineCount = value;
                    SESSION.mineCount = value;
                });
            }
            case FOLLOW -> followTargetField = addField(new Rect(contentX, fieldY + 44, contentWidth, ACTION_FIELD_HEIGHT), followTarget, value -> {
                followTarget = value;
                SESSION.followTarget = value;
            });
            case BUILD -> {
                buildFileField = addField(new Rect(contentX, fieldY + 16, contentWidth, ACTION_FIELD_HEIGHT), buildFile, value -> {
                    buildFile = value;
                    SESSION.buildFile = value;
                });
                int originY = fieldY + 54;
                int colWidth = Math.max(60, (contentWidth - gap * 2) / 3);
                buildXField = addField(new Rect(contentX, originY + 16, colWidth, ACTION_FIELD_HEIGHT), buildX, value -> {
                    buildX = value;
                    SESSION.buildX = value;
                });
                buildYField = addField(new Rect(contentX + colWidth + gap, originY + 16, colWidth, ACTION_FIELD_HEIGHT), buildY, value -> {
                    buildY = value;
                    SESSION.buildY = value;
                });
                buildZField = addField(new Rect(contentX + (colWidth + gap) * 2, originY + 16, colWidth, ACTION_FIELD_HEIGHT), buildZ, value -> {
                    buildZ = value;
                    SESSION.buildZ = value;
                });
            }
            case UTILITY -> {
            }
        }
    }

    private EditBox addField(Rect rect, String initialValue, java.util.function.Consumer<String> responder) {
        EditBox field = new EditBox(font, rect.x(), rect.y(), rect.width(), rect.height(), Component.empty());
        field.setValue(initialValue);
        field.setBordered(false);
        field.setTextColor(COLOR_TEXT);
        field.setTextColorUneditable(COLOR_TEXT_MUTED);
        field.setResponder(responder);
        addRenderableWidget(field);
        return field;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        synchronizeSelections();
        Layout layout = layout();

        guiGraphics.fillGradient(0, 0, width, height, COLOR_BACKGROUND_TOP, COLOR_BACKGROUND_BOTTOM);
        drawPanel(guiGraphics, layout.topBar(), false);
        drawPanel(guiGraphics, layout.sidebar(), true);
        drawPanel(guiGraphics, layout.main(), false);

        drawTopBar(guiGraphics, layout);
        drawSidebar(guiGraphics, layout, mouseX, mouseY);

        switch (section) {
            case DASHBOARD -> renderDashboard(guiGraphics, layout, mouseX, mouseY);
            case ACTIONS -> renderActions(guiGraphics, layout, mouseX, mouseY);
            case SETTINGS -> renderSettings(guiGraphics, layout, mouseX, mouseY);
            case CONSOLE -> renderConsole(guiGraphics, layout, mouseX, mouseY);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        drawBanner(guiGraphics, layout);
    }

    private void drawTopBar(GuiGraphics guiGraphics, Layout layout) {
        Rect topBar = layout.topBar();
        guiGraphics.drawString(font, "Hypervision", topBar.x() + 18, topBar.y() + 14, COLOR_TEXT);
        guiGraphics.drawString(font, "Command menu", topBar.x() + 18, topBar.y() + 30, COLOR_TEXT_MUTED);

        String status = "Status: " + currentPathState();
        guiGraphics.drawString(font, status, topBar.right() - font.width(status) - 18, topBar.y() + 14, COLOR_TEXT);

        String keyHint = "Controls > Hypervision";
        guiGraphics.drawString(font, keyHint, topBar.right() - font.width(keyHint) - 18, topBar.y() + 30, COLOR_ACCENT);
    }

    private void drawSidebar(GuiGraphics guiGraphics, Layout layout, int mouseX, int mouseY) {
        Rect sidebar = layout.sidebar();
        int y = sidebar.y() + 18;
        for (Section value : Section.values()) {
            Rect button = new Rect(sidebar.x() + 12, y, sidebar.width() - 24, NAV_BUTTON_HEIGHT);
            drawSidebarButton(guiGraphics, button, value.label, isInside(mouseX, mouseY, button), value == section);
            y += NAV_BUTTON_HEIGHT + 8;
        }
    }

    private void renderDashboard(GuiGraphics guiGraphics, Layout layout, int mouseX, int mouseY) {
        Rect main = innerMain(layout);
        guiGraphics.drawString(font, "Dashboard", main.x(), main.y(), COLOR_TEXT);
        guiGraphics.drawString(font, "Core actions without chat.", main.x(), main.y() + 18, COLOR_TEXT_MUTED);

        Rect quick = new Rect(main.x(), main.y() + 42, main.width(), 154);
        Rect context = new Rect(main.x(), quick.bottom() + 14, main.width(), Math.max(120, main.bottom() - quick.bottom() - 14));
        drawCard(guiGraphics, quick, "Quick actions");
        drawCard(guiGraphics, context, "Current context");

        int gap = 10;
        int halfWidth = (quick.width() - 50 - gap) / 2;
        Rect openActions = new Rect(quick.x() + 18, quick.y() + 34, halfWidth, BUTTON_HEIGHT);
        Rect picker = new Rect(openActions.right() + gap, quick.y() + 34, halfWidth, BUTTON_HEIGHT);
        Rect pause = new Rect(quick.x() + 18, quick.y() + 72, halfWidth, BUTTON_HEIGHT);
        Rect resume = new Rect(pause.right() + gap, quick.y() + 72, halfWidth, BUTTON_HEIGHT);
        Rect cancel = new Rect(quick.x() + 18, quick.y() + 110, quick.width() - 36, BUTTON_HEIGHT);

        drawButton(guiGraphics, openActions, "Open actions", isInside(mouseX, mouseY, openActions), false);
        drawButton(guiGraphics, picker, "Selection picker", isInside(mouseX, mouseY, picker), false);
        drawButton(guiGraphics, pause, "Pause", isInside(mouseX, mouseY, pause), false);
        drawButton(guiGraphics, resume, "Resume", isInside(mouseX, mouseY, resume), false);
        drawButton(guiGraphics, cancel, "Cancel", isInside(mouseX, mouseY, cancel), false);

        int y = context.y() + 34;
        drawKeyValue(guiGraphics, context.x() + 18, y, "Player", formatBlock(baritone.getPlayerContext().playerFeet()), context.width() - 36);
        drawKeyValue(guiGraphics, context.x() + 18, y + 22, "Goal", currentGoalText(), context.width() - 36);
        drawKeyValue(guiGraphics, context.x() + 18, y + 44, "Control", currentProcessText(), context.width() - 36);
        drawKeyValue(guiGraphics, context.x() + 18, y + 66, "ETA", currentEtaText(), context.width() - 36);
        drawKeyValue(guiGraphics, context.x() + 18, y + 88, "Path", currentPathState(), context.width() - 36);
    }

    private void renderActions(GuiGraphics guiGraphics, Layout layout, int mouseX, int mouseY) {
        Rect form = actionsFormRect(layout);
        drawCard(guiGraphics, form, "Actions");
        guiGraphics.drawString(font, "Task", form.x() + 18, form.y() + 34, COLOR_TEXT_MUTED);
        Rect selector = actionSelectorLabelRect(form);
        Rect previous = actionSelectorPreviousRect(form);
        Rect next = actionSelectorNextRect(form);
        drawButton(guiGraphics, previous, "<", isInside(mouseX, mouseY, previous), false);
        drawButton(guiGraphics, selector, actionPane.label, isInside(mouseX, mouseY, selector), true);
        drawButton(guiGraphics, next, ">", isInside(mouseX, mouseY, next), false);

        switch (actionPane) {
            case GOTO -> renderGotoPane(guiGraphics, form, mouseX, mouseY);
            case MINE -> renderMinePane(guiGraphics, form, mouseX, mouseY);
            case FOLLOW -> renderFollowPane(guiGraphics, form, mouseX, mouseY);
            case BUILD -> renderBuildPane(guiGraphics, form, mouseX, mouseY);
            case UTILITY -> renderUtilityPane(guiGraphics, form, mouseX, mouseY);
        }
    }

    private void renderSettings(GuiGraphics guiGraphics, Layout layout, int mouseX, int mouseY) {
        Rect main = innerMain(layout);
        Rect list = settingsListRect(layout);
        Rect detail = settingsDetailRect(layout);
        guiGraphics.drawString(font, "Settings", main.x(), main.y(), COLOR_TEXT);
        guiGraphics.drawString(font, "Search, inspect, and edit runtime settings.", main.x(), main.y() + 18, COLOR_TEXT_MUTED);

        drawCard(guiGraphics, list, "Settings");
        drawCard(guiGraphics, detail, selectedSetting == null ? "Detail" : selectedSetting.getName());
        drawFieldBackplate(guiGraphics, insetTop(list, 16, 28, list.width() - 32, FIELD_HEIGHT));

        List<Settings.Setting<?>> filteredSettings = filteredSettings();
        int rows = settingsVisibleRows(layout);
        int baseY = list.y() + 64;
        for (int index = 0; index < rows; index++) {
            int settingIndex = settingsScroll + index;
            if (settingIndex >= filteredSettings.size()) {
                break;
            }
            Settings.Setting<?> setting = filteredSettings.get(settingIndex);
            Rect row = new Rect(list.x() + 12, baseY + index * LIST_ROW_HEIGHT, list.width() - 24, 20);
            boolean selected = Objects.equals(setting, selectedSetting);
            int background = selected ? COLOR_LIST_SELECTED : isInside(mouseX, mouseY, row) ? COLOR_PANEL_SOFT : 0;
            if (background != 0) {
                guiGraphics.fill(row.x(), row.y(), row.right(), row.bottom(), background);
            }
            guiGraphics.drawString(font, trimToWidth(setting.getName(), row.width() - 50), row.x() + 10, row.y() + 6, selected ? COLOR_TEXT : COLOR_TEXT_MUTED);
            if (setting.value != setting.defaultValue) {
                guiGraphics.drawString(font, "MOD", row.right() - 26, row.y() + 6, COLOR_ACCENT);
            }
        }

        if (selectedSetting == null) {
            guiGraphics.drawString(font, "No setting selected", detail.x() + 18, detail.y() + 34, COLOR_TEXT_MUTED);
            return;
        }

        int detailY = detail.y() + 34;
        guiGraphics.drawString(font, "Type", detail.x() + 18, detailY, COLOR_TEXT_SOFT);
        guiGraphics.drawString(font, SettingsUtil.settingTypeToString(selectedSetting), detail.x() + 18, detailY + 16, COLOR_TEXT);
        guiGraphics.drawString(font, "Current", detail.x() + 18, detailY + 42, COLOR_TEXT_SOFT);
        detailY = drawWrappedText(guiGraphics, safeSettingValue(selectedSetting), detail.x() + 18, detailY + 58, detail.width() - 36, COLOR_TEXT, 3);
        guiGraphics.drawString(font, "Default", detail.x() + 18, detailY + 10, COLOR_TEXT_SOFT);
        detailY = drawWrappedText(guiGraphics, safeDefaultValue(selectedSetting), detail.x() + 18, detailY + 26, detail.width() - 36, COLOR_TEXT_MUTED, 3);

        if (selectedSetting.getValueClass() == Boolean.class) {
            Rect toggleRect = new Rect(detail.x() + 18, detail.bottom() - 92, 150, BUTTON_HEIGHT);
            drawButton(guiGraphics, toggleRect, Boolean.TRUE.equals(selectedSetting.value) ? "Disable" : "Enable", isInside(mouseX, mouseY, toggleRect), Boolean.TRUE.equals(selectedSetting.value));
        } else {
            Rect valueRect = new Rect(detail.x() + 18, detail.bottom() - 92, Math.max(180, detail.width() - 150), FIELD_HEIGHT);
            Rect applyRect = new Rect(valueRect.right() + 10, detail.bottom() - 96, 104, BUTTON_HEIGHT);
            guiGraphics.drawString(font, "Edit value", detail.x() + 18, valueRect.y() - 18, COLOR_TEXT_SOFT);
            drawFieldBackplate(guiGraphics, valueRect);
            drawButton(guiGraphics, applyRect, "Apply", isInside(mouseX, mouseY, applyRect), false);
        }

        Rect resetRect = new Rect(detail.x() + 18, detail.bottom() - 52, 150, BUTTON_HEIGHT);
        drawButton(guiGraphics, resetRect, "Reset setting", isInside(mouseX, mouseY, resetRect), false);
    }

    private void renderConsole(GuiGraphics guiGraphics, Layout layout, int mouseX, int mouseY) {
        Rect main = innerMain(layout);
        Rect list = consoleListRect(layout);
        Rect detail = consoleDetailRect(layout);
        guiGraphics.drawString(font, "Console", main.x(), main.y(), COLOR_TEXT);
        guiGraphics.drawString(font, "Search commands and run them without touching chat.", main.x(), main.y() + 18, COLOR_TEXT_MUTED);

        drawCard(guiGraphics, list, "Commands");
        drawCard(guiGraphics, detail, selectedCommand == null ? "Detail" : selectedCommand.getNames().get(0));
        drawFieldBackplate(guiGraphics, insetTop(list, 16, 28, list.width() - 32, FIELD_HEIGHT));

        List<ICommand> filteredCommands = filteredCommands();
        int rows = consoleVisibleRows(layout);
        int baseY = list.y() + 64;
        for (int index = 0; index < rows; index++) {
            int commandIndex = commandScroll + index;
            if (commandIndex >= filteredCommands.size()) {
                break;
            }
            ICommand command = filteredCommands.get(commandIndex);
            Rect row = new Rect(list.x() + 12, baseY + index * LIST_ROW_HEIGHT, list.width() - 24, 20);
            boolean selected = Objects.equals(command, selectedCommand);
            int background = selected ? COLOR_LIST_SELECTED : isInside(mouseX, mouseY, row) ? COLOR_PANEL_SOFT : 0;
            if (background != 0) {
                guiGraphics.fill(row.x(), row.y(), row.right(), row.bottom(), background);
            }
            guiGraphics.drawString(font, trimToWidth(command.getNames().get(0), row.width() / 2), row.x() + 10, row.y() + 5, selected ? COLOR_TEXT : COLOR_TEXT_MUTED);
            guiGraphics.drawString(font, trimToWidth(command.getShortDesc(), row.width() / 2 - 14), row.x() + row.width() / 2, row.y() + 5, COLOR_TEXT_SOFT);
        }

        if (selectedCommand == null) {
            guiGraphics.drawString(font, "No command selected", detail.x() + 18, detail.y() + 34, COLOR_TEXT_MUTED);
            return;
        }

        guiGraphics.drawString(font, trimToWidth(selectedCommand.getShortDesc(), detail.width() - 36), detail.x() + 18, detail.y() + 34, COLOR_TEXT);
        int descY = detail.y() + 58;
        for (String line : selectedCommand.getLongDesc()) {
            if (line.isEmpty()) {
                descY += 8;
                continue;
            }
            descY = drawWrappedText(guiGraphics, line, detail.x() + 18, descY, detail.width() - 36, COLOR_TEXT_MUTED, 2) + 2;
            if (descY > detail.y() + 150) {
                break;
            }
        }

        Rect argsRect = new Rect(detail.x() + 18, detail.y() + 188, Math.max(180, detail.width() - 164), FIELD_HEIGHT);
        Rect runRect = new Rect(argsRect.right() + 10, detail.y() + 184, 118, BUTTON_HEIGHT);
        guiGraphics.drawString(font, "Arguments", detail.x() + 18, argsRect.y() - 18, COLOR_TEXT_SOFT);
        drawFieldBackplate(guiGraphics, argsRect);
        drawButton(guiGraphics, runRect, "Run command", isInside(mouseX, mouseY, runRect), false);

        Rect preview = new Rect(detail.x() + 18, detail.y() + 234, detail.width() - 36, Math.max(84, detail.bottom() - (detail.y() + 252)));
        drawCard(guiGraphics, preview, "Preview");
        drawWrappedText(guiGraphics, previewConsoleCommand(), preview.x() + 18, preview.y() + 34, preview.width() - 36, COLOR_SUCCESS, 3);
    }

    private void renderGotoPane(GuiGraphics guiGraphics, Rect form, int mouseX, int mouseY) {
        Rect content = actionsContentRect(form);
        int contentX = content.x();
        int contentWidth = content.width();
        int fieldY = content.y();
        int gap = 8;
        int axisLabelY = fieldY + 12;
        int fieldTop = fieldY + 20;
        int colWidth = Math.max(60, (contentWidth - gap * 2) / 3);
        Rect xRect = new Rect(contentX, fieldTop, colWidth, ACTION_FIELD_HEIGHT);
        Rect yRect = new Rect(contentX + colWidth + gap, fieldTop, colWidth, ACTION_FIELD_HEIGHT);
        Rect zRect = new Rect(contentX + (colWidth + gap) * 2, fieldTop, colWidth, ACTION_FIELD_HEIGHT);
        int buttonWidth = (contentWidth - gap) / 2;
        Rect currentRect = new Rect(contentX, fieldY + 50, buttonWidth, ACTION_BUTTON_HEIGHT);
        Rect lookRect = new Rect(contentX + buttonWidth + gap, fieldY + 50, buttonWidth, ACTION_BUTTON_HEIGHT);
        Rect runRect = new Rect(contentX, fieldY + 78, contentWidth, ACTION_BUTTON_HEIGHT);

        guiGraphics.drawString(font, "Coordinates", contentX, fieldY, COLOR_TEXT_MUTED);
        drawFieldBackplate(guiGraphics, xRect);
        drawFieldBackplate(guiGraphics, yRect);
        drawFieldBackplate(guiGraphics, zRect);
        guiGraphics.drawString(font, "X", xRect.x(), axisLabelY, COLOR_TEXT_SOFT);
        guiGraphics.drawString(font, "Y", yRect.x(), axisLabelY, COLOR_TEXT_SOFT);
        guiGraphics.drawString(font, "Z", zRect.x(), axisLabelY, COLOR_TEXT_SOFT);
        drawButton(guiGraphics, currentRect, "Use position", isInside(mouseX, mouseY, currentRect), false);
        drawButton(guiGraphics, lookRect, "Use look target", isInside(mouseX, mouseY, lookRect), false);
        drawButton(guiGraphics, runRect, "Run goto", isInside(mouseX, mouseY, runRect), false);
    }

    private void renderMinePane(GuiGraphics guiGraphics, Rect form, int mouseX, int mouseY) {
        Rect content = actionsContentRect(form);
        int contentX = content.x();
        int contentWidth = content.width();
        int gap = 8;
        int fieldY = content.y();
        int countWidth = Math.min(88, Math.max(68, contentWidth / 4));
        Rect targetRect = new Rect(contentX, fieldY + 16, contentWidth - countWidth - gap, ACTION_FIELD_HEIGHT);
        Rect countRect = new Rect(contentX + contentWidth - countWidth, fieldY + 16, countWidth, ACTION_FIELD_HEIGHT);
        int buttonWidth = (contentWidth - gap) / 2;
        Rect pickRect = new Rect(contentX, fieldY + 46, buttonWidth, ACTION_BUTTON_HEIGHT);
        Rect mineRect = new Rect(contentX + buttonWidth + gap, fieldY + 46, buttonWidth, ACTION_BUTTON_HEIGHT);

        guiGraphics.drawString(font, "Block", contentX, fieldY, COLOR_TEXT_MUTED);
        drawFieldBackplate(guiGraphics, targetRect);
        guiGraphics.drawString(font, "Quantity", countRect.x(), countRect.y() - 12, COLOR_TEXT_SOFT);
        drawFieldBackplate(guiGraphics, countRect);
        drawButton(guiGraphics, pickRect, "Use look target", isInside(mouseX, mouseY, pickRect), false);
        drawButton(guiGraphics, mineRect, "Mine now", isInside(mouseX, mouseY, mineRect), false);
        guiGraphics.drawString(font, trimToWidth(actionPreview(), contentWidth), contentX, mineRect.bottom() + 12, COLOR_SUCCESS);
    }

    private void renderFollowPane(GuiGraphics guiGraphics, Rect form, int mouseX, int mouseY) {
        Rect content = actionsContentRect(form);
        int contentX = content.x();
        int contentWidth = content.width();
        int fieldY = content.y();
        Rect modeRect = new Rect(contentX, fieldY + 16, Math.min(220, contentWidth), ACTION_BUTTON_HEIGHT);
        Rect targetRect = new Rect(contentX, fieldY + 54, contentWidth, ACTION_FIELD_HEIGHT);
        Rect followRect = new Rect(contentX, fieldY + 84, Math.min(170, contentWidth), ACTION_BUTTON_HEIGHT);

        guiGraphics.drawString(font, "Mode", contentX, fieldY, COLOR_TEXT_MUTED);
        drawButton(guiGraphics, modeRect, followMode.label, isInside(mouseX, mouseY, modeRect), true);
        guiGraphics.drawString(font, "Target", contentX, targetRect.y() - 12, COLOR_TEXT_SOFT);
        drawFieldBackplate(guiGraphics, targetRect);
        drawButton(guiGraphics, followRect, "Follow", isInside(mouseX, mouseY, followRect), false);
        guiGraphics.drawString(font, trimToWidth(actionPreview(), contentWidth), contentX, followRect.bottom() + 12, COLOR_SUCCESS);
    }

    private void renderBuildPane(GuiGraphics guiGraphics, Rect form, int mouseX, int mouseY) {
        Rect content = actionsContentRect(form);
        int contentX = content.x();
        int contentWidth = content.width();
        int gap = 8;
        int fieldY = content.y();
        Rect fileRect = new Rect(contentX, fieldY + 16, contentWidth, ACTION_FIELD_HEIGHT);
        int colWidth = Math.max(60, (contentWidth - gap * 2) / 3);
        Rect xRect = new Rect(contentX, fieldY + 70, colWidth, ACTION_FIELD_HEIGHT);
        Rect yRect = new Rect(contentX + colWidth + gap, fieldY + 70, colWidth, ACTION_FIELD_HEIGHT);
        Rect zRect = new Rect(contentX + (colWidth + gap) * 2, fieldY + 70, colWidth, ACTION_FIELD_HEIGHT);
        int buttonWidth = (contentWidth - gap) / 2;
        Rect positionRect = new Rect(contentX, fieldY + 100, buttonWidth, ACTION_BUTTON_HEIGHT);
        Rect buildRect = new Rect(contentX + buttonWidth + gap, fieldY + 100, buttonWidth, ACTION_BUTTON_HEIGHT);

        guiGraphics.drawString(font, "Schematic file", contentX, fieldY, COLOR_TEXT_MUTED);
        drawFieldBackplate(guiGraphics, fileRect);
        guiGraphics.drawString(font, "Origin", contentX, fieldY + 54, COLOR_TEXT_MUTED);
        drawFieldBackplate(guiGraphics, xRect);
        drawFieldBackplate(guiGraphics, yRect);
        drawFieldBackplate(guiGraphics, zRect);
        guiGraphics.drawString(font, "X", xRect.x(), xRect.y() - 12, COLOR_TEXT_SOFT);
        guiGraphics.drawString(font, "Y", yRect.x(), yRect.y() - 12, COLOR_TEXT_SOFT);
        guiGraphics.drawString(font, "Z", zRect.x(), zRect.y() - 12, COLOR_TEXT_SOFT);
        drawButton(guiGraphics, positionRect, "Use position", isInside(mouseX, mouseY, positionRect), false);
        drawButton(guiGraphics, buildRect, "Build", isInside(mouseX, mouseY, buildRect), false);
    }

    private void renderUtilityPane(GuiGraphics guiGraphics, Rect form, int mouseX, int mouseY) {
        Rect content = actionsContentRect(form);
        int contentX = content.x();
        int contentWidth = content.width();
        int gap = 8;
        int buttonWidth = Math.max(110, (contentWidth - gap) / 2);
        int buttonY = content.y() + 16;
        Rect exploreRect = new Rect(contentX, buttonY, buttonWidth, ACTION_BUTTON_HEIGHT);
        Rect surfaceRect = new Rect(exploreRect.right() + gap, buttonY, buttonWidth, ACTION_BUTTON_HEIGHT);
        Rect pathRect = new Rect(contentX, buttonY + 30, buttonWidth, ACTION_BUTTON_HEIGHT);
        Rect saveRect = new Rect(pathRect.right() + gap, buttonY + 30, buttonWidth, ACTION_BUTTON_HEIGHT);
        Rect reloadRect = new Rect(contentX, buttonY + 60, buttonWidth, ACTION_BUTTON_HEIGHT);
        Rect pickerRect = new Rect(reloadRect.right() + gap, buttonY + 60, buttonWidth, ACTION_BUTTON_HEIGHT);

        guiGraphics.drawString(font, "Utilities", contentX, content.y(), COLOR_TEXT_MUTED);
        drawButton(guiGraphics, exploreRect, "Explore", isInside(mouseX, mouseY, exploreRect), false);
        drawButton(guiGraphics, surfaceRect, "Surface", isInside(mouseX, mouseY, surfaceRect), false);
        drawButton(guiGraphics, pathRect, "Path", isInside(mouseX, mouseY, pathRect), false);
        drawButton(guiGraphics, saveRect, "Save cache", isInside(mouseX, mouseY, saveRect), false);
        drawButton(guiGraphics, reloadRect, "Reload cache", isInside(mouseX, mouseY, reloadRect), false);
        drawButton(guiGraphics, pickerRect, "Selection picker", isInside(mouseX, mouseY, pickerRect), false);
        guiGraphics.drawString(font, trimToWidth("Utilities stay available without opening chat.", contentWidth), contentX, pickerRect.bottom() + 12, COLOR_TEXT_MUTED);
    }

    private void drawBanner(GuiGraphics guiGraphics, Layout layout) {
        if (bannerMessage == null || System.currentTimeMillis() > bannerDeadline) {
            return;
        }
        int bannerWidth = Math.min(layout.main().width(), Math.max(240, font.width(bannerMessage) + 28));
        int x = layout.main().x() + (layout.main().width() - bannerWidth) / 2;
        int y = layout.topBar().bottom() + 8;
        guiGraphics.fill(x, y, x + bannerWidth, y + 22, COLOR_ACCENT_SOFT);
        guiGraphics.drawString(font, bannerMessage, x + 14, y + 7, COLOR_TEXT);
    }

    private void drawPanel(GuiGraphics guiGraphics, Rect rect, boolean subtle) {
        int fill = subtle ? COLOR_PANEL_SOFT : COLOR_PANEL;
        guiGraphics.fill(rect.x(), rect.y(), rect.right(), rect.bottom(), fill);
        guiGraphics.fill(rect.x(), rect.y(), rect.right(), rect.y() + 1, COLOR_BORDER);
        guiGraphics.fill(rect.x(), rect.bottom() - 1, rect.right(), rect.bottom(), COLOR_BORDER);
        guiGraphics.fill(rect.x(), rect.y(), rect.x() + 1, rect.bottom(), COLOR_BORDER);
        guiGraphics.fill(rect.right() - 1, rect.y(), rect.right(), rect.bottom(), COLOR_BORDER);
    }

    private void drawCard(GuiGraphics guiGraphics, Rect rect, String label) {
        guiGraphics.fill(rect.x(), rect.y(), rect.right(), rect.bottom(), COLOR_PANEL_ALT);
        guiGraphics.fill(rect.x(), rect.y(), rect.right(), rect.y() + 1, COLOR_BORDER);
        guiGraphics.fill(rect.x(), rect.bottom() - 1, rect.right(), rect.bottom(), COLOR_BORDER);
        guiGraphics.fill(rect.x(), rect.y(), rect.x() + 1, rect.bottom(), COLOR_BORDER);
        guiGraphics.fill(rect.right() - 1, rect.y(), rect.right(), rect.bottom(), COLOR_BORDER);
        guiGraphics.drawString(font, label, rect.x() + 14, rect.y() + 12, COLOR_TEXT);
    }

    private void drawButton(GuiGraphics guiGraphics, Rect rect, String label, boolean hovered, boolean accent) {
        int fill = accent ? COLOR_ACCENT_SOFT : hovered ? COLOR_BUTTON_HOVER : COLOR_BUTTON;
        guiGraphics.fill(rect.x(), rect.y(), rect.right(), rect.bottom(), fill);
        guiGraphics.fill(rect.x(), rect.y(), rect.right(), rect.y() + 1, accent ? COLOR_ACCENT : COLOR_BORDER);
        guiGraphics.fill(rect.x(), rect.bottom() - 1, rect.right(), rect.bottom(), accent ? COLOR_ACCENT : COLOR_BORDER);
        guiGraphics.drawString(font, trimToWidth(label, rect.width() - 16), rect.x() + 10, rect.y() + 10, COLOR_TEXT);
    }

    private void drawSidebarButton(GuiGraphics guiGraphics, Rect rect, String label, boolean hovered, boolean selected) {
        int fill = selected ? COLOR_ACCENT_SOFT : hovered ? COLOR_BUTTON_HOVER : 0x00000000;
        if (fill != 0) {
            guiGraphics.fill(rect.x(), rect.y(), rect.right(), rect.bottom(), fill);
        }
        guiGraphics.fill(rect.x(), rect.bottom() - 1, rect.right(), rect.bottom(), selected ? COLOR_ACCENT : COLOR_BORDER);
        guiGraphics.drawString(font, label, rect.x() + 12, rect.y() + 10, COLOR_TEXT);
    }

    private void drawFieldBackplate(GuiGraphics guiGraphics, Rect rect) {
        guiGraphics.fill(rect.x(), rect.y(), rect.right(), rect.bottom(), 0xFF111821);
        guiGraphics.fill(rect.x(), rect.bottom() - 1, rect.right(), rect.bottom(), COLOR_ACCENT_SOFT);
    }

    private void drawKeyValue(GuiGraphics guiGraphics, int x, int y, String key, String value, int width) {
        int keyWidth = 72;
        guiGraphics.drawString(font, key, x, y, COLOR_TEXT_SOFT);
        guiGraphics.drawString(font, trimToWidth(value, Math.max(40, width - keyWidth - 10)), x + keyWidth, y, COLOR_TEXT);
    }

    private int drawWrappedText(GuiGraphics guiGraphics, String text, int x, int y, int maxWidth, int color, int maxLines) {
        int line = 0;
        for (FormattedCharSequence sequence : font.split(Component.literal(text), maxWidth)) {
            if (line >= maxLines) {
                break;
            }
            guiGraphics.drawString(font, sequence, x, y + line * (font.lineHeight + 2), color);
            line++;
        }
        return y + line * (font.lineHeight + 2);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button != 0) {
            return false;
        }

        Layout layout = layout();
        if (handleSidebarClick(layout, mouseX, mouseY)) {
            return true;
        }
        return switch (section) {
            case DASHBOARD -> handleDashboardClick(layout, mouseX, mouseY);
            case ACTIONS -> handleActionsClick(layout, mouseX, mouseY);
            case SETTINGS -> handleSettingsClick(layout, mouseX, mouseY);
            case CONSOLE -> handleConsoleClick(layout, mouseX, mouseY);
        };
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (section == Section.CONSOLE && commandArgsField != null && commandArgsField.isFocused() && keyCode == 257) {
            executeConsoleCommand();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        Layout layout = layout();
        if (section == Section.SETTINGS && isInside(mouseX, mouseY, settingsListBodyRect(layout))) {
            settingsScroll = clampScroll(settingsScroll - (int) Math.signum(verticalAmount), filteredSettings().size(), settingsVisibleRows(layout));
            return true;
        }
        if (section == Section.CONSOLE && isInside(mouseX, mouseY, consoleListBodyRect(layout))) {
            commandScroll = clampScroll(commandScroll - (int) Math.signum(verticalAmount), filteredCommands().size(), consoleVisibleRows(layout));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private boolean handleSidebarClick(Layout layout, double mouseX, double mouseY) {
        Rect sidebar = layout.sidebar();
        int y = sidebar.y() + 18;
        for (Section value : Section.values()) {
            Rect button = new Rect(sidebar.x() + 12, y, sidebar.width() - 24, NAV_BUTTON_HEIGHT);
            if (isInside(mouseX, mouseY, button)) {
                if (section != value) {
                    section = value;
                    SESSION.section = value;
                    rebuildWidgets();
                }
                return true;
            }
            y += NAV_BUTTON_HEIGHT + 8;
        }
        return false;
    }

    private boolean handleDashboardClick(Layout layout, double mouseX, double mouseY) {
        Rect main = innerMain(layout);
        Rect quick = new Rect(main.x(), main.y() + 42, main.width(), 154);
        int gap = 10;
        int halfWidth = (quick.width() - 50 - gap) / 2;
        Rect openActions = new Rect(quick.x() + 18, quick.y() + 34, halfWidth, BUTTON_HEIGHT);
        Rect picker = new Rect(openActions.right() + gap, quick.y() + 34, halfWidth, BUTTON_HEIGHT);
        Rect pause = new Rect(quick.x() + 18, quick.y() + 72, halfWidth, BUTTON_HEIGHT);
        Rect resume = new Rect(pause.right() + gap, quick.y() + 72, halfWidth, BUTTON_HEIGHT);
        Rect cancel = new Rect(quick.x() + 18, quick.y() + 110, quick.width() - 36, BUTTON_HEIGHT);

        if (isInside(mouseX, mouseY, openActions)) {
            section = Section.ACTIONS;
            rebuildWidgets();
            return true;
        }
        if (isInside(mouseX, mouseY, picker)) {
            baritone.openClick();
            onClose();
            return true;
        }
        if (isInside(mouseX, mouseY, pause)) {
            runBackendCommand("pause", "Paused Hypervision");
            return true;
        }
        if (isInside(mouseX, mouseY, resume)) {
            runBackendCommand("resume", "Resumed Hypervision");
            return true;
        }
        if (isInside(mouseX, mouseY, cancel)) {
            runBackendCommand("cancel", "Canceled the active process");
            return true;
        }
        return false;
    }

    private boolean handleActionsClick(Layout layout, double mouseX, double mouseY) {
        Rect form = actionsFormRect(layout);
        if (isInside(mouseX, mouseY, actionSelectorPreviousRect(form))) {
            actionPane = actionPane.previous();
            SESSION.actionPane = actionPane;
            rebuildWidgets();
            return true;
        }
        if (isInside(mouseX, mouseY, actionSelectorLabelRect(form)) || isInside(mouseX, mouseY, actionSelectorNextRect(form))) {
            actionPane = actionPane.next();
            SESSION.actionPane = actionPane;
            rebuildWidgets();
            return true;
        }

        Rect content = actionsContentRect(form);
        int contentX = content.x();
        int contentWidth = content.width();
        int gap = 8;
        switch (actionPane) {
            case GOTO -> {
                int fieldY = content.y();
                int buttonWidth = (contentWidth - gap) / 2;
                Rect currentRect = new Rect(contentX, fieldY + 50, buttonWidth, ACTION_BUTTON_HEIGHT);
                Rect lookRect = new Rect(contentX + buttonWidth + gap, fieldY + 50, buttonWidth, ACTION_BUTTON_HEIGHT);
                Rect runRect = new Rect(contentX, fieldY + 78, contentWidth, ACTION_BUTTON_HEIGHT);
                if (isInside(mouseX, mouseY, currentRect)) {
                    BlockPos pos = baritone.getPlayerContext().playerFeet();
                    gotoX = Integer.toString(pos.getX());
                    gotoY = Integer.toString(pos.getY());
                    gotoZ = Integer.toString(pos.getZ());
                    SESSION.gotoX = gotoX;
                    SESSION.gotoY = gotoY;
                    SESSION.gotoZ = gotoZ;
                    rebuildWidgets();
                    return true;
                }
                if (isInside(mouseX, mouseY, lookRect)) {
                    BlockPos pos = getLookTarget();
                    if (pos == null) {
                        flash("No block target under the crosshair");
                    } else {
                        gotoX = Integer.toString(pos.getX());
                        gotoY = Integer.toString(pos.getY());
                        gotoZ = Integer.toString(pos.getZ());
                        SESSION.gotoX = gotoX;
                        SESSION.gotoY = gotoY;
                        SESSION.gotoZ = gotoZ;
                        rebuildWidgets();
                    }
                    return true;
                }
                if (isInside(mouseX, mouseY, runRect)) {
                    executeActionCommand(actionPreview());
                    return true;
                }
            }
            case MINE -> {
                int fieldY = content.y();
                int buttonWidth = (contentWidth - gap) / 2;
                Rect pickRect = new Rect(contentX, fieldY + 46, buttonWidth, ACTION_BUTTON_HEIGHT);
                Rect mineRect = new Rect(contentX + buttonWidth + gap, fieldY + 46, buttonWidth, ACTION_BUTTON_HEIGHT);
                if (isInside(mouseX, mouseY, pickRect)) {
                    BlockPos pos = getLookTarget();
                    if (pos == null || baritone.getPlayerContext().world() == null) {
                        flash("No block target under the crosshair");
                    } else {
                        mineTarget = baritone.getPlayerContext().world().getBlockState(pos).getBlock().builtInRegistryHolder().key().location().toString();
                        SESSION.mineTarget = mineTarget;
                        rebuildWidgets();
                    }
                    return true;
                }
                if (isInside(mouseX, mouseY, mineRect)) {
                    executeActionCommand(actionPreview());
                    return true;
                }
            }
            case FOLLOW -> {
                int fieldY = content.y();
                Rect modeRect = new Rect(contentX, fieldY + 16, Math.min(220, contentWidth), ACTION_BUTTON_HEIGHT);
                Rect followRect = new Rect(contentX, fieldY + 84, Math.min(170, contentWidth), ACTION_BUTTON_HEIGHT);
                if (isInside(mouseX, mouseY, modeRect)) {
                    followMode = followMode.next();
                    SESSION.followMode = followMode;
                    return true;
                }
                if (isInside(mouseX, mouseY, followRect)) {
                    executeActionCommand(actionPreview());
                    return true;
                }
            }
            case BUILD -> {
                int fieldY = content.y();
                int buttonWidth = (contentWidth - gap) / 2;
                Rect positionRect = new Rect(contentX, fieldY + 100, buttonWidth, ACTION_BUTTON_HEIGHT);
                Rect buildRect = new Rect(contentX + buttonWidth + gap, fieldY + 100, buttonWidth, ACTION_BUTTON_HEIGHT);
                if (isInside(mouseX, mouseY, positionRect)) {
                    BlockPos pos = baritone.getPlayerContext().playerFeet();
                    buildX = Integer.toString(pos.getX());
                    buildY = Integer.toString(pos.getY());
                    buildZ = Integer.toString(pos.getZ());
                    SESSION.buildX = buildX;
                    SESSION.buildY = buildY;
                    SESSION.buildZ = buildZ;
                    rebuildWidgets();
                    return true;
                }
                if (isInside(mouseX, mouseY, buildRect)) {
                    executeActionCommand(actionPreview());
                    return true;
                }
            }
            case UTILITY -> {
                int utilityButtonWidth = Math.max(150, (contentWidth - gap) / 2);
                int buttonY = content.y() + 16;
                Rect exploreRect = new Rect(contentX, buttonY, utilityButtonWidth, ACTION_BUTTON_HEIGHT);
                Rect surfaceRect = new Rect(exploreRect.right() + gap, buttonY, utilityButtonWidth, ACTION_BUTTON_HEIGHT);
                Rect pathRect = new Rect(contentX, buttonY + 30, utilityButtonWidth, ACTION_BUTTON_HEIGHT);
                Rect saveRect = new Rect(pathRect.right() + gap, buttonY + 30, utilityButtonWidth, ACTION_BUTTON_HEIGHT);
                Rect reloadRect = new Rect(contentX, buttonY + 60, utilityButtonWidth, ACTION_BUTTON_HEIGHT);
                Rect pickerRect = new Rect(reloadRect.right() + gap, buttonY + 60, utilityButtonWidth, ACTION_BUTTON_HEIGHT);
                if (isInside(mouseX, mouseY, exploreRect)) return backendAction("explore", "Started exploring");
                if (isInside(mouseX, mouseY, surfaceRect)) return backendAction("surface", "Heading for the surface");
                if (isInside(mouseX, mouseY, pathRect)) return backendAction("path", "Requested pathing");
                if (isInside(mouseX, mouseY, saveRect)) return backendAction("saveall", "Saved the world cache");
                if (isInside(mouseX, mouseY, reloadRect)) return backendAction("reloadall", "Reloaded the world cache");
                if (isInside(mouseX, mouseY, pickerRect)) {
                    baritone.openClick();
                    onClose();
                    return true;
                }
            }
        }
        return false;
    }

    private boolean backendAction(String command, String success) {
        runBackendCommand(command, success);
        return true;
    }

    private boolean handleSettingsClick(Layout layout, double mouseX, double mouseY) {
        Rect listBody = settingsListBodyRect(layout);
        List<Settings.Setting<?>> filtered = filteredSettings();
        int rows = settingsVisibleRows(layout);
        for (int index = 0; index < rows; index++) {
            int settingIndex = settingsScroll + index;
            if (settingIndex >= filtered.size()) {
                break;
            }
            Rect row = new Rect(listBody.x(), listBody.y() + index * LIST_ROW_HEIGHT, listBody.width(), 20);
            if (isInside(mouseX, mouseY, row)) {
                selectSetting(filtered.get(settingIndex));
                rebuildWidgets();
                return true;
            }
        }

        if (selectedSetting == null) {
            return false;
        }

        Rect detail = settingsDetailRect(layout);
        if (selectedSetting.getValueClass() == Boolean.class) {
            Rect toggleRect = new Rect(detail.x() + 18, detail.bottom() - 92, 150, BUTTON_HEIGHT);
            if (isInside(mouseX, mouseY, toggleRect)) {
                toggleSelectedSetting();
                return true;
            }
        } else {
            Rect valueRect = new Rect(detail.x() + 18, detail.bottom() - 92, Math.max(180, detail.width() - 150), FIELD_HEIGHT);
            Rect applyRect = new Rect(valueRect.right() + 10, detail.bottom() - 96, 104, BUTTON_HEIGHT);
            if (isInside(mouseX, mouseY, applyRect)) {
                applySelectedSetting();
                return true;
            }
        }

        Rect resetRect = new Rect(detail.x() + 18, detail.bottom() - 52, 150, BUTTON_HEIGHT);
        if (isInside(mouseX, mouseY, resetRect)) {
            resetSelectedSetting();
            return true;
        }
        return false;
    }

    private boolean handleConsoleClick(Layout layout, double mouseX, double mouseY) {
        Rect listBody = consoleListBodyRect(layout);
        List<ICommand> filtered = filteredCommands();
        int rows = consoleVisibleRows(layout);
        for (int index = 0; index < rows; index++) {
            int commandIndex = commandScroll + index;
            if (commandIndex >= filtered.size()) {
                break;
            }
            Rect row = new Rect(listBody.x(), listBody.y() + index * LIST_ROW_HEIGHT, listBody.width(), 20);
            if (isInside(mouseX, mouseY, row)) {
                selectedCommand = filtered.get(commandIndex);
                return true;
            }
        }

        Rect detail = consoleDetailRect(layout);
        Rect argsRect = new Rect(detail.x() + 18, detail.y() + 188, Math.max(180, detail.width() - 164), FIELD_HEIGHT);
        Rect runRect = new Rect(argsRect.right() + 10, detail.y() + 184, 118, BUTTON_HEIGHT);
        if (isInside(mouseX, mouseY, runRect)) {
            executeConsoleCommand();
            return true;
        }
        return false;
    }

    private void executeActionCommand(String command) {
        if (command == null || command.isBlank()) {
            flash("Complete the form before running this action");
            return;
        }
        runBackendCommand(command, "Executed " + command);
    }

    private void executeConsoleCommand() {
        String command = previewConsoleCommand();
        if (command == null || command.isBlank()) {
            flash("Choose a command before running it");
            return;
        }
        runBackendCommand(command, "Executed " + command);
    }

    private void runBackendCommand(String command, String successMessage) {
        boolean executed = baritone.getCommandManager().execute(command);
        if (executed) {
            flash(successMessage);
        } else {
            flash("Unable to execute: " + command);
        }
    }

    private void toggleSelectedSetting() {
        if (selectedSetting == null || selectedSetting.getValueClass() != Boolean.class) {
            return;
        }
        @SuppressWarnings("unchecked")
        Settings.Setting<Boolean> booleanSetting = (Settings.Setting<Boolean>) selectedSetting;
        booleanSetting.value = !booleanSetting.value;
        SettingsUtil.save(Baritone.settings());
        flash("Updated setting " + selectedSetting.getName());
    }

    private void applySelectedSetting() {
        if (selectedSetting == null) {
            return;
        }
        try {
            SettingsUtil.parseAndApply(Baritone.settings(), selectedSetting.getName(), settingsDraftValue);
            SettingsUtil.save(Baritone.settings());
            selectSetting(selectedSetting);
            rebuildWidgets();
            flash("Applied setting " + selectedSetting.getName());
        } catch (Exception ignored) {
            flash("Invalid value for " + selectedSetting.getName());
        }
    }

    private void resetSelectedSetting() {
        if (selectedSetting == null) {
            return;
        }
        selectedSetting.reset();
        SettingsUtil.save(Baritone.settings());
        selectSetting(selectedSetting);
        rebuildWidgets();
        flash("Reset setting " + selectedSetting.getName());
    }

    private void selectSetting(Settings.Setting<?> setting) {
        selectedSetting = setting;
        settingsDraftValue = safeSettingValue(setting);
    }

    private void synchronizeSelections() {
        List<Settings.Setting<?>> filteredSettings = filteredSettings();
        if (selectedSetting == null || !filteredSettings.contains(selectedSetting)) {
            selectedSetting = filteredSettings.isEmpty() ? null : filteredSettings.get(0);
            if (selectedSetting != null) {
                settingsDraftValue = safeSettingValue(selectedSetting);
            }
        }
        settingsScroll = clampScroll(settingsScroll, filteredSettings.size(), settingsVisibleRows(layout()));

        List<ICommand> filteredCommands = filteredCommands();
        if (selectedCommand == null || !filteredCommands.contains(selectedCommand)) {
            selectedCommand = filteredCommands.isEmpty() ? null : filteredCommands.get(0);
        }
        commandScroll = clampScroll(commandScroll, filteredCommands.size(), consoleVisibleRows(layout()));
    }

    private List<Settings.Setting<?>> filteredSettings() {
        String query = settingsSearch.toLowerCase(Locale.ROOT);
        return Baritone.settings().allSettings.stream()
                .filter(setting -> !setting.isJavaOnly())
                .filter(setting -> setting.getName().toLowerCase(Locale.ROOT).contains(query))
                .sorted(Comparator.comparing(Settings.Setting::getName))
                .collect(Collectors.toList());
    }

    private List<ICommand> filteredCommands() {
        String query = commandSearch.toLowerCase(Locale.ROOT);
        return commands.stream()
                .filter(command -> command.getNames().stream().anyMatch(name -> name.toLowerCase(Locale.ROOT).contains(query)))
                .collect(Collectors.toList());
    }

    private String actionPreview() {
        return switch (actionPane) {
            case GOTO -> buildGotoPreview();
            case MINE -> buildMinePreview();
            case FOLLOW -> buildFollowPreview();
            case BUILD -> buildBuildPreview();
            case UTILITY -> "Use one of the utility actions";
        };
    }

    private String buildGotoPreview() {
        if (gotoX.isBlank() || gotoY.isBlank() || gotoZ.isBlank()) {
            return "";
        }
        return String.format("goto %s %s %s", gotoX.trim(), gotoY.trim(), gotoZ.trim());
    }

    private String buildMinePreview() {
        if (mineTarget.isBlank()) {
            return "";
        }
        if (mineCount.isBlank()) {
            return "mine " + mineTarget.trim();
        }
        return String.format("mine %s %s", mineCount.trim(), mineTarget.trim());
    }

    private String buildFollowPreview() {
        return switch (followMode) {
            case PLAYERS -> "follow players";
            case ENTITIES -> "follow entities";
            case PLAYER -> followTarget.isBlank() ? "" : "follow player " + followTarget.trim();
            case ENTITY -> followTarget.isBlank() ? "" : "follow entity " + followTarget.trim();
        };
    }

    private String buildBuildPreview() {
        if (buildFile.isBlank()) {
            return "";
        }
        if (buildX.isBlank() || buildY.isBlank() || buildZ.isBlank()) {
            return "build " + buildFile.trim();
        }
        return String.format("build %s %s %s %s", buildFile.trim(), buildX.trim(), buildY.trim(), buildZ.trim());
    }

    private String previewConsoleCommand() {
        if (selectedCommand == null) {
            return "";
        }
        return commandArgs.isBlank() ? selectedCommand.getNames().get(0) : selectedCommand.getNames().get(0) + " " + commandArgs.trim();
    }

    private ICommand findCommand(String primaryName) {
        return commands.stream()
                .filter(command -> command.getNames().contains(primaryName))
                .findFirst()
                .orElse(commands.isEmpty() ? null : commands.get(0));
    }

    private BlockPos getLookTarget() {
        if (mc.hitResult instanceof BlockHitResult blockHitResult) {
            return blockHitResult.getBlockPos();
        }
        return null;
    }

    private String currentGoalText() {
        Goal goal = baritone.getPathingBehavior().getGoal();
        return goal == null ? "None" : trimToWidth(goal.toString(), 128);
    }

    private String currentProcessText() {
        Optional<IBaritoneProcess> process = baritone.getPathingControlManager().mostRecentInControl();
        return process.map(IBaritoneProcess::displayName).orElse("Idle");
    }

    private String currentEtaText() {
        return baritone.getPathingBehavior().estimatedTicksToGoal().map(this::formatTicks).orElse("Unknown");
    }

    private String currentPathState() {
        if (baritone.getElytraProcess().isActive()) {
            return "Elytra active";
        }
        if (baritone.getPathingBehavior().isPathing()) {
            return "Pathing";
        }
        if (baritone.getPathingBehavior().getInProgress().isPresent()) {
            return "Calculating";
        }
        return baritone.getPathingBehavior().getGoal() != null ? "Goal armed" : "Idle";
    }

    private String formatTicks(double ticks) {
        int totalSeconds = Math.max(0, (int) Math.round(ticks / 20.0D));
        return String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    private String formatBlock(BlockPos pos) {
        return pos == null ? "Unknown" : pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }

    private String safeSettingValue(Settings.Setting<?> setting) {
        try {
            return SettingsUtil.settingValueToString(setting);
        } catch (Exception ignored) {
            return String.valueOf(setting.value);
        }
    }

    private String safeDefaultValue(Settings.Setting<?> setting) {
        try {
            return SettingsUtil.settingDefaultToString(setting);
        } catch (Exception ignored) {
            return String.valueOf(setting.defaultValue);
        }
    }

    private void flash(String message) {
        bannerMessage = message;
        bannerDeadline = System.currentTimeMillis() + 3000L;
    }

    private Layout layout() {
        return new Layout(width, height);
    }

    private Rect innerMain(Layout layout) {
        return new Rect(layout.main().x() + 18, layout.main().y() + 18, layout.main().width() - 36, layout.main().height() - 36);
    }

    private Rect actionsNavRect(Layout layout) {
        Rect main = innerMain(layout);
        int width = clampInt(main.width() / 8, 108, 132);
        return new Rect(main.x(), main.y() + 42, width, main.height() - 42);
    }

    private Rect actionsFormRect(Layout layout) {
        Rect main = innerMain(layout);
        return new Rect(main.x(), main.y(), main.width(), main.height());
    }

    private Rect actionsContentRect(Layout layout) {
        return actionsContentRect(actionsFormRect(layout));
    }

    private Rect actionsContentRect(Rect form) {
        return new Rect(form.x() + 18, form.y() + 74, form.width() - 36, Math.max(74, form.height() - 92));
    }

    private Rect actionsPreviewRect(Layout layout) {
        Rect form = actionsFormRect(layout);
        return new Rect(form.x() + 18, form.bottom() - 78, form.width() - 36, 54);
    }

    private Rect actionSelectorPreviousRect(Rect form) {
        return new Rect(form.x() + 18, form.y() + 32, 24, ACTION_BUTTON_HEIGHT);
    }

    private Rect actionSelectorLabelRect(Rect form) {
        Rect previous = actionSelectorPreviousRect(form);
        Rect next = actionSelectorNextRect(form);
        return new Rect(previous.right() + 8, previous.y(), Math.max(72, next.x() - previous.right() - 16), ACTION_BUTTON_HEIGHT);
    }

    private Rect actionSelectorNextRect(Rect form) {
        return new Rect(form.right() - 42, form.y() + 32, 24, ACTION_BUTTON_HEIGHT);
    }

    private static final class SessionState {
        private Section section = Section.DASHBOARD;
        private ActionPane actionPane = ActionPane.GOTO;
        private FollowMode followMode = FollowMode.PLAYERS;
        private String gotoX = "";
        private String gotoY = "";
        private String gotoZ = "";
        private String mineTarget = "diamond_ore";
        private String mineCount = "";
        private String followTarget = "";
        private String buildFile = "";
        private String buildX = "";
        private String buildY = "";
        private String buildZ = "";
    }

    private Rect settingsListRect(Layout layout) {
        Rect main = innerMain(layout);
        int width = Math.min(300, Math.max(260, main.width() / 3));
        return new Rect(main.x(), main.y() + 42, width, main.height() - 42);
    }

    private Rect settingsDetailRect(Layout layout) {
        Rect main = innerMain(layout);
        Rect list = settingsListRect(layout);
        return new Rect(list.right() + 12, main.y() + 42, main.right() - list.right() - 12, main.height() - 42);
    }

    private Rect settingsListBodyRect(Layout layout) {
        Rect list = settingsListRect(layout);
        return new Rect(list.x() + 12, list.y() + 64, list.width() - 24, Math.max(0, list.height() - 80));
    }

    private Rect consoleListRect(Layout layout) {
        return settingsListRect(layout);
    }

    private Rect consoleDetailRect(Layout layout) {
        return settingsDetailRect(layout);
    }

    private Rect consoleListBodyRect(Layout layout) {
        return settingsListBodyRect(layout);
    }

    private int settingsVisibleRows(Layout layout) {
        return Math.max(1, settingsListBodyRect(layout).height() / LIST_ROW_HEIGHT);
    }

    private int consoleVisibleRows(Layout layout) {
        return Math.max(1, consoleListBodyRect(layout).height() / LIST_ROW_HEIGHT);
    }

    private Rect insetTop(Rect rect, int insetX, int insetY, int width, int height) {
        return new Rect(rect.x() + insetX, rect.y() + insetY, width, height);
    }

    private boolean isInside(double mouseX, double mouseY, Rect rect) {
        return mouseX >= rect.x() && mouseX <= rect.right() && mouseY >= rect.y() && mouseY <= rect.bottom();
    }

    private int clampScroll(int value, int itemCount, int pageSize) {
        return Math.max(0, Math.min(value, Math.max(0, itemCount - pageSize)));
    }

    private String trimToWidth(String value, int maxWidth) {
        if (value == null || font.width(value) <= maxWidth) {
            return value == null ? "" : value;
        }
        String ellipsis = "...";
        return font.plainSubstrByWidth(value, Math.max(0, maxWidth - font.width(ellipsis))) + ellipsis;
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private enum Section {
        DASHBOARD("Dashboard"),
        ACTIONS("Actions"),
        SETTINGS("Settings"),
        CONSOLE("Console");

        private final String label;

        Section(String label) {
            this.label = label;
        }
    }

    private enum ActionPane {
        GOTO("Go to"),
        MINE("Mine"),
        FOLLOW("Follow"),
        BUILD("Build"),
        UTILITY("Utility");

        private final String label;

        ActionPane(String label) {
            this.label = label;
        }

        private ActionPane next() {
            ActionPane[] values = values();
            return values[(ordinal() + 1) % values.length];
        }

        private ActionPane previous() {
            ActionPane[] values = values();
            return values[(ordinal() + values.length - 1) % values.length];
        }
    }

    private enum FollowMode {
        PLAYERS("Group: players"),
        ENTITIES("Group: entities"),
        PLAYER("Target player"),
        ENTITY("Target entity");

        private final String label;

        FollowMode(String label) {
            this.label = label;
        }

        private FollowMode next() {
            FollowMode[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }

    private record Rect(int x, int y, int width, int height) {
        private int right() {
            return x + width;
        }

        private int bottom() {
            return y + height;
        }
    }

    private static final class Layout {
        private final Rect topBar;
        private final Rect sidebar;
        private final Rect main;

        private Layout(int screenWidth, int screenHeight) {
            int outer = clampInt(screenWidth / 40, 16, 28);
            int gap = 12;
            int topBarHeight = 54;
            int bodyTop = outer + topBarHeight + gap;
            int bodyHeight = screenHeight - bodyTop - outer;
            int sidebarWidth = clampInt(screenWidth / 5, 168, 216);

            this.topBar = new Rect(outer, outer, screenWidth - outer * 2, topBarHeight);
            this.sidebar = new Rect(outer, bodyTop, sidebarWidth, bodyHeight);
            this.main = new Rect(sidebar.right() + gap, bodyTop, screenWidth - outer - (sidebar.right() + gap), bodyHeight);
        }

        private Rect topBar() { return topBar; }
        private Rect sidebar() { return sidebar; }
        private Rect main() { return main; }
    }
}
