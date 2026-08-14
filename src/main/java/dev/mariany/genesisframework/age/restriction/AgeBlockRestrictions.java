package dev.mariany.genesisframework.age.restriction;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.age.AgeEntry;
import dev.mariany.genesisframework.age.AgeLockNotifier;
import dev.mariany.genesisframework.age.ServerAgeManager;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Optional;

public final class AgeBlockRestrictions {
    private AgeBlockRestrictions() {
    }

    public static void bootstrap() {
        GenesisFramework.bootstrapLog("Age Block Restrictions");
        UseBlockCallback.EVENT.register(AgeBlockRestrictions::checkBlockUse);
    }

    private static InteractionResult checkBlockUse(
            Player player,
            Level level,
            InteractionHand hand,
            BlockHitResult result
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        ServerAgeManager serverAgeManager = GenesisFramework.getServerAgeManager();
        BlockState state = level.getBlockState(result.getBlockPos());
        Block block = state.getBlock();

        if (serverAgeManager.isUnlocked(serverPlayer, block)) {
            return InteractionResult.PASS;
        }

        Optional<AgeEntry> optionalAgeEntry = serverAgeManager
                .getRequiredAges(block.asItem().getDefaultInstance())
                .stream()
                .findAny();

        optionalAgeEntry.ifPresent(ageEntry -> AgeLockNotifier.notifyAgeLockedClick(
                block,
                ageEntry,
                serverPlayer
        ));

        return InteractionResult.FAIL;
    }
}
