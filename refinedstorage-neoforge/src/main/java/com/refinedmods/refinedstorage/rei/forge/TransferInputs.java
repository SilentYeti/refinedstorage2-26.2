package com.refinedmods.refinedstorage.rei.forge;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

class TransferInputs {
    private final List<TransferInput> inputs = new ArrayList<>();
    private final Map<Integer, TransferInput> inputsBySlotIndex = new HashMap<>();

    void addInput(final int index, final TransferInput input) {
        inputs.add(input);
        inputsBySlotIndex.put(index, input);
    }

    List<ResourceAmount> createCraftingRequests() {
        final MutableResourceList requests = MutableResourceListImpl.orderPreserving();
        for (final TransferInput transferInput : inputs) {
            if (transferInput.type() == TransferInputType.AUTOCRAFTABLE
                && transferInput.autocraftableResource() != null) {
                requests.add(transferInput.autocraftableResource(), 1);
            }
        }
        return requests.copyState().stream().toList();
    }

    @Nullable
    TransferInput getInput(final int slotIndex) {
        return inputsBySlotIndex.get(slotIndex);
    }

    TransferType getType() {
        if (inputs.stream().allMatch(input -> input.type() == TransferInputType.AVAILABLE)) {
            return TransferType.AVAILABLE;
        }
        final boolean hasMissing = inputs.stream().anyMatch(input -> input.type() == TransferInputType.MISSING);
        final boolean hasAutocraftable = inputs.stream()
            .anyMatch(input -> input.type() == TransferInputType.AUTOCRAFTABLE);
        if (hasMissing && hasAutocraftable) {
            return TransferType.MISSING_BUT_SOME_AUTOCRAFTABLE;
        } else if (hasAutocraftable) {
            return TransferType.MISSING_BUT_ALL_AUTOCRAFTABLE;
        }
        return TransferType.MISSING;
    }
}
