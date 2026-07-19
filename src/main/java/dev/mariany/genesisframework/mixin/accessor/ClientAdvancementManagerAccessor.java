package dev.mariany.genesisframework.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.multiplayer.ClientAdvancements;

@Mixin(ClientAdvancements.class)
public interface ClientAdvancementManagerAccessor {
    @Accessor("progress")
    Map<AdvancementHolder, AdvancementProgress> genesis$advancementProgresses();
}
