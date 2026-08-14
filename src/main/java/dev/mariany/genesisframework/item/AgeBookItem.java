package dev.mariany.genesisframework.item;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.advancement.AdvancementHelper;
import dev.mariany.genesisframework.age.Age;
import dev.mariany.genesisframework.age.AgeEntry;
import dev.mariany.genesisframework.age.ServerAgeManager;
import dev.mariany.genesisframework.component.GFComponentTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class AgeBookItem extends Item {
    public AgeBookItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        List<ResourceKey<Age>> ages = itemStack.getOrDefault(GFComponentTypes.AGES, List.of());
        itemStack.consume(1, player);

        if (ages.isEmpty()) {
            return InteractionResult.FAIL;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        ServerAgeManager serverAgeManager = GenesisFramework.getServerAgeManager();

        for (ResourceKey<Age> ageKey : ages) {
            Optional<AgeEntry> optionalAgeEntry = serverAgeManager.get(ageKey.identifier());

            if (optionalAgeEntry.isEmpty()) {
                GenesisFramework.LOGGER.error("Invalid age: {}", ageKey);
                continue;
            }

            AdvancementHelper.giveAdvancement(serverPlayer, optionalAgeEntry.get().getAdvancementHolder());
        }

        player.awardStat(Stats.ITEM_USED.get(this));

        return InteractionResult.SUCCESS;
    }
}
