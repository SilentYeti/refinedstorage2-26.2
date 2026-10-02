package com.refinedmods.refinedstorage.rei.forge;

import com.refinedmods.refinedstorage.common.support.AbstractBaseContainerMenu;
import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage.common.support.containermenu.FilterSlot;
import com.refinedmods.refinedstorage.common.support.packet.c2s.C2SPackets;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitor;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

class FilterDraggableStackVisitor
    implements DraggableStackVisitor<AbstractBaseScreen<? extends AbstractBaseContainerMenu>> {
    @Override
    public Stream<BoundsProvider> getDraggableAcceptingBounds(
        final DraggingContext<AbstractBaseScreen<? extends AbstractBaseContainerMenu>> context,
        final DraggableStack stack
    ) {
        final var screen = context.getScreen();
        final var menu = screen.getMenu();
        final List<BoundsProvider> bounds = new ArrayList<>();
        if (stack.getStack().getType() == VanillaEntryTypes.ITEM) {
            addSlotBounds(stack, menu, bounds, screen);
        }
        return bounds.stream();
    }

    private void addSlotBounds(
        final DraggableStack stack,
        final AbstractBaseContainerMenu menu,
        final List<BoundsProvider> bounds,
        final AbstractBaseScreen<? extends AbstractBaseContainerMenu> screen
    ) {
        final ItemStack itemStack = (ItemStack) stack.getStack().getValue();
        for (final Slot slot : menu.slots) {
            if (slot instanceof FilterSlot filterSlot && isValid(itemStack, filterSlot)) {
                bounds.add(BoundsProvider.ofRectangle(toRectangle(screen, slot)));
            }
        }
    }

    @Override
    public DraggedAcceptorResult acceptDraggedStack(
        final DraggingContext<AbstractBaseScreen<? extends AbstractBaseContainerMenu>> context,
        final DraggableStack stack
    ) {
        final var screen = context.getScreen();
        final var menu = screen.getMenu();
        if (stack.getStack().getValue() instanceof ItemStack itemStack) {
            return accept(context, menu, screen, itemStack);
        }
        return DraggedAcceptorResult.PASS;
    }

    private DraggedAcceptorResult accept(
        final DraggingContext<AbstractBaseScreen<? extends AbstractBaseContainerMenu>> context,
        final AbstractBaseContainerMenu menu,
        final AbstractBaseScreen<? extends AbstractBaseContainerMenu> screen,
        final ItemStack stack
    ) {
        for (final Slot slot : menu.slots) {
            if (slot instanceof FilterSlot filterSlot && isValid(stack, filterSlot)) {
                final Rectangle slotBounds = toRectangle(screen, slot);
                if (!slotBounds.contains(context.getCurrentPosition())) {
                    continue;
                }
                C2SPackets.sendFilterSlotChange(stack, filterSlot.index);
                return DraggedAcceptorResult.ACCEPTED;
            }
        }
        return DraggedAcceptorResult.PASS;
    }

    private static boolean isValid(final ItemStack stack, final FilterSlot slot) {
        return slot.isActive() && slot.mayPlace(stack);
    }

    private static Rectangle toRectangle(final AbstractBaseScreen<? extends AbstractBaseContainerMenu> screen,
                                         final Slot slot) {
        return new Rectangle(screen.getLeftPos() + slot.x, screen.getTopPos() + slot.y, 18, 18);
    }

    @Override
    public <R extends Screen> boolean isHandingScreen(final R screen) {
        return screen instanceof AbstractBaseScreen<?>
            && ((AbstractBaseScreen<?>) screen).getMenu() instanceof AbstractBaseContainerMenu;
    }
}
