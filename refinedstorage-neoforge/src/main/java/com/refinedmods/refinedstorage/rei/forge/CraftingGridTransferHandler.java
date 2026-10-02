package com.refinedmods.refinedstorage.rei.forge;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepository;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.common.grid.AbstractCraftingGridContainerMenu;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.common.util.ClientPlatformUtil;

import java.awt.Color;
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
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.rei.common.Common.MOD_ID;
import static java.util.Comparator.comparingLong;

class CraftingGridTransferHandler extends AbstractTransferHandler {
    private static final Color MISSING_COLOR = new Color(1.0f, 0.0f, 0.0f, 0.4f);
    private static final Component MISSING = Component.translatable("gui.%s.transfer.missing".formatted(MOD_ID))
        .withStyle(ChatFormatting.RED);
    private static final Component CTRL_CLICK_TO_AUTOCRAFT = Component
        .translatable("gui.%s.transfer.ctrl_click_to_autocraft".formatted(MOD_ID))
        .withStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.WHITE));
    private static final Component MISSING_BUT_ALL_AUTOCRAFTABLE = createAutocraftableHint(
        Component.translatable("gui.%s.transfer.missing_but_all_autocraftable".formatted(MOD_ID))
    ).append("\n").append(CTRL_CLICK_TO_AUTOCRAFT);
    private static final Component MISSING_BUT_AUTOCRAFTABLE = createAutocraftableHint(
        Component.translatable("gui.%s.transfer.missing_but_some_autocraftable".formatted(MOD_ID))
    ).append("\n").append(CTRL_CLICK_TO_AUTOCRAFT);

    @Override
    public Result handle(final Context context) {
        if (!(context.getMenu() instanceof AbstractCraftingGridContainerMenu containerMenu)
            || !context.getDisplay().getCategoryIdentifier().equals(BuiltinPlugin.CRAFTING)
            || !(context.getDisplay() instanceof DefaultCraftingDisplay defaultCraftingDisplay)) {
            return Result.createNotApplicable();
        }
        final List<EntryIngredient> ingredients = defaultCraftingDisplay.getOrganisedInputEntries(3, 3);
        final MutableResourceList available = containerMenu.getAvailableListForRecipeTransfer();
        final ResourceRepository<GridResource> repository = containerMenu.getRepository();
        final TransferInputs transferInputs = getTransferInputs(repository, ingredients, available);
        final TransferType type = transferInputs.getType();
        if (context.isActuallyCrafting()) {
            return doActuallyCrafting(context, containerMenu, type, transferInputs, ingredients);
        }
        if (type == TransferType.AVAILABLE) {
            return Result.createSuccessful().blocksFurtherHandling();
        }
        return Result.createSuccessful()
            .color(getColor(type))
            .tooltip(getTooltip(type))
            .renderer(createTransferInputsRenderer(transferInputs))
            .blocksFurtherHandling();
    }

    private Result doActuallyCrafting(final Context context, final AbstractCraftingGridContainerMenu containerMenu,
                                      final TransferType type, final TransferInputs transferInputs,
                                      final List<EntryIngredient> ingredients) {
        if (type.canOpenAutocraftingPreview() && ClientPlatformUtil.isCommandOrControlDown()) {
            final List<ResourceAmount> craftingRequests = transferInputs.createCraftingRequests();
            RefinedStorageClientApi.INSTANCE.openAutocraftingPreview(craftingRequests, context.getContainerScreen());
            return Result.createSuccessful().blocksFurtherHandling(false);
        } else {
            doTransfer(ingredients, containerMenu);
        }
        return Result.createSuccessful().blocksFurtherHandling();
    }

    private static int getColor(final TransferType type) {
        return switch (type) {
            case MISSING -> MISSING_COLOR.getRGB();
            case MISSING_BUT_ALL_AUTOCRAFTABLE, MISSING_BUT_SOME_AUTOCRAFTABLE -> AUTOCRAFTABLE_COLOR;
            default -> 0;
        };
    }

    private static Component getTooltip(final TransferType type) {
        return switch (type) {
            case MISSING -> MISSING;
            case MISSING_BUT_ALL_AUTOCRAFTABLE -> MISSING_BUT_ALL_AUTOCRAFTABLE;
            case MISSING_BUT_SOME_AUTOCRAFTABLE -> MISSING_BUT_AUTOCRAFTABLE;
            default -> Component.empty();
        };
    }

    private void doTransfer(final List<EntryIngredient> ingredients,
                            final AbstractCraftingGridContainerMenu containerMenu) {
        final List<List<ItemResource>> inputs = getInputs(ingredients);
        containerMenu.transferRecipe(inputs);
    }

    private TransferInputs getTransferInputs(final ResourceRepository<GridResource> repository,
                                             final List<EntryIngredient> ingredients,
                                             final MutableResourceList available) {
        final TransferInputs transferInputs = new TransferInputs();
        for (int i = 0; i < ingredients.size(); ++i) {
            final EntryIngredient ingredient = ingredients.get(i);
            if (ingredient.isEmpty()) {
                continue;
            }
            final TransferInput transferInput = toTransferInput(repository, available, ingredient);
            transferInputs.addInput(i, transferInput);
        }
        return transferInputs;
    }

    private TransferInput toTransferInput(final ResourceRepository<GridResource> repository,
                                          final MutableResourceList available,
                                          final EntryIngredient ingredient) {
        final List<ItemStack> possibilities = convertIngredientToItemStacks(ingredient);
        for (final ItemStack possibility : possibilities) {
            final ItemResource possibilityResource = ItemResource.ofItemStack(possibility);
            if (available.remove(possibilityResource, 1) != null) {
                return new TransferInput(ingredient, TransferInputType.AVAILABLE, null);
            }
        }
        final List<ItemResource> autocraftingPossibilities = possibilities
            .stream()
            .map(ItemResource::ofItemStack)
            .filter(repository::isSticky)
            .sorted(comparingLong(repository::getAmount))
            .toList();
        if (!autocraftingPossibilities.isEmpty()) {
            return new TransferInput(ingredient, TransferInputType.AUTOCRAFTABLE, autocraftingPossibilities.getFirst());
        }
        return new TransferInput(ingredient, TransferInputType.MISSING, null);
    }

    private List<List<ItemResource>> getInputs(final List<EntryIngredient> ingredients) {
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

    private TransferHandlerRenderer createTransferInputsRenderer(final TransferInputs transferInputs) {
        return (graphics, mouseX, mouseY, delta, widgets, bounds, display) -> {
            int index = 0;
            for (final Widget widget : widgets) {
                if (widget instanceof Slot slot && slot.getNoticeMark() == Slot.INPUT) {
                    final TransferInput input = transferInputs.getInput(index++);
                    if (input == null) {
                        continue;
                    }
                    if (input.type() == TransferInputType.MISSING) {
                        renderSlotHighlight(graphics, slot, MISSING_COLOR.getRGB());
                    } else if (input.type() == TransferInputType.AUTOCRAFTABLE) {
                        renderSlotHighlight(graphics, slot, AUTOCRAFTABLE_COLOR);
                    }
                }
            }
        };
    }
}
