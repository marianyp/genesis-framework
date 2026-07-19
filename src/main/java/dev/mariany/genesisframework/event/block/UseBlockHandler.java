package dev.mariany.genesisframework.event.block;

import dev.mariany.genesisframework.age.AgeEntry;
import dev.mariany.genesisframework.age.AgeLockNotifier;
import dev.mariany.genesisframework.age.ServerAgeManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Optional;

public class UseBlockHandler {
    public static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult result) {
        if (player instanceof ServerPlayer serverPlayer) {
            ServerAgeManager serverAgeManager = ServerAgeManager.getInstance();
            BlockState state = level.getBlockState(result.getBlockPos());
            Block block = state.getBlock();

            if (!serverAgeManager.isUnlocked(serverPlayer, block)) {
                String itemTranslation = block.getName().getString();

                Optional<AgeEntry> optionalAgeEntry = serverAgeManager.getRequiredAges(block.asItem().getDefaultInstance())
                                                                      .stream()
                                                                      .findAny();

                optionalAgeEntry.ifPresent(ageEntry ->
                        AgeLockNotifier.notifyAgeLockedClick(itemTranslation, ageEntry.getAge(), serverPlayer)
                );

                return InteractionResult.FAIL;
            }
        }

        return InteractionResult.PASS;
    }
}
