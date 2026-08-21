package dev.mariany.genesisframework.age;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.ItemStackTemplate;

public record AgeDisplay(ItemStackTemplate icon, Component description) {
    public static final Codec<AgeDisplay> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            ItemStackTemplate.CODEC.fieldOf("icon").forGetter(AgeDisplay::icon),
                            ComponentSerialization.CODEC.optionalFieldOf("description", Component.empty()).forGetter(AgeDisplay::description)
                    )
                    .apply(instance, AgeDisplay::new)
    );
}
