package dev.mariany.genesisframework.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mariany.genesisframework.age.AgeDataLoader;
import dev.mariany.genesisframework.instruction.InstructionDataLoader;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ReloadableServerResources.class)
public class DataPackContentsMixin {
    @Unique
    private AgeDataLoader ageLoader;

    @Unique
    private InstructionDataLoader instructionDataLoader;

    /**
     * Initiate age and instruction data loaders.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(
            LayeredRegistryAccess<RegistryLayer> fullLayers,
            HolderLookup.Provider loadingContext,
            FeatureFlagSet enabledFeatures,
            Commands.CommandSelection commandSelection,
            List<Registry.PendingTags<?>> postponedTags,
            PermissionSet functionCompilationPermissions,
            List<DataComponentInitializers.PendingComponents<?>> newComponents,
            CallbackInfo ci
    ) {
        this.ageLoader = new AgeDataLoader(loadingContext);
        this.instructionDataLoader = new InstructionDataLoader(loadingContext);
    }

    /**
     * Include age and instruction data loaders when getting data pack contents.
     */
    @WrapOperation(method = "listeners", at = @At(value = "INVOKE", target = "Ljava/util/List;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;"))
    public List<PreparableReloadListener> wrapGetContents(
            Object first,
            Object second,
            Object third,
            Operation<List<PreparableReloadListener>> original
    ) {
        List<PreparableReloadListener> resourceReloaders = new ArrayList<>(List.of(this.ageLoader, this.instructionDataLoader));
        resourceReloaders.addAll(original.call(first, second, third));
        return resourceReloaders;
    }
}
