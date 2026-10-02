package com.refinedmods.refinedstorage.rei.forge;

import org.jspecify.annotations.Nullable;

import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.widgets.TextField;

public final class ReiHelper {
    private ReiHelper() {
    }

    @Nullable
    public static String getSearchFieldText() {
        final TextField field = REIRuntime.getInstance().getSearchTextField();
        if (field != null) {
            return field.getText();
        }
        return null;
    }

    public static void setSearchFieldText(final String text) {
        final TextField field = REIRuntime.getInstance().getSearchTextField();
        if (field != null) {
            field.setText(text);
        }
    }
}
