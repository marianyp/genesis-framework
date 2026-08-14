package dev.mariany.genesisframework.compat;

import dev.mariany.genesisframework.age.AgeMetadata;
import dev.mariany.genesisframework.client.GenesisFrameworkClient;
import dev.mariany.genesisframework.client.age.ClientAgeManager;
import dev.mariany.genesisframework.client.age.requirement.AgeRequirementData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public final class AgeRecipeTransferMessages {
    private AgeRecipeTransferMessages() {
    }

    public static Optional<Component> forLockedOutputs(Collection<ItemStack> outputs) {
        ClientAgeManager clientAgeManager = GenesisFrameworkClient.getAgeManager();

        List<ItemStack> lockedOutputs = outputs
                .stream()
                .filter(stack -> !clientAgeManager.isUnlocked(stack))
                .toList();

        if (lockedOutputs.isEmpty()) {
            return Optional.empty();
        }

        List<AgeMetadata> missingAges = lockedOutputs
                .stream()
                .map(clientAgeManager::getAgeRequirements)
                .flatMap(Optional::stream)
                .flatMap(data -> missingAges(data).stream())
                .distinct()
                .sorted(Comparator.comparing(AgeMetadata::id))
                .toList();

        MutableComponent component;

        if (missingAges.size() > 1) {
            component = Component.translatable(
                    "gui.genesisframework.recipe_transfer.age_locked.multiple",
                    missingAges.size()
            );
        } else {
            component = Component.translatable(
                    "gui.genesisframework.recipe_transfer.age_locked",
                    missingAges.getFirst().component()
            );
        }

        return Optional.of(component.withColor(CommonColors.SOFT_RED));
    }

    private static List<AgeMetadata> missingAges(AgeRequirementData data) {
        return data.requiredAges()
                   .stream()
                   .filter(age -> !data.isUnlocked(age))
                   .toList();
    }
}
