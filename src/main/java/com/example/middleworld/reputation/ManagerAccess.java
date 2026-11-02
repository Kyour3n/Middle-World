package com.example.middleworld.reputation;

import com.example.middleworld.entity.Race;
import net.minecraft.world.entity.player.Player;

/** Small bridge so mob code does not depend on how reputation is stored. */
public class ManagerAccess {
    /** True if this player's standing with the race's faction is high enough that the race leaves them alone. */
    public static boolean isCalmAround(Player player, Race race) {
        return ReputationManager.rank(player, race.faction).atLeast(race.calmFrom);
    }
}
