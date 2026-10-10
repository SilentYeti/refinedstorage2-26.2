package com.refinedmods.refinedstorage.jei.fabric;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.fabric.api.RefinedStoragePlugin;

import net.fabricmc.loader.api.FabricLoader;

import static com.refinedmods.refinedstorage.jei.common.Common.init;

public class JeiRefinedStoragePlugin implements RefinedStoragePlugin {
    @Override
    public void onApiAvailable(final RefinedStorageApi api) {
        if (!FabricLoader.getInstance().isModLoaded("jei")) {
            return;
        }
        init(api);
    }
}
