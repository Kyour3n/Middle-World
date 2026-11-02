package com.example.middleworld.entity;

import com.example.middleworld.reputation.Faction;
import com.example.middleworld.reputation.Rank;
import net.minecraft.world.entity.MobCategory;

/** The three playable-world races. Each one maps onto a reputation faction. */
public enum Race {
    //     id        faction            category              w     h     hp    dmg    speed  pitch  calmFrom        scale x/y/z
    GOBLIN("goblin", Faction.GOBLINS, MobCategory.MONSTER, 0.5F, 1.3F, 0.75D, 0.85D, 0.06D, 1.35F, Rank.FRIENDLY, 1.00F, 1.00F, 1.00F),
    ELF("elf", Faction.ELVES, MobCategory.CREATURE, 0.6F, 1.8F, 0.90D, 1.00D, 0.02D, 1.20F, Rank.NEUTRAL, 0.92F, 1.00F, 0.92F),
    DWARF("dwarf", Faction.DWARVES, MobCategory.CREATURE, 0.7F, 1.4F, 1.30D, 1.15D, -0.02D, 0.75F, Rank.NEUTRAL, 1.22F, 1.00F, 1.15F);

    public final String id;
    public final Faction faction;
    public final MobCategory category;
    public final float width;
    public final float height;
    public final double healthMult;
    public final double damageMult;
    public final double speedBonus;
    public final float voicePitch;
    /** Players at or above this rank are left alone. Below it, this race attacks them on sight. */
    public final Rank calmFrom;
    public final float scaleX;
    public final float scaleY;
    public final float scaleZ;

    Race(String id, Faction faction, MobCategory category, float width, float height,
         double healthMult, double damageMult, double speedBonus, float voicePitch, Rank calmFrom,
         float scaleX, float scaleY, float scaleZ) {
        this.id = id;
        this.faction = faction;
        this.category = category;
        this.width = width;
        this.height = height;
        this.healthMult = healthMult;
        this.damageMult = damageMult;
        this.speedBonus = speedBonus;
        this.voicePitch = voicePitch;
        this.calmFrom = calmFrom;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
    }

    public boolean isHostileByNature() {
        return this == GOBLIN;
    }
}
