package dev.mariany.genesisframework.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mariany.genesisframework.event.server.advancement.ServerAdvancementEvents;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.server.ServerAdvancementManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerAdvancementManager.class)
public class ServerAdvancementLoaderMixin {
    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/advancements/AdvancementTree;addAll(Ljava/lang/Iterable;)V"
            )
    )
    private void wrapAddAll(
            AdvancementTree tree,
            Iterable<AdvancementHolder> advancements,
            Operation<Void> original
    ) {
        ServerAdvancementManager serverAdvancementManager = (ServerAdvancementManager) (Object) this;

        if (ServerAdvancementEvents.ALLOW_TREE_BUILD.invoker().allowTreeBuild(serverAdvancementManager)) {
            original.call(tree, advancements);
        }
    }

    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/advancements/AdvancementTree;repositionNodes()V"
            )
    )
    private void wrapRepositionNodes(AdvancementTree tree, Operation<Void> original) {
        ServerAdvancementManager serverAdvancementManager = (ServerAdvancementManager) (Object) this;

        if (ServerAdvancementEvents.ALLOW_TREE_BUILD.invoker().allowTreeBuild(serverAdvancementManager)) {
            original.call(tree);
        }
    }
}
