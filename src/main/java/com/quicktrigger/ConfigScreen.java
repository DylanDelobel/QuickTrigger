package com.quicktrigger;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ConfigScreen extends Screen {

    private static final int ROW_HEIGHT = 24;
    private static final int BTN_WIDTH  = 160;
    private static final int BTN_HEIGHT = 20;
    private static final int GAP        = 6;
    private static final int ICON_SIZE  = 16;
    // button + gap + icon preview
    private static final int TOTAL_WIDTH = BTN_WIDTH + GAP + ICON_SIZE;

    private final Screen parent;
    private final String[] pendingColors = new String[9];

    // Calculated in init(), reused in render()
    private int blockStartX;
    private int startY;
    private int rowCount;

    public ConfigScreen(Screen parent) {
        super(Component.literal("QuickTrigger — Configuration"));
        this.parent = parent;
        System.arraycopy(QuickTriggerConfig.INSTANCE.bedColors, 0, pendingColors, 0, 9);
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        rowCount = QuickTriggerClient.maxHomes;
        blockStartX = centerX - TOTAL_WIDTH / 2;
        startY = this.height / 2 - (rowCount * ROW_HEIGHT) / 2 - 20;

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
                    (button, value) -> pendingColors[index] = value.name()
                );
            this.addRenderableWidget(btn);
        }

        int bottomY = startY + rowCount * ROW_HEIGHT + 8;
        int halfBtn = (TOTAL_WIDTH - GAP) / 2;

        this.addRenderableWidget(Button.builder(
            Component.literal("Sauvegarder"),
            btn -> {
                System.arraycopy(pendingColors, 0, QuickTriggerConfig.INSTANCE.bedColors, 0, 9);
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        int iconX = blockStartX + BTN_WIDTH + GAP;

        for (int i = 0; i < rowCount; i++) {
            int rowY = startY + i * ROW_HEIGHT;
            QuickTriggerConfig.BedColor color = QuickTriggerConfig.BedColor.fromName(pendingColors[i]);
            graphics.renderItem(new ItemStack(color.item), iconX, rowY + (BTN_HEIGHT - ICON_SIZE) / 2);
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
}
