package com.quicktrigger;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ConfigScreen extends Screen {

    private static final int ROW_HEIGHT  = 24;
    private static final int BTN_WIDTH   = 90;   // cycle button couleur
    private static final int NAME_WIDTH  = 110;  // EditBox nom custom
    private static final int BTN_HEIGHT  = 20;
    private static final int GAP         = 6;
    private static final int ICON_SIZE   = 16;
    // color button + gap + name field + gap + icon preview
    private static final int TOTAL_WIDTH = BTN_WIDTH + GAP + NAME_WIDTH + GAP + ICON_SIZE;

    private final Screen parent;
    private final String[] pendingColors = new String[9];
    private final String[] pendingNames  = new String[9];
    private EditBox[] nameFields;

    // Calculated in init(), reused in render()
    private int blockStartX;
    private int startY;
    private int rowCount;
    // Pre-built to avoid NPE when item components are not yet bound (main menu)
    private ItemStack[] iconStacks;

    public ConfigScreen(Screen parent) {
        super(Component.literal("QuickTrigger — Configuration"));
        this.parent = parent;
        System.arraycopy(QuickTriggerConfig.INSTANCE.bedColors, 0, pendingColors, 0, 9);
        String serverKey = QuickTriggerClient.currentServerKey != null ? QuickTriggerClient.currentServerKey : "offline";
        System.arraycopy(QuickTriggerConfig.INSTANCE.getNamesForServer(serverKey), 0, pendingNames, 0, 9);
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        rowCount = QuickTriggerClient.maxHomes;
        blockStartX = centerX - TOTAL_WIDTH / 2;
        startY = this.height / 2 - (rowCount * ROW_HEIGHT) / 2 - 20;

        iconStacks  = new ItemStack[rowCount];
        nameFields  = new EditBox[rowCount];
        for (int i = 0; i < rowCount; i++) {
            iconStacks[i] = safeStack(QuickTriggerConfig.BedColor.fromName(pendingColors[i]));
        }

        for (int i = 0; i < rowCount; i++) {
            final int index = i;
            QuickTriggerConfig.BedColor initial = QuickTriggerConfig.BedColor.fromName(pendingColors[i]);

            CycleButton<QuickTriggerConfig.BedColor> btn = CycleButton
                .<QuickTriggerConfig.BedColor>builder(
                    color -> Component.translatable(color.item.getDescriptionId()), initial)
                .withValues(QuickTriggerConfig.BedColor.values())
                .create(
                    blockStartX,
                    startY + i * ROW_HEIGHT,
                    BTN_WIDTH,
                    BTN_HEIGHT,
                    Component.literal("Home #" + (i + 1)),
                    (button, value) -> {
                        pendingColors[index] = value.name();
                        iconStacks[index] = safeStack(value);
                    }
                );
            this.addRenderableWidget(btn);

            EditBox nameField = new EditBox(
                this.font,
                blockStartX + BTN_WIDTH + GAP,
                startY + i * ROW_HEIGHT,
                NAME_WIDTH,
                BTN_HEIGHT,
                Component.literal("Nom du home #" + (i + 1))
            );
            nameField.setMaxLength(QuickTriggerConfig.MAX_NAME_LENGTH);
            nameField.setValue(pendingNames[i] == null ? "" : pendingNames[i]);
            nameField.setHint(Component.literal("Home #" + (i + 1)));
            final int nameIndex = i;
            nameField.setResponder(value -> pendingNames[nameIndex] = value);
            this.addRenderableWidget(nameField);
            nameFields[i] = nameField;
        }

        int bottomY = startY + rowCount * ROW_HEIGHT + 8;
        int halfBtn = (TOTAL_WIDTH - GAP) / 2;

        this.addRenderableWidget(Button.builder(
            Component.literal("Sauvegarder"),
            btn -> {
                System.arraycopy(pendingColors, 0, QuickTriggerConfig.INSTANCE.bedColors, 0, 9);
                String key = QuickTriggerClient.currentServerKey != null ? QuickTriggerClient.currentServerKey : "offline";
                String[] stored = QuickTriggerConfig.INSTANCE.getNamesForServer(key);
                System.arraycopy(pendingNames, 0, stored, 0, 9);
                QuickTriggerConfig.INSTANCE.save();
                this.minecraft.setScreen(parent);
            })
            .pos(blockStartX, bottomY)
            .size(halfBtn, BTN_HEIGHT)
            .build()
        );

        this.addRenderableWidget(Button.builder(
            Component.literal("Annuler"),
            btn -> this.minecraft.setScreen(parent))
            .pos(blockStartX + halfBtn + GAP, bottomY)
            .size(halfBtn, BTN_HEIGHT)
            .build()
        );
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        int iconX = blockStartX + BTN_WIDTH + GAP + NAME_WIDTH + GAP;

        for (int i = 0; i < rowCount; i++) {
            int rowY = startY + i * ROW_HEIGHT;
            graphics.item(iconStacks[i], iconX, rowY + (BTN_HEIGHT - ICON_SIZE) / 2);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    private static ItemStack safeStack(QuickTriggerConfig.BedColor color) {
        try {
            return new ItemStack(color.item);
        } catch (NullPointerException e) {
            // Item components not yet bound (opened from main menu before loading a world)
            return ItemStack.EMPTY;
        }
    }
}
