package dev.mariany.genesisframework.client.age.requirement;

import dev.mariany.genesisframework.age.AgeMetadata;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Environment(EnvType.CLIENT)
public record AgeRequirementData(
        ItemStack stack,
        List<AgeMetadata> requiredAges,
        Set<AgeMetadata> unlockedAges
) {
    public AgeRequirementData {
        stack = stack.copy();
        requiredAges = List.copyOf(requiredAges);
        unlockedAges = Set.copyOf(unlockedAges);
    }

    public AgeRequirementData(ItemStack stack, List<AgeMetadata> requiredAges) {
        this(stack, requiredAges, Set.of());
    }

    public boolean isUnlocked(AgeMetadata ageMetadata) {
        return this.unlockedAges.contains(ageMetadata);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof AgeRequirementData(
                ItemStack otherStack,
                List<AgeMetadata> otherRequiredAges,
                Set<AgeMetadata> otherUnlockedAges
        )
                && this.stack.getItem() == otherStack.getItem()
                && this.requiredAges.equals(otherRequiredAges)
                && this.unlockedAges.equals(otherUnlockedAges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.stack.getItem(), this.requiredAges, this.unlockedAges);
    }
}
