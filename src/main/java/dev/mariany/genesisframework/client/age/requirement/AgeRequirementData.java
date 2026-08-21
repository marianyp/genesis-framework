package dev.mariany.genesisframework.client.age.requirement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Environment(EnvType.CLIENT)
public record AgeRequirementData(
        ItemStack stack,
        List<Identifier> requiredAgeIds,
        Set<Identifier> unlockedAgeIds
) {
    public AgeRequirementData {
        stack = stack.copy();
        requiredAgeIds = List.copyOf(requiredAgeIds);
        unlockedAgeIds = Set.copyOf(unlockedAgeIds);
    }

    public boolean isUnlocked(Identifier ageId) {
        return this.unlockedAgeIds.contains(ageId);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof AgeRequirementData(
                ItemStack otherStack,
                List<Identifier> otherRequiredAges,
                Set<Identifier> otherUnlockedAges
        )
                && this.stack.getItem() == otherStack.getItem()
                && this.requiredAgeIds.equals(otherRequiredAges)
                && this.unlockedAgeIds.equals(otherUnlockedAges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.stack.getItem(), this.requiredAgeIds, this.unlockedAgeIds);
    }
}
