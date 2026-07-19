package dev.mariany.genesisframework.sound;

import dev.mariany.genesisframework.GenesisFramework;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class GFSoundEvents {
    public static final SoundEvent UI_TOAST_INSTRUCTIONS_COMPLETE = register("ui.toast.instructions_complete");

    private static SoundEvent register(String id) {
        return register(GenesisFramework.id(id));
    }

    private static SoundEvent register(Identifier id) {
        return register(id, id);
    }

    private static SoundEvent register(Identifier id, Identifier soundId) {
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(soundId));
    }

    private static Holder.Reference<SoundEvent> registerReference(String id) {
        return registerReference(GenesisFramework.id(id));
    }

    private static Holder.Reference<SoundEvent> registerReference(Identifier id) {
        return registerReference(id, id);
    }

    private static Holder.Reference<SoundEvent> registerReference(Identifier id, Identifier soundId) {
        return Registry.registerForHolder(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(soundId));
    }

    public static void bootstrap() {
        GenesisFramework.bootstrapLog("Sound Events");
    }
}
