package com.quicktrigger;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import com.quicktrigger.QuickTriggerConfig.BedColor;



public class ConfigScreen extends Screen {

    private static final int ROW_HEIGHT = 24;
    private static final int BTN_WIDTH= 90;
    private static final int NAME_WIDTH=110;

    private static final int BTN_HEIGHT  = 20;
    private static final int GAP=6;
    private static final int ICON_SIZE = 16;
    private static final int TOTAL_WIDTH = BTN_WIDTH + GAP + NAME_WIDTH + GAP + ICON_SIZE;

    private final Screen parent;


    // edits live here until "Sauvegarder", cancel just drops them
    private final String[]pendingColors=new String[9];
    private final String[]pendingNames=new String[9];
    private EditBox[] nameFields;

    private int left;
    private int top;
    private int rows;


    private ItemStack[] icons;

    public ConfigScreen(Screen parent) {
        super(Component.literal("QuickTrigger — Configuration"));
        this.parent = parent;
        System.arraycopy(QuickTriggerConfig.INSTANCE.bedColors, 0, pendingColors,0,9);
        System.arraycopy(QuickTriggerConfig.INSTANCE.getNamesForServer(serverKey()), 0, pendingNames, 0, 9);
    }

    //opened from the main menu there's no server yet
    private static String serverKey() {

        return ClientState.currentServerKey != null ? ClientState.currentServerKey : "offline";
    }

    @Override
    protected void init() {
        rows = ClientState.maxHomes;
        left = this.width / 2 - TOTAL_WIDTH / 2;
        top= this.height / 2 -(rows * ROW_HEIGHT) / 2 - 20;

        icons =new ItemStack[rows];
        nameFields = new EditBox[rows];

        for (int i = 0; i < rows; i++) {
            final int idx=i;
            int y = top + i * ROW_HEIGHT;
            BedColor initial =BedColor.fromName(pendingColors[i]);
            icons[i] = safeStack(initial);

            CycleButton<BedColor> colorBtn= CycleButton
                .<BedColor>builder(color -> Component.translatable(color.item.getDescriptionId()),initial)
                .withValues(BedColor.values())
                .create(left, y, BTN_WIDTH, BTN_HEIGHT, Component.literal("Home #" + (i + 1)),
                    (button, value) ->{
                        pendingColors[idx] = value.name();
                        icons[idx]=safeStack(value);
                    });
            this.addRenderableWidget(colorBtn);

            EditBox nameField=new EditBox(this.font,left + BTN_WIDTH + GAP,y,NAME_WIDTH,BTN_HEIGHT,
                Component.literal("Nom du home #" +(i + 1)));
            nameField.setMaxLength(QuickTriggerConfig.MAX_NAME_LENGTH);


            nameField.setValue(pendingNames[i] == null ? "" : pendingNames[i]);
            nameField.setHint(Component.literal("Home #" + (i + 1)));
            nameField.setResponder(value -> pendingNames[idx]= value);
            this.addRenderableWidget(nameField);
            nameFields[i] = nameField;
        }

        int bottomY=top + rows * ROW_HEIGHT + 8;
        int halfW=(TOTAL_WIDTH - GAP)/ 2;

        this.addRenderableWidget(Button.builder(Component.literal("Sauvegarder"),btn -> {
                System.arraycopy(pendingColors, 0, QuickTriggerConfig.INSTANCE.bedColors, 0, 9);
                //write into the stored array itself, getNamesForServer hands back the live array
                String[] stored = QuickTriggerConfig.INSTANCE.getNamesForServer(serverKey());
                System.arraycopy(pendingNames,0,stored,0, 9);
                QuickTriggerConfig.INSTANCE.save();
                this.minecraft.setScreenAndShow(parent);


            })
            .pos(left,bottomY)
            .size(halfW, BTN_HEIGHT)
            .build());

        this.addRenderableWidget(Button.builder(Component.literal("Annuler"),btn -> this.minecraft.setScreenAndShow(parent))
            .pos(left + halfW + GAP, bottomY)
            .size(halfW,BTN_HEIGHT)
            .build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics,int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(this.font,this.title,this.width / 2, 20,0xFFFFFF);

        // bed preview sits right of the name box
        int iconX = left + BTN_WIDTH + GAP + NAME_WIDTH + GAP;
        for(int i =0; i < rows; i++) {
            graphics.item(icons[i], iconX, top + i * ROW_HEIGHT + (BTN_HEIGHT - ICON_SIZE) / 2);
        }
    }

    @Override
    public boolean shouldCloseOnEsc(){
        return true;
    }

    @Override
    public void onClose(){
        this.minecraft.setScreenAndShow(parent);
    }

    // item can come back null if the registry isnt ready yet, dont want the screen to blow up
    private static ItemStack safeStack(BedColor color){
        try{

            return new ItemStack(color.item);
        }catch (NullPointerException e) {
            return ItemStack.EMPTY;
        }
    }
}
