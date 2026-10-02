package com.refinedmods.refinedstorage.rei.fabric;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.RecipeModIngredientConverter;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.Optional;

import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.world.item.ItemStack;

class ReiRecipeModIngredientConverter implements RecipeModIngredientConverter {
    @Override
    public Optional<PlatformResourceKey> convertToResource(final Object ingredient) {
        if (ingredient instanceof FluidStack fluidStack) {
            return Optional.of(new FluidResource(fluidStack.getFluid(), fluidStack.getPatch()));
        }
        if (ingredient instanceof ItemStack itemStack) {
            return Optional.of(ItemResource.ofItemStack(itemStack));
        }
        return Optional.empty();
    }

    @Override
    public Optional<ResourceAmount> convertToResourceAmount(final Object ingredient) {
        if (ingredient instanceof FluidStack fluidStack) {
            return Optional.of(new ResourceAmount(
                new FluidResource(fluidStack.getFluid(), fluidStack.getPatch()),
                fluidStack.getAmount()
            ));
        }
        if (ingredient instanceof ItemStack itemStack) {
            return Optional.of(new ResourceAmount(
                ItemResource.ofItemStack(itemStack),
                itemStack.getCount()
            ));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Object> convertToIngredient(final PlatformResourceKey resource) {
        if (resource instanceof ItemResource itemResource) {
            return Optional.of(EntryStacks.of(itemResource.toItemStack()));
        }
        if (resource instanceof FluidResource fluidResource) {
            final FluidStack fluidStack = FluidStack.create(
                fluidResource.fluid(),
                FluidStack.bucketAmount(),
                fluidResource.components()
            );
            return Optional.of(EntryStacks.of(fluidStack));
        }
        return Optional.empty();
    }
}
