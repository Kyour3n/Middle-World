package com.example.middleworld.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;

/** Goblins count as monsters: Iron Golems attack them and they are hostile by default. */
public class GoblinMob extends FactionMob implements Enemy {
    public GoblinMob(EntityType<? extends FactionMob> type, Level level, Role role) {
        super(type, level, Race.GOBLIN, role);
    }
}
