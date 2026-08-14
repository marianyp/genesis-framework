package dev.mariany.genesisframework.event.client.recipe;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

@Environment(EnvType.CLIENT)
public final class ClientRecipeEvents {
    private ClientRecipeEvents() {
    }

    public static final Event<ModifyDisplayItems> MODIFY_DISPLAY_ITEMS = EventFactory.createArrayBacked(
            ModifyDisplayItems.class,
            callbacks -> items -> {
                List<ItemStack> modifiedItems = items;

                for (ModifyDisplayItems callback : callbacks) {
                    modifiedItems = callback.modifyDisplayItems(modifiedItems);
                }

                return modifiedItems;
            }
    );

    public static final Event<ModifyCollections> MODIFY_COLLECTIONS = EventFactory.createArrayBacked(
            ModifyCollections.class,
            callbacks -> (collections, level) -> {
                List<RecipeCollection> modifiedCollections = collections;

                for (ModifyCollections callback : callbacks) {
                    modifiedCollections = callback.modifyCollections(modifiedCollections, level);
                }

                return modifiedCollections;
            }
    );

    public static final Event<AllowToast> ALLOW_TOAST = EventFactory.createArrayBacked(
            AllowToast.class,
            callbacks -> item -> {
                for (AllowToast callback : callbacks) {
                    if (callback.test(item)) {
                        return true;
                    }
                }

                return false;
            }
    );

    public static final Event<AllowToastRender> ALLOW_TOAST_RENDER = EventFactory.createArrayBacked(
            AllowToastRender.class,
            callbacks -> entries -> {
                for (AllowToastRender callback : callbacks) {
                    if (!callback.allowToastRender(entries)) {
                        return false;
                    }
                }

                return true;
            }
    );

    public static final Event<ModifyButtonEntries> MODIFY_BUTTON_ENTRIES = EventFactory.createArrayBacked(
            ModifyButtonEntries.class,
            callbacks -> entries -> {
                List<RecipeButton.ResolvedEntry> modifiedEntries = entries;

                for (ModifyButtonEntries callback : callbacks) {
                    modifiedEntries = callback.modifyButtonEntries(modifiedEntries);
                }

                return modifiedEntries;
            }
    );

    public interface ModifyDisplayItems {
        List<ItemStack> modifyDisplayItems(List<ItemStack> items);
    }

    public interface ModifyCollections {
        List<RecipeCollection> modifyCollections(List<RecipeCollection> collections, Level level);
    }

    public interface AllowToast {
        boolean test(ItemStack item);
    }

    public interface AllowToastRender {
        boolean allowToastRender(List<RecipeToast.Entry> entries);
    }

    public interface ModifyButtonEntries {
        List<RecipeButton.ResolvedEntry> modifyButtonEntries(List<RecipeButton.ResolvedEntry> entries);
    }
}
