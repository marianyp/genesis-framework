package dev.mariany.genesisframework.gamerule;

import dev.mariany.genesisframework.GenesisFramework;
import dev.mariany.genesisframework.age.AgeShareManager;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

public final class GFGameRules {
    public static final GameRule<AgeShareManager.AgeSharingOption> AGE_SHARING = GameRuleBuilder
            .forEnum(AgeShareManager.AgeSharingOption.DISABLED)
            .codec(StringRepresentable.fromEnum(AgeShareManager.AgeSharingOption::values))
            .category(GameRuleCategory.MISC)
            .buildAndRegister(GenesisFramework.id("age_sharing"));

    private GFGameRules() {
    }

    public static void bootstrap() {
        GenesisFramework.bootstrapLog("Game Rules");
    }
}
