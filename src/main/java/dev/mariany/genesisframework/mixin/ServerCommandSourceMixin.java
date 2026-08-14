package dev.mariany.genesisframework.mixin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.mariany.genesisframework.event.server.command.ServerCommandEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.Optional;

@Mixin(CommandSourceStack.class)
public class ServerCommandSourceMixin {
    /**
     * Queries {@link ServerCommandEvents#SUGGEST_REGISTRY_ELEMENTS} before generating registry suggestions.
     */
    @Inject(
            method = "suggestRegistryElements(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/commands/SharedSuggestionProvider$ElementSuggestionType;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;Lcom/mojang/brigadier/context/CommandContext;)Ljava/util/concurrent/CompletableFuture;",
            at = @At(value = "HEAD"), cancellable = true
    )
    public void injectListIdSuggestions(
            ResourceKey<? extends Registry<?>> key,
            SharedSuggestionProvider.ElementSuggestionType suggestedIdType,
            SuggestionsBuilder builder,
            CommandContext<?> context,
            CallbackInfoReturnable<CompletableFuture<Suggestions>> cir
    ) {
        Optional<CompletableFuture<Suggestions>> suggestions = ServerCommandEvents.SUGGEST_REGISTRY_ELEMENTS
                .invoker()
                .suggestRegistryElements(key, suggestedIdType, builder, context);

        if (suggestions.isEmpty()) {
            return;
        }

        cir.setReturnValue(suggestions.get());
    }
}
