package com.example.middleworld.reputation;

import com.example.middleworld.MWConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;

/** Makes Iron Golems attack players who are Hostile with the Villagers. */
public class ReputationTargetGoal extends NearestAttackableTargetGoal<Player> {

    public ReputationTargetGoal(IronGolem golem) {
        super(golem, Player.class, 10, true, false, ReputationTargetGoal::isHatedByVillagers);
    }

    private static boolean isHatedByVillagers(LivingEntity entity) {
        return MWConfig.GOLEMS_ATTACK_HOSTILE.get()
                && entity instanceof Player player
                && !player.isCreative()
                && !player.isSpectator()
                && ReputationManager.rank(player, Faction.VILLAGERS) == Rank.HOSTILE;
    }
}
