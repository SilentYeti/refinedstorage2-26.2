package com.refinedmods.refinedstorage.rei.forge;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.support.AbstractBaseContainerMenu;
import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage.common.support.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlot;
import com.refinedmods.refinedstorage.common.support.packet.c2s.C2SPackets;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitor;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.Slot;

class ResourceDraggableStackVisitor
    implements DraggableStackVisitor<AbstractBaseScreen<? extends AbstractBaseContainerMenu>> {
    @Override
    public Stream<BoundsProvider> getDraggableAcceptingBounds(
        final DraggingContext<AbstractBaseScreen<? extends AbstractBaseContainerMenu>> context,
        final DraggableStack stack
    ) {
        final var screen = context.getScreen();
        final var menu = screen.getMenu();
        final var value = stack.getStack().getValue();
        final List<BoundsProvider> bounds = new ArrayList<>();
        if (menu instanceof AbstractResourceContainerMenu resourceMenu) {
            addSlotBounds(resourceMenu, value, bounds, screen);
        }
        return bounds.stream();
    }

    private void addSlotBounds(
        final AbstractResourceContainerMenu resourceMenu,
        final Object value,
        final List<BoundsProvider> bounds,
        final AbstractBaseScreen<? extends AbstractBaseContainerMenu> screen
    ) {
        RefinedStorageApi.INSTANCE.getIngredientConverter().convertToResource(value).ifPresent(resource -> {
            for (final ResourceSlot slot : resourceMenu.getResourceSlots()) {
                if (isValid(resource, slot)) {
                    bounds.add(BoundsProvider.ofRectangle(toRectangle(screen, slot)));
                }
            }
        });
    }

    @Override
    public DraggedAcceptorResult acceptDraggedStack(
        final DraggingContext<AbstractBaseScreen<? extends AbstractBaseContainerMenu>> context,
        final DraggableStack stack
    ) {
        final var screen = context.getScreen();
        final var menu = screen.getMenu();
        final Object stackValue = stack.getStack().getValue();
        return RefinedStorageApi.INSTANCE.getIngredientConverter().convertToResource(stackValue)
            .map(resource -> accept(context, menu, screen, resource))
            .orElse(DraggedAcceptorResult.PASS);
    }

    private DraggedAcceptorResult accept(
        final DraggingContext<AbstractBaseScreen<? extends AbstractBaseContainerMenu>> context,
        final AbstractBaseContainerMenu menu,
        final AbstractBaseScreen<? extends AbstractBaseContainerMenu> screen,
        final PlatformResourceKey resource
    ) {
        if (menu instanceof AbstractResourceContainerMenu resourceMenu) {
            for (final ResourceSlot slot : resourceMenu.getResourceSlots()) {
                final Rectangle slotBounds = toRectangle(screen, slot);
                if (isValid(resource, slot) && slotBounds.contains(context.getCurrentPosition())) {
                    C2SPackets.sendResourceFilterSlotChange(resource, slot.index);
                    return DraggedAcceptorResult.ACCEPTED;
                }
            }
        }
        return DraggedAcceptorResult.PASS;
    }

    private static boolean isValid(final PlatformResourceKey resource, final ResourceSlot slot) {
        return slot.isFilter() && slot.isActive() && slot.isValid(resource);
    }

    private static Rectangle toRectangle(final AbstractBaseScreen<? extends AbstractBaseContainerMenu> screen,
                                         final Slot slot) {
        return new Rectangle(screen.getLeftPos() + slot.x, screen.getTopPos() + slot.y, 18, 18);
    }

    @Override
    public <R extends Screen> boolean isHandingScreen(final R screen) {
        return screen instanceof AbstractBaseScreen<?>
            && ((AbstractBaseScreen<?>) screen).getMenu() instanceof AbstractResourceContainerMenu;
    }
}
