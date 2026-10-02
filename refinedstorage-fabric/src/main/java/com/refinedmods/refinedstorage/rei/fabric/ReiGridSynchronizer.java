package com.refinedmods.refinedstorage.rei.fabric;

import com.refinedmods.refinedstorage.common.api.grid.GridSynchronizer;
import com.refinedmods.refinedstorage.common.grid.NoopGridSynchronizer;

import org.jspecify.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.rei.common.Common.SYNCHRONIZER_HELP;
import static com.refinedmods.refinedstorage.rei.common.Common.SYNCHRONIZER_TITLE;
import static com.refinedmods.refinedstorage.rei.common.Common.TWO_WAY_SYNCHRONIZER_HELP;
import static com.refinedmods.refinedstorage.rei.common.Common.TWO_WAY_SYNCHRONIZER_TITLE;

public class ReiGridSynchronizer implements GridSynchronizer {
    private final boolean twoWay;

    public ReiGridSynchronizer(final boolean twoWay) {
        this.twoWay = twoWay;
    }

    @Override
    public MutableComponent getTitle() {
        return twoWay ? TWO_WAY_SYNCHRONIZER_TITLE : SYNCHRONIZER_TITLE;
    }

    @Override
    public Component getHelpText() {
        return twoWay ? TWO_WAY_SYNCHRONIZER_HELP : SYNCHRONIZER_HELP;
    }

    @Override
    public void synchronizeFromGrid(final String text) {
        ReiHelper.setSearchFieldText(text);
    }

    @Override
    @Nullable
    public String getTextToSynchronizeToGrid() {
        return twoWay ? ReiHelper.getSearchFieldText() : null;
    }

    @Override
    public Identifier getSprite() {
        return twoWay ? NoopGridSynchronizer.ON_TWO_WAY : NoopGridSynchronizer.ON;
    }
}
