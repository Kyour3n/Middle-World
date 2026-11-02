package com.example.middleworld.reputation;

import com.example.middleworld.entity.FactionMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.raid.Raider;

/** Maps entities to the faction they belong to. */
public class Factions {

    /** @return the faction of this entity, or null if it belongs to none. */
    public static Faction of(LivingEntity entity) {
        if (entity instanceof Villager || entity instanceof WanderingTrader || entity instanceof IronGolem) {
            return Faction.VILLAGERS;
        }
        if (entity instanceof Raider) { // pillagers, vindicators, evokers, illusioners, ravagers, witches in raids
            return Faction.PILLAGERS;
        }
        if (entity instanceof AbstractPiglin) { // piglins and piglin brutes
            return Faction.PIGLINS;
        }
        if (entity instanceof FactionMob mob) { // goblins, elves and dwarves, every role
            return mob.race().faction;
        }
        return null;
    }
}
