package dev.mariany.genesisframework.mixin.compat.jei;

import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

public final class JEIMixinConfigPlugin implements IMixinConfigPlugin {
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return FabricLoader.getInstance().isModLoaded("jei");
    }
}
