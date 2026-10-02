package com.refinedmods.refinedstorage.rei.fabric;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;

import dev.architectury.event.CompoundEventResult;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.registry.screen.FocusedStackProvider;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.client.gui.screens.Screen;

class ResourceFocusedStackProvider implements FocusedStackProvider {
    @Override
    public CompoundEventResult<EntryStack<?>> provide(final Screen screen, final Point mouse) {
        if (!(screen instanceof AbstractBaseScreen<?> baseScreen)) {
            return CompoundEventResult.pass();
        }
        final PlatformResourceKey hoveredResource = baseScreen.getHoveredResource();
        if (hoveredResource == null) {
            return CompoundEventResult.pass();
        }
        final Object converted = RefinedStorageApi.INSTANCE.getIngredientConverter()
            .convertToIngredient(hoveredResource)
            .orElse(null);
        if (converted instanceof EntryStack<?> stack) {
            return CompoundEventResult.interruptTrue(stack);
        }
        return CompoundEventResult.pass();
    }
}

