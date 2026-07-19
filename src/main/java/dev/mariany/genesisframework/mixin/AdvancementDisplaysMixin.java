package dev.mariany.genesisframework.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mariany.genesisframework.age.AgeEntry;
import dev.mariany.genesisframework.age.ServerAgeManager;
import dev.mariany.genesisframework.instruction.InstructionEntry;
import dev.mariany.genesisframework.instruction.ServerInstructionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.server.advancements.AdvancementVisibilityEvaluator;

@Mixin(AdvancementVisibilityEvaluator.class)
public class AdvancementDisplaysMixin {
    /**
     * Enforce advancement to be displayed and thus sent to client if it belongs to an age or instruction.
     */
    @WrapOperation(
            method = "evaluateVisibility(Lnet/minecraft/advancements/AdvancementNode;Lit/unimi/dsi/fastutil/Stack;Ljava/util/function/Predicate;Lnet/minecraft/server/advancements/AdvancementVisibilityEvaluator$Output;)Z",
            at = @At(value = "INVOKE", target = "Ljava/util/function/Predicate;test(Ljava/lang/Object;)Z")
    )
    private static boolean wrapShouldDisplay(
            Predicate<AdvancementNode> predicate, Object object, Operation<Boolean> original
    ) {
        if (object instanceof AdvancementNode placedAdvancement) {
            Optional<AgeEntry> optionalAge = ServerAgeManager.getInstance().find(placedAdvancement.holder());

            if (optionalAge.isPresent()) {
                return true;
            }

            Optional<InstructionEntry> optionalInstruction = ServerInstructionManager.getInstance().find(
                    placedAdvancement.holder()
            );

            if (optionalInstruction.isPresent()) {
                return true;
            }
        }

        return original.call(predicate, object);
    }
}
