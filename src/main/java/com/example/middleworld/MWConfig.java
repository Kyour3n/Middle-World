package com.example.middleworld;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Common config: config/middleworld-common.toml */
public class MWConfig {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue GAIN_MULTIPLIER = B
            .comment("Multiplier applied to every reputation GAIN. 1.0 = default, 0.5 = half as fast.")
            .defineInRange("gainMultiplier", 1.0D, 0.0D, 10.0D);

    public static final ModConfigSpec.DoubleValue LOSS_MULTIPLIER = B
            .comment("Multiplier applied to every reputation LOSS (penalties). 0 disables penalties.")
            .defineInRange("lossMultiplier", 1.0D, 0.0D, 10.0D);

    public static final ModConfigSpec.DoubleValue RIVAL_PERCENT = B
            .comment("How strongly reputation spills over to rival factions. 0 = factions are independent, 1 = default, 2 = double.")
            .defineInRange("rivalSpillover", 1.0D, 0.0D, 5.0D);

    public static final ModConfigSpec.IntValue HIT_PENALTY = B
            .comment("Reputation lost for each hit on a peaceful faction member (not in self-defense).")
            .defineInRange("hitPenalty", 2, 0, 50);

    public static final ModConfigSpec.IntValue TRADE_DAILY_CAP = B
            .comment("Maximum Villager reputation gained from trading per in-game day.")
            .defineInRange("tradeGainPerDay", 10, 0, 200);

    public static final ModConfigSpec.IntValue RECOVERY_PER_DAY = B
            .comment("How much negative reputation heals toward neutral each in-game day. 0 disables recovery.")
            .defineInRange("recoveryPerDay", 5, 0, 200);

    public static final ModConfigSpec.BooleanValue GOLEMS_ATTACK_HOSTILE = B
            .comment("Iron Golems attack players who are Hostile with the Villagers.")
            .define("golemsAttackHostile", true);

    public static final ModConfigSpec.BooleanValue VILLAGERS_REFUSE_HOSTILE = B
            .comment("Villagers and Wandering Traders refuse to trade with players who are Hostile with the Villagers.")
            .define("villagersRefuseHostile", true);

    public static final ModConfigSpec.BooleanValue FRIENDLY_MOBS_STAY_CALM = B
            .comment("Faction mobs do not target players who are Friendly or better with their faction (unless provoked).")
            .define("friendlyMobsStayCalm", true);

    public static final ModConfigSpec SPEC = B.build();
}
