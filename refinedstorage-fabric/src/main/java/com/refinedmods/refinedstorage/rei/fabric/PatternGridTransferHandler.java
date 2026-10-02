package com.refinedmods.refinedstorage.rei.fabric;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepository;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridContainerMenu;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.List;
import java.util.stream.Collectors;

import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRenderer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCraftingDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.rei.common.Common.MOD_ID;

class PatternGridTransferHandler extends AbstractTransferHandler {
    private static final Component ALL_AUTOCRAFTABLE = createAutocraftableHint(
        Component.translatable("gui.%s.transfer.all_autocraftable".formatted(MOD_ID))
    );
    private static final Component SOME_AUTOCRAFTABLE = createAutocraftableHint(
        Component.translatable("gui.%s.transfer.some_autocraftable".formatted(MOD_ID))
    );

    @Override
    public Result handle(final Context context) {
        if (!(context.getMenu() instanceof PatternGridContainerMenu containerMenu)) {
            return Result.createNotApplicable();
        }
        final ResourceRepository<GridResource> repository = containerMenu.getRepository();
        final List<EntryIngredient> inputs = context.getDisplay()
            .getInputEntries()
            .stream()
            .filter(ingredient -> !ingredient.isEmpty())
            .toList();
        final List<EntryIngredient> autocraftable = getAutocraftableIngredients(repository, inputs);
        if (!transferRegularRecipe(context, containerMenu)) {
            transferProcessingRecipe(context, containerMenu);
        }
        final Result result = Result.createSuccessful();
        if (!autocraftable.isEmpty()) {
            final boolean areAllAutocraftable = inputs.size() == autocraftable.size();
            if (areAllAutocraftable) {
                result.tooltip(ALL_AUTOCRAFTABLE);
            } else {
                result.tooltip(SOME_AUTOCRAFTABLE);
            }
            result.color(AUTOCRAFTABLE_COLOR);
            result.renderer(createAutocraftableRenderer(autocraftable));
        }
        return result.blocksFurtherHandling();
    }

    private boolean transferRegularRecipe(final Context context, final PatternGridContainerMenu containerMenu) {
        if (context.getDisplay().getCategoryIdentifier().equals(BuiltinPlugin.CRAFTING)
            && context.getDisplay() instanceof DefaultCraftingDisplay defaultCraftingDisplay) {
            transferCraftingRecipe(context, containerMenu, defaultCraftingDisplay);
            return true;
        } else if (context.getDisplay().getCategoryIdentifier().equals(BuiltinPlugin.STONE_CUTTING)) {
            transferStonecutterRecipe(context, containerMenu);
            return true;
        } else if (context.getDisplay().getCategoryIdentifier().equals(BuiltinPlugin.SMITHING)) {
            transferSmithingTableRecipe(context, containerMenu);
            return true;
        }
        return false;
    }

    private void transferCraftingRecipe(final Context context,
                                        final PatternGridContainerMenu containerMenu,
                                        final DefaultCraftingDisplay display) {
        final List<List<ItemResource>> inputs = getItems(
            display.getOrganisedInputEntries(3, 3)
        );
        if (context.isActuallyCrafting()) {
            containerMenu.transferCraftingRecipe(inputs);
        }
    }

    private void transferStonecutterRecipe(final Context context, final PatternGridContainerMenu menu) {
        final List<List<ItemResource>> inputs = getItems(context.getDisplay().getInputEntries());
        final List<List<ItemResource>> outputs = getItems(context.getDisplay().getOutputEntries());
        final boolean valid = !inputs.isEmpty()
            && !outputs.isEmpty()
            && !inputs.getFirst().isEmpty()
            && !outputs.getFirst().isEmpty();
        if (context.isActuallyCrafting() && valid) {
            menu.transferStonecutterRecipe(inputs.getFirst().getFirst(), outputs.getFirst().getFirst());
        }
    }

    private void transferSmithingTableRecipe(final Context context, final PatternGridContainerMenu menu) {
        final List<List<ItemResource>> inputs = getItems(context.getDisplay().getInputEntries());
        if (context.isActuallyCrafting() && inputs.size() == 3) {
            menu.transferSmithingTableRecipe(inputs.get(0), inputs.get(1), inputs.get(2));
        }
    }

    private void transferProcessingRecipe(final Context context, final PatternGridContainerMenu menu) {
        final List<List<ResourceAmount>> inputs = getResources(context.getDisplay().getInputEntries());
        final List<List<ResourceAmount>> outputs = getResources(context.getDisplay().getOutputEntries());
        if (context.isActuallyCrafting()) {
            menu.transferProcessingRecipe(inputs, outputs);
        }
    }

    private List<List<ItemResource>> getItems(final List<EntryIngredient> ingredients) {
        return ingredients.stream()
            .map(this::convertIngredientToItemStacks)
            .map(list -> list.stream().map(ItemResource::ofItemStack).collect(Collectors.toList()))
            .toList();
    }

    private List<ItemStack> convertIngredientToItemStacks(final EntryIngredient ingredient) {
        return CollectionUtils.<EntryStack<?>, ItemStack>filterAndMap(
            ingredient,
            stack -> stack.getType() == VanillaEntryTypes.ITEM,
            EntryStack::castValue
        );
    }

    private List<List<ResourceAmount>> getResources(final List<EntryIngredient> ingredients) {
        return ingredients.stream()
            .map(this::getResources)
            .toList();
    }

    private List<ResourceAmount> getResources(final EntryIngredient ingredient) {
        return ingredient.stream()
            .flatMap(
                item -> RefinedStorageApi.INSTANCE.getIngredientConverter().convertToResourceAmount(item.getValue())
                    .stream())
            .collect(Collectors.toList());
    }

    private List<EntryIngredient> getAutocraftableIngredients(final ResourceRepository<GridResource> repository,
                                                              final List<EntryIngredient> ingredients) {
        return ingredients.stream().filter(ingredient -> getResources(ingredient)
                .stream()
                .map(ResourceAmount::resource)
                .anyMatch(repository::isSticky))
            .toList();
    }

    private TransferHandlerRenderer createAutocraftableRenderer(final List<EntryIngredient> autocraftableIngredients) {
        return (graphics, mouseX, mouseY, delta, widgets, bounds, display) -> {
            for (final Widget widget : widgets) {
                if (widget instanceof Slot slot && slot.getNoticeMark() == Slot.INPUT) {
                    final boolean autocraftable = autocraftableIngredients.stream()
                        .anyMatch(ingredient -> ingredient.stream().anyMatch(ing -> slot.getEntries().contains(ing)));
                    if (autocraftable) {
                        renderSlotHighlight(graphics, slot, AUTOCRAFTABLE_COLOR);
                    }
                }
            }
        };
    }
}
