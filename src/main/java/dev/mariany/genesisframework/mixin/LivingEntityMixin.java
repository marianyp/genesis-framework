package dev.mariany.genesisframework.mixin;

import dev.mariany.genesisframework.age.ServerAgeManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "collectEquipmentChanges", at = @At(value = "RETURN"))
    protected void collectEquipmentChanges(
            Map<EquipmentSlot, ItemStack> lastEquipmentItems,
            CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir
    ) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;

        if (!(livingEntity instanceof ServerPlayer serverPlayer)) {
            return;
        }

        Map<EquipmentSlot, ItemStack> changedItems = cir.getReturnValue();

        if (changedItems == null) {
            return;
        }

        ServerAgeManager.getInstance().onEquipmentUpdate(serverPlayer, changedItems);
    }
}
