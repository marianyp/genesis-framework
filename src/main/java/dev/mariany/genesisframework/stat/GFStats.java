package dev.mariany.genesisframework.stat;

import dev.mariany.genesisframework.GenesisFramework;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

public final class GFStats {
    public static final Holder.Reference<Identifier> HOSTILE_KILLS = register(
            "hostile_kills",
            StatFormatter.DEFAULT
    );

    private GFStats() {
    }

    private static Holder.Reference<Identifier> register(String name, StatFormatter formatter) {
        Identifier id = GenesisFramework.id(name);
        Holder.Reference<Identifier> reference = Registry.registerForHolder(BuiltInRegistries.CUSTOM_STAT, id, id);
        Stats.CUSTOM.get(id, formatter);
        return reference;
    }

    public static void bootstrap() {
        GenesisFramework.bootstrapLog("Stats");
    }
}
