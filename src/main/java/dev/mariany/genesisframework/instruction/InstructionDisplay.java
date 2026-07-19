package dev.mariany.genesisframework.instruction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.ItemStackTemplate;

public record InstructionDisplay(ItemStackTemplate icon, Component title, Component description) {
    public static final Codec<InstructionDisplay> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            ItemStackTemplate.CODEC.fieldOf("icon").forGetter(InstructionDisplay::icon),
                            ComponentSerialization.CODEC.fieldOf("title").forGetter(InstructionDisplay::title),
                            ComponentSerialization.CODEC.optionalFieldOf("description", Component.empty())
                                    .forGetter(InstructionDisplay::description)
                    )
                    .apply(instance, InstructionDisplay::new)
    );
}
