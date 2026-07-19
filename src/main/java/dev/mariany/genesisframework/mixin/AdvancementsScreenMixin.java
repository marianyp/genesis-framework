package dev.mariany.genesisframework.mixin;

import dev.mariany.genesisframework.age.AgeEntry;
import dev.mariany.genesisframework.client.GFClient;
import dev.mariany.genesisframework.instruction.InstructionEntry;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Optional;

@Mixin(AdvancementsScreen.class)
public abstract class AdvancementsScreenMixin {
    @Shadow
    @Final
    private Map<AdvancementHolder, AdvancementTab> tabs;

    @Shadow
    public abstract void onSelectedTabChanged(@Nullable AdvancementHolder advancement);

    @Shadow
    @Final
    private ClientAdvancements advancements;

    /**
     * Start on Age tab if advancement present.
     */
    @Inject(method = "init", at = @At(value = "TAIL"))
    public void injectInit(CallbackInfo ci) {
        if (!GFClient.getConfig().advancementScreenStartsOnAges) {
            return;
        }

        Optional<AdvancementHolder> optionalAgeRoot = this.tabs
                .keySet()
                .stream()
                .filter(AgeEntry::isRoot)
                .findFirst();

        optionalAgeRoot.ifPresent(advancementEntry -> {
            this.onSelectedTabChanged(advancementEntry);
            this.advancements.setSelectedTab(advancementEntry, true);
        });
    }

    /**
     * Prevents instruction tab from being added.
     */
    @Inject(method = "onAddAdvancementRoot", at = @At(value = "HEAD"), cancellable = true)
    public void injectOnRootAdded(AdvancementNode root, CallbackInfo ci) {
        if (root.holder().id().equals(InstructionEntry.ROOT_ADVANCEMENT_ID)) {
            ci.cancel();
        }
    }
}
