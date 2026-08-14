package dev.mariany.genesisframework.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mariany.genesisframework.event.server.advancement.ServerAdvancementEvents;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.server.advancements.AdvancementVisibilityEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

@Mixin(AdvancementVisibilityEvaluator.class)
public class AdvancementDisplaysMixin {
    /**
     * Queries {@link ServerAdvancementEvents#SHOULD_FORCE_DISPLAY} while evaluating advancement visibility.
     */
    @WrapOperation(
            method = "evaluateVisibility(Lnet/minecraft/advancements/AdvancementNode;Lit/unimi/dsi/fastutil/Stack;Ljava/util/function/Predicate;Lnet/minecraft/server/advancements/AdvancementVisibilityEvaluator$Output;)Z",
            at = @At(value = "INVOKE", target = "Ljava/util/function/Predicate;test(Ljava/lang/Object;)Z")
    )
    private static boolean wrapTest(
            Predicate<AdvancementNode> predicate,
            Object object,
            Operation<Boolean> original
    ) {
        if (object instanceof AdvancementNode advancementNode) {
            if (ServerAdvancementEvents.SHOULD_FORCE_DISPLAY.invoker().shouldForceDisplay(advancementNode)) {
                return true;
            }
        }

        return original.call(predicate, object);
    }
}
