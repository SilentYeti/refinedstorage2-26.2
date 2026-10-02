package com.refinedmods.refinedstorage.rei.fabric;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.fabric.api.RefinedStoragePlugin;

import static com.refinedmods.refinedstorage.rei.common.Common.SYNCHRONIZER_ID;
import static com.refinedmods.refinedstorage.rei.common.Common.TWO_WAY_SYNCHRONIZER_ID;

public class ReiRefinedStoragePlugin implements RefinedStoragePlugin {
    @Override
    public void onApiAvailable(final RefinedStorageApi api) {
        api.addIngredientConverter(new ReiRecipeModIngredientConverter());
        api.getGridSynchronizerRegistry().register(SYNCHRONIZER_ID, new ReiGridSynchronizer(false));
        api.getGridSynchronizerRegistry().register(TWO_WAY_SYNCHRONIZER_ID, new ReiGridSynchronizer(true));
    }
}
