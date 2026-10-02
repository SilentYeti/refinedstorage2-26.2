package com.refinedmods.refinedstorage.rei.forge;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.grid.screen.AbstractGridScreen;

import dev.architectury.event.CompoundEventResult;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.registry.screen.FocusedStackProvider;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.client.gui.screens.Screen;

class GridFocusedStackProvider implements FocusedStackProvider {
    @Override
    public CompoundEventResult<EntryStack<?>> provide(final Screen screen, final Point mouse) {
        if (!(screen instanceof AbstractGridScreen<?> gridScreen)) {
            return CompoundEventResult.pass();
        }
        final GridResource resource = gridScreen.getCurrentGridResource();
        if (resource == null) {
            return CompoundEventResult.pass();
        }
        final PlatformResourceKey underlyingResource = resource.getAutocraftingResource();
        if (underlyingResource == null) {
            return CompoundEventResult.pass();
        }
        return provide(underlyingResource);
    }

    private CompoundEventResult<EntryStack<?>> provide(final PlatformResourceKey resource) {
        final Object converted = RefinedStorageApi.INSTANCE.getIngredientConverter()
            .convertToIngredient(resource)
            .orElse(null);
        if (converted instanceof EntryStack<?> stack) {
            return CompoundEventResult.interruptTrue(stack);
        }
        return CompoundEventResult.pass();
    }
}
