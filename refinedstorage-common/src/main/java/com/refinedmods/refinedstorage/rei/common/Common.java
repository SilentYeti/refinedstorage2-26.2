package com.refinedmods.refinedstorage.rei.common;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

public final class Common {
    public static final String MOD_ID = "refinedstorage_rei_integration";

    public static final Identifier SYNCHRONIZER_ID = Identifier.fromNamespaceAndPath(
        MOD_ID,
        "rei"
    );
    public static final MutableComponent SYNCHRONIZER_TITLE = Component.translatable(
        "gui.%s.grid.synchronizer".formatted(MOD_ID)
    );
    public static final Component SYNCHRONIZER_HELP = Component.translatable(
        "gui.%s.grid.synchronizer.help".formatted(MOD_ID)
    );

    public static final Identifier TWO_WAY_SYNCHRONIZER_ID = Identifier.fromNamespaceAndPath(
        MOD_ID,
        "rei_two_way"
    );
    public static final MutableComponent TWO_WAY_SYNCHRONIZER_TITLE = Component.translatable(
        "gui.%s.grid.synchronizer.two_way".formatted(MOD_ID)
    );
    public static final Component TWO_WAY_SYNCHRONIZER_HELP = Component.translatable(
        "gui.%s.grid.synchronizer.two_way.help".formatted(MOD_ID)
    );

    public static final Identifier FULLY_CHARGED_CONTROLLER_GROUP_ID = Identifier.fromNamespaceAndPath(
        MOD_ID,
        "fully_charged_controller"
    );
    public static final Component FULLY_CHARGED_CONTROLLER_GROUP_NAME = Component.translatable(
        "block.%s.controller.fully_charged".formatted(MOD_ID)
    );

    private Common() {
    }
}
