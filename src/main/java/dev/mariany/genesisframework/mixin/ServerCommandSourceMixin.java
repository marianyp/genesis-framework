package dev.mariany.genesisframework.mixin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.mariany.genesisframework.age.AgeEntry;
import dev.mariany.genesisframework.age.ServerAgeManager;
import dev.mariany.genesisframework.registry.GFRegistryKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

@Mixin(CommandSourceStack.class)
public class ServerCommandSourceMixin {
    @Inject(method = "suggestRegistryElements(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/commands/SharedSuggestionProvider$ElementSuggestionType;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;Lcom/mojang/brigadier/context/CommandContext;)Ljava/util/concurrent/CompletableFuture;", at = @At(value = "HEAD"), cancellable = true)
    public void injectListIdSuggestions(
            ResourceKey<? extends Registry<?>> registryRef,
            SharedSuggestionProvider.ElementSuggestionType suggestedIdType,
            SuggestionsBuilder builder,
            CommandContext<?> context,
            CallbackInfoReturnable<CompletableFuture<Suggestions>> cir
    ) {
        if (registryRef == GFRegistryKeys.AGE) {
            CompletableFuture<Suggestions> suggestions = SharedSuggestionProvider.suggestResource(
                    ServerAgeManager.getInstance().getAges().stream().map(AgeEntry::getId),
                    builder
            );

            cir.setReturnValue(suggestions);
        }
    }
}
