package dev.mariany.genesisframework.mixin.accessor;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerAdvancementManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ServerAdvancementManager.class)
public interface ServerAdvancementManagerAccessor {
    @Accessor("advancements")
    Map<Identifier, AdvancementHolder> genesis$advancements();

    @Mutable
    @Accessor("advancements")
    void genesis$advancements(Map<Identifier, AdvancementHolder> advancements);

    @Accessor("tree")
    AdvancementTree genesis$tree();
}
