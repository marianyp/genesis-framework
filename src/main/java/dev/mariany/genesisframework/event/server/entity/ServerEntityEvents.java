package dev.mariany.genesisframework.event.server.entity;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.portal.TeleportTransition;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public final class ServerEntityEvents {
    private ServerEntityEvents() {
    }

    public static final Event<ModifyPortalDestination> MODIFY_PORTAL_DESTINATION = EventFactory.createArrayBacked(
            ModifyPortalDestination.class,
            callbacks -> (entity, destination) -> {
                TeleportTransition modifiedDestination = destination;

                for (ModifyPortalDestination callback : callbacks) {
                    modifiedDestination = callback.modifyPortalDestination(entity, modifiedDestination);
                }

                return modifiedDestination;
            }
    );

    public static final Event<EquipmentChanged> EQUIPMENT_CHANGED = EventFactory.createArrayBacked(
            EquipmentChanged.class,
            callbacks -> (entity, changedItems) -> {
                for (EquipmentChanged callback : callbacks) {
                    callback.onEquipmentChanged(entity, changedItems);
                }
            }
    );

    public interface ModifyPortalDestination {
        @Nullable
        TeleportTransition modifyPortalDestination(Entity entity, @Nullable TeleportTransition destination);
    }

    public interface EquipmentChanged {
        void onEquipmentChanged(LivingEntity entity, Map<EquipmentSlot, ItemStack> changedItems);
    }
}
