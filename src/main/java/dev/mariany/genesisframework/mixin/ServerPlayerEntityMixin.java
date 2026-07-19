package dev.mariany.genesisframework.mixin;

import com.mojang.authlib.GameProfile;
import dev.mariany.genesisframework.stat.GFStats;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin extends Player {
    public ServerPlayerEntityMixin(Level level, GameProfile profile) {
        super(level, profile);
    }

    /**
     * Increment Hostile Kills Stat.
     */
    @Inject(method = "awardKillScore", at = @At(value = "HEAD"))
    public void injectUpdateKilledAdvancementCriterion(Entity entityKilled, DamageSource damageSource, CallbackInfo ci) {
        if (entityKilled instanceof Monster) {
            this.awardStat(GFStats.HOSTILE_KILLS.value());
        }
    }
}
