package mcjty.rftoolscontrol.blocks.processor;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.entity.GenericEnergyStorageTileEntity;
import mcjty.lib.gui.RenderHelper;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.WindowManager;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.network.Argument;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.network.RFToolsCtrlMessages;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;

public class GuiProcessor extends GenericGuiContainer<ProcessorTileEntity> {
    public static final int SIDEWIDTH = 80;
    public static final int WIDTH = 256;
    public static final int HEIGHT = 236;

    public static int ICONSIZE = 20;

    private static final ResourceLocation mainBackground = new ResourceLocation(RFToolsControl.MODID, "textures/gui/processor.png");
    private static final ResourceLocation sideBackground = new ResourceLocation(RFToolsControl.MODID, "textures/gui/sidegui.png");
    private static final ResourceLocation icons = new ResourceLocation(RFToolsControl.MODID, "textures/gui/icons.png");

    private Window sideWindow;
    private EnergyBar energyBar;
    private ToggleButton[] setupButtons = new ToggleButton[ProcessorTileEntity.CARD_SLOTS];
    private WidgetList log;

    public GuiProcessor(ProcessorTileEntity tileEntity, ProcessorContainer container) {
        super(RFToolsControl.instance, RFToolsCtrlMessages.INSTANCE, tileEntity, container, RFToolsControl.GUI_MANUAL_CONTROL, "processor");

        xSize = WIDTH;
        ySize = HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        // --- Main window ---
        Panel toplevel = new Panel(mc, this).setLayout(new PositionalLayout()).setBackground(mainBackground);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        int maxEnergyStored = tileEntity.getMaxEnergyStored(EnumFacing.DOWN);
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(new PositionalLayout.PositionalHint(122, 8, 70, 10)).setShowText(false).setHorizontal();
        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());
        toplevel.addChild(energyBar);

        setupLogWindow();
        toplevel.addChild(log);

        for (int i = 0; i < ProcessorTileEntity.CARD_SLOTS ; i++) {
            setupButtons[i] = new ToggleButton(mc, this)
                .addButtonEvent(this::setupMode)
                .setLayoutHint(new PositionalLayout.PositionalHint(11 + i * 18, 6, 15, 7));
            toplevel.addChild(setupButtons[i]);
        }
        window = new Window(this, toplevel);

