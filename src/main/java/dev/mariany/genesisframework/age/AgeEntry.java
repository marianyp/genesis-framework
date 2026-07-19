package dev.mariany.genesisframework.age;

import dev.mariany.genesisframework.GenesisFramework;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.triggers.PlayerTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AgeEntry {
    private static final String ADVANCEMENT_PREFIX = "age/";
    public static final Identifier ROOT_ADVANCEMENT_ID = GenesisFramework.id(ADVANCEMENT_PREFIX + "root");

    private final Identifier id;
    private final Age age;
    private final AdvancementHolder advancementHolder;

    public AgeEntry(Identifier id, Age age) {
        this.id = id;
        this.age = age;
        this.advancementHolder = createAdvancementEntry(id, age);
    }

    public static boolean isRoot(AdvancementHolder advancementHolder) {
        return advancementHolder.id().equals(AgeEntry.ROOT_ADVANCEMENT_ID);
    }

    private AdvancementHolder createAdvancementEntry(Identifier id, Age age) {
        return new AdvancementHolder(getAdvancementId(this), createAdvancement(id, age));
    }

    public static Advancement createAdvancement(Identifier id, Age age) {
        Identifier parent = age.parent().map(AgeEntry::getAdvancementId).orElse(AgeEntry.ROOT_ADVANCEMENT_ID);

        boolean alert = true;

        Map<String, Criterion<?>> advancementCriteria = new HashMap<>(age.criteria());

        if (advancementCriteria.isEmpty()) {
            advancementCriteria.put(
                    "root",
                    new Criterion<>(
                            CriteriaTriggers.TICK,
                            PlayerTrigger.TriggerInstance.tick().triggerInstance()
                    )
            );

            alert = false;
        }

        AdvancementRequirements requirements = age.requirements().isEmpty() ?
                AdvancementRequirements.allOf(advancementCriteria.keySet()) :
                age.requirements();

        return new Advancement(
                Optional.of(parent),
                Optional.of(createAdvancementDisplay(id, age, alert)),
                AdvancementRewards.EMPTY,
                advancementCriteria,
                requirements,
                false
        );
    }

    private static DisplayInfo createAdvancementDisplay(Identifier id, Age age, boolean alert) {
        AgeDisplay ageDisplay = age.display();

        MutableComponent title = getCategory(id)
                .map(category -> Component.translatable("age." + id.getNamespace() + ".category." + category)
                                          .append(Component.literal(": "))
                                          .append(ageDisplay.title())
                                          .append(Component.literal(" "))
                                          .append(Component.translatable("age.genesisframework.age")))
                .orElseGet(() -> Component.translatable(
                        "age.genesisframework.title",
                        ageDisplay.title(),
                        Component.translatable("age.genesisframework.age")
                ));

        AdvancementType frame = age.requiresParent() ? AdvancementType.GOAL : AdvancementType.CHALLENGE;

        return new DisplayInfo(
                ageDisplay.icon(),
                title,
                ageDisplay.description(),
                Optional.empty(),
                frame,
                alert,
                alert,
                false
        );
    }

    public static Optional<String> getCategory(Identifier id) {
        String path = id.getPath();
        int index = path.indexOf('/');

        if (index >= 0) {
            return Optional.of(path.substring(0, index));
        }

        return Optional.empty();
    }

    public static Optional<String> getSubPath(Identifier id) {
        String path = id.getPath();
        int index = path.indexOf('/');

        if (index >= 0 && index + 1 < path.length()) {
            return Optional.of(path.substring(index + 1));
        }

        return Optional.empty();
    }


    public static Identifier getAdvancementId(AgeEntry ageEntry) {
        return getAdvancementId(ageEntry.getId());
    }

    public static Identifier getAdvancementId(Identifier id) {
        return id.withPrefix(ADVANCEMENT_PREFIX);
    }

    public Identifier getId() {
        return this.id;
    }

    public Age getAge() {
        return this.age;
    }

    public AdvancementHolder getAdvancementHolder() {
        return this.advancementHolder;
    }

    public boolean isDone(ServerPlayer player) {
        return player.getAdvancements().getOrStartProgress(this.advancementHolder).isDone();
    }

    @SuppressWarnings("unused")
    public Optional<Identifier> getParentAdvancementId() {
        return this.advancementHolder.value().parent();
    }
}
