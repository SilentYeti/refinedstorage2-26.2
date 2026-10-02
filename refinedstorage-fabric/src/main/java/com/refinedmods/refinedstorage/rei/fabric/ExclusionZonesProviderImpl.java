package com.refinedmods.refinedstorage.rei.fabric;

import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;

import java.util.Collection;
import java.util.stream.Collectors;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZonesProvider;

class ExclusionZonesProviderImpl implements ExclusionZonesProvider<AbstractBaseScreen<?>> {
    @Override
    public Collection<Rectangle> provide(final AbstractBaseScreen<?> screen) {
        return screen.getExclusionZones().stream()
            .map(rect -> new Rectangle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()))
            .collect(Collectors.toSet());
    }
}
