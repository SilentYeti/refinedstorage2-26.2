package com.refinedmods.refinedstorage.rei.fabric;

import com.refinedmods.refinedstorage.common.content.ContentIds;
import com.refinedmods.refinedstorage.common.content.Items;
import com.refinedmods.refinedstorage.common.content.Tags;
import com.refinedmods.refinedstorage.common.controller.ControllerBlockItem;
import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;

import java.util.function.Supplier;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static com.refinedmods.refinedstorage.rei.common.Common.FULLY_CHARGED_CONTROLLER_GROUP_ID;
import static com.refinedmods.refinedstorage.rei.common.Common.FULLY_CHARGED_CONTROLLER_GROUP_NAME;

@Environment(EnvType.CLIENT)
public class ReiClientPlugin implements REIClientPlugin {
    @Override
    public void registerScreens(final ScreenRegistry registry) {
        registry.registerFocusedStack(new GridFocusedStackProvider());
        registry.registerFocusedStack(new ResourceFocusedStackProvider());
        registry.registerDraggableStackVisitor(new ResourceDraggableStackVisitor());
        registry.registerDraggableStackVisitor(new FilterDraggableStackVisitor());
    }

    @Override
    public void registerTransferHandlers(final TransferHandlerRegistry registry) {
        registry.register(new CraftingGridTransferHandler());
        registry.register(new PatternGridTransferHandler());
    }

    private Component tagName(final String name) {
        return createTranslation("tag.item", name);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void registerCollapsibleEntries(final CollapsibleEntryRegistry registry) {
        groupItems(registry, tagName("cables"), ContentIds.CABLE, Tags.CABLES);
        groupItems(registry, tagName("grids"), ContentIds.GRID, Tags.GRIDS);
        groupItems(registry, tagName("pattern_grids"), ContentIds.PATTERN_GRID, Tags.PATTERN_GRIDS);
        groupItems(registry, tagName("crafting_grids"), ContentIds.CRAFTING_GRID, Tags.CRAFTING_GRIDS);
        groupItems(registry, tagName("importers"), ContentIds.IMPORTER, Tags.IMPORTERS);
        groupItems(registry, tagName("exporters"), ContentIds.EXPORTER, Tags.EXPORTERS);
        groupItems(registry, tagName("detectors"), ContentIds.DETECTOR, Tags.DETECTORS);
        groupItems(registry, tagName("destructors"), ContentIds.DESTRUCTOR, Tags.DESTRUCTORS);
        groupItems(registry, tagName("constructors"), ContentIds.CONSTRUCTOR, Tags.CONSTRUCTORS);
        groupItems(registry, tagName("external_storages"), ContentIds.EXTERNAL_STORAGE, Tags.EXTERNAL_STORAGES);
        groupItems(registry, tagName("controllers"), ContentIds.CONTROLLER, Tags.CONTROLLERS);
        registry.group(
            FULLY_CHARGED_CONTROLLER_GROUP_ID,
            FULLY_CHARGED_CONTROLLER_GROUP_NAME,
            Items.INSTANCE.getControllers().stream()
                .map(Supplier::get)
                .map(ControllerBlockItem::createAtEnergyCapacity)
                .map(EntryStacks::of)
                .toList()
        );
        groupItems(
            registry,
            tagName("creative_controllers"),
            ContentIds.CREATIVE_CONTROLLER,
            Tags.CREATIVE_CONTROLLERS
        );
        groupItems(
            registry,
            tagName("wireless_transmitters"),
            ContentIds.WIRELESS_TRANSMITTER,
            Tags.WIRELESS_TRANSMITTERS
        );
        groupItems(registry, tagName("network_receivers"), ContentIds.NETWORK_RECEIVER, Tags.NETWORK_RECEIVERS);
        groupItems(
            registry,
            tagName("network_transmitters"),
            ContentIds.NETWORK_TRANSMITTER,
            Tags.NETWORK_TRANSMITTERS
        );
        groupItems(
            registry,
            tagName("security_managers"),
            ContentIds.SECURITY_MANAGER,
            Tags.SECURITY_MANAGERS
        );
        groupItems(
            registry,
            tagName("relays"),
            ContentIds.RELAY,
            Tags.RELAYS
        );
        groupItems(
            registry,
            tagName("disk_interfaces"),
            ContentIds.DISK_INTERFACE,
            Tags.DISK_INTERFACES
        );
        groupItems(
            registry,
            tagName("autocrafters"),
            ContentIds.AUTOCRAFTER,
            Tags.AUTOCRAFTERS
        );
        groupItems(
            registry,
            tagName("autocrafter_managers"),
            ContentIds.AUTOCRAFTER_MANAGER,
            Tags.AUTOCRAFTER_MANAGERS
        );
        groupItems(
            registry,
            tagName("autocrafting_monitors"),
            ContentIds.AUTOCRAFTING_MONITOR,
            Tags.AUTOCRAFTING_MONITORS
        );
    }

    @Override
    public void registerExclusionZones(final ExclusionZones zones) {
        zones.register(AbstractBaseScreen.class, new ExclusionZonesProviderImpl());
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void groupItems(
        final CollapsibleEntryRegistry registry,
        final Component name,
        final Identifier itemIdentifier,
        final TagKey<Item> tag
    ) {
        registry.group(itemIdentifier, name, EntryIngredients.ofItemTag(tag));
    }
}
