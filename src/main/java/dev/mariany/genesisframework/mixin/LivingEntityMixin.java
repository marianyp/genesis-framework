package dev.mariany.genesisframework.mixin;

import dev.mariany.genesisframework.event.server.entity.ServerEntityEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    /**
     * Invokes {@link ServerEntityEvents#EQUIPMENT_CHANGED} when equipment changes are collected.
     */
    @Inject(method = "collectEquipmentChanges", at = @At(value = "RETURN"))
    protected void injectCollectEquipmentChanges(
            Map<EquipmentSlot, ItemStack> lastEquipmentItems,
            CallbackInfoReturnable<@Nullable Map<EquipmentSlot, ItemStack>> cir
    ) {
        Map<EquipmentSlot, ItemStack> changedItems = cir.getReturnValue();

        if (changedItems == null || changedItems.isEmpty()) {
            return;
        }

        ServerEntityEvents.EQUIPMENT_CHANGED
                .invoker()
                .onEquipmentChanged((LivingEntity) (Object) this, changedItems);
    }
}