        // --- Side window ---
        Panel listPanel = setupListPanel();
        Panel sidePanel = new Panel(mc, this).setLayout(new PositionalLayout()).setBackground(sideBackground)
                .addChild(listPanel);
        sidePanel.setBounds(new Rectangle(guiLeft-SIDEWIDTH, guiTop, SIDEWIDTH, ySize));
        sideWindow = new Window(this, sidePanel);
    }

    private void setupLogWindow() {
        log = new WidgetList(mc, this).setFilledBackground(0xff000000).setFilledRectThickness(2)
                .setLayoutHint(new PositionalLayout.PositionalHint(9, 40, 180, 110))
                .setRowheight(14)
                .setInvisibleSelection(true)
                .setDrawHorizontalLines(false);
        log.addChild(new Label(mc, this).setColor(0xff008800).setText("Processor booting...").setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT));
        log.addChild(new Label(mc, this).setColor(0xff008800).setText("Initializing memory... [OK]").setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT));
        log.addChild(new Label(mc, this).setColor(0xff008800).setText("Initializing items... [OK]").setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT));
        log.addChild(new Label(mc, this).setColor(0xff008800).setText("Entering card setup mode: 3").setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT));
    }

    private void setupMode(Widget parent) {
        ToggleButton tb = (ToggleButton) parent;
        if (tb.isPressed()) {
            for (ToggleButton button : setupButtons) {
                if (button != tb) {
                    button.setPressed(false);
                }
            }
        }
    }

    private int getSetupMode() {
        for (int i = 0 ; i < setupButtons.length ; i++) {
            if (setupButtons[i].isPressed()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        int setupMode = getSetupMode();
        if (setupMode == -1) {
            super.mouseClicked(x, y, button);
        } else {
            int leftx = window.getToplevel().getBounds().x;
            x -= leftx;
            if (x >= 10 && x <= 10 + ProcessorTileEntity.CARD_SLOTS*18 && y >= 6 && y <= 6+7) {
                super.mouseClicked(x + leftx, y, button);
            } else {
                CardInfo cardInfo = tileEntity.getCardInfo(setupMode);
                int itemAlloc = cardInfo.getItemAllocation();
                int varAlloc = cardInfo.getVarAllocation();

                for (int i = 0 ; i < ProcessorTileEntity.ITEM_SLOTS ; i++) {
                    Slot slot = inventorySlots.getSlot(ProcessorContainer.SLOT_BUFFER + i);
                    if (x >= slot.xDisplayPosition && x <= slot.xDisplayPosition + 17 && y >= slot.yDisplayPosition && y <= slot.yDisplayPosition + 17) {
                        boolean allocated = ((itemAlloc >> i) & 1) != 0;
                        allocated = !allocated;
                        if (allocated) {
                            itemAlloc = itemAlloc | (1 << i);
                        } else {
                            itemAlloc = itemAlloc & ~(1 << i);
                        }
                        cardInfo.setItemAllocation(itemAlloc);
                        sendServerCommand(RFToolsCtrlMessages.INSTANCE, ProcessorTileEntity.CMD_ALLOCATE,
                                new Argument("card", setupMode),
                                new Argument("items", itemAlloc),
                                new Argument("vars", varAlloc));
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected void registerWindows(WindowManager mgr) {
        super.registerWindows(mgr);
        mgr.addWindow(sideWindow);
        mgr.getIconManager().setClickHoldToDrag(true);
    }

    private Panel setupListPanel() {
        WidgetList list = new WidgetList(mc, this)
                .setLayoutHint(new PositionalLayout.PositionalHint(0, 0, 62, 220))
                .setPropagateEventsToChildren(true)
                .setInvisibleSelection(true)
                .setDrawHorizontalLines(false);
        Slider slider = new Slider(mc, this)
                .setVertical()
                .setScrollable(list)
                .setLayoutHint(new PositionalLayout.PositionalHint(62, 0, 9, 220));

        // @todo temporary
        for (int i = 0 ; i < 32 ; i++) {
            Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout()).setDesiredWidth(45);
            if (i >= 3 && i <= 6) {
                panel.setFilledBackground(0x7700ff00);
            }
            panel.addChild(new Label(mc, this).setText(String.valueOf(i)).setDesiredWidth(30));
            panel.addChild(new Button(mc, this).setText("..."));
            list.addChild(panel);
        }

        return new Panel(mc, this).setLayout(new PositionalLayout()).setLayoutHint(new PositionalLayout.PositionalHint(5, 5, 72, 220))
                .addChild(list)
                .addChild(slider);
//                .setFilledRectThickness(-2)
//                .setFilledBackground(StyleConfig.colorListBackground);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawWindow();

        int currentRF = GenericEnergyStorageTileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
        tileEntity.requestRfFromServer(RFToolsControl.MODID);

        drawAllocatedSlots();
    }

    private void drawAllocatedSlots() {
        int setupMode = getSetupMode();
        if (setupMode == -1) {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(guiLeft, guiTop, 0.0F);

        CardInfo cardInfo = tileEntity.getCardInfo(setupMode);
        int itemAlloc = cardInfo.getItemAllocation();

        for (int i = 0 ; i < ProcessorTileEntity.ITEM_SLOTS ; i++) {
            Slot slot = inventorySlots.getSlot(ProcessorContainer.SLOT_BUFFER + i);

            boolean allocated = ((itemAlloc >> i) & 1) != 0;
            int border = allocated ? 0xffffffff : 0xaaaaaaaa;
            int fill = allocated ? 0x7700ff00 : 0x77444444;
            RenderHelper.drawFlatBox(slot.xDisplayPosition, slot.yDisplayPosition, slot.xDisplayPosition + 17, slot.yDisplayPosition + 17,
                    border, fill);
        }

        GlStateManager.popMatrix();
    }
}