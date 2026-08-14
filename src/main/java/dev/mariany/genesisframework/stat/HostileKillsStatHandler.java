package dev.mariany.genesisframework.stat;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;

public final class HostileKillsStatHandler {
    private HostileKillsStatHandler() {
    }

    public static void bootstrap() {
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register(HostileKillsStatHandler::onKill);
    }

    private static void onKill(ServerLevel level, Entity entity, LivingEntity killedEntity, DamageSource damageSource) {
        if (!(entity instanceof ServerPlayer player) || !(killedEntity instanceof Monster)) {
            return;
        }

        player.awardStat(GFStats.HOSTILE_KILLS.value());
    }
}
