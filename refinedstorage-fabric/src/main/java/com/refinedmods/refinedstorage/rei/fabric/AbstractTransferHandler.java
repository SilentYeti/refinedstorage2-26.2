package com.refinedmods.refinedstorage.rei.fabric;

import com.refinedmods.refinedstorage.common.grid.AutocraftableResourceHint;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

abstract class AbstractTransferHandler implements TransferHandler {
    protected static final int AUTOCRAFTABLE_COLOR = AutocraftableResourceHint.AUTOCRAFTABLE.getColor();

    protected static MutableComponent createAutocraftableHint(final Component component) {
        return component.copy().withColor(AUTOCRAFTABLE_COLOR);
    }

    protected static void renderSlotHighlight(final GuiGraphicsExtractor graphics, final Slot slot, final int color) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(0, 0);
        final Rectangle innerBounds = slot.getInnerBounds();
        graphics.fill(
            innerBounds.x,
            innerBounds.y,
            innerBounds.getMaxX(),
            innerBounds.getMaxY(),
            color
        );
        graphics.pose().popMatrix();
    }
}
