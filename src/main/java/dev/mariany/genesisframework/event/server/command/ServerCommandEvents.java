package dev.mariany.genesisframework.event.server.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.concurrent.CompletableFuture;
import java.util.Optional;

public final class ServerCommandEvents {
    private ServerCommandEvents() {
    }

    public static final Event<SuggestRegistryElements> SUGGEST_REGISTRY_ELEMENTS = EventFactory.createArrayBacked(
            SuggestRegistryElements.class,
            callbacks -> (key, suggestionType, builder, context) -> {
                for (SuggestRegistryElements callback : callbacks) {
                    Optional<CompletableFuture<Suggestions>> suggestions = callback.suggestRegistryElements(
                            key,
                            suggestionType,
                            builder,
                            context
                    );

                    if (suggestions.isPresent()) {
                        return suggestions;
                    }
                }

                return Optional.empty();
            }
    );

    public interface SuggestRegistryElements {
        Optional<CompletableFuture<Suggestions>> suggestRegistryElements(
                ResourceKey<? extends Registry<?>> key,
                SharedSuggestionProvider.ElementSuggestionType suggestionType,
                SuggestionsBuilder builder,
                CommandContext<?> context
        );
    }
}
