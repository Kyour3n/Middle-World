package com.example.middleworld.entity;

/** The ten roles. Every race has all ten, giving 30 mobs that share one class. */
public enum Role {
    //            id          ranged hat    tip    hp    dmg   speed   armor kbRes scale  weight min max
    WARRIOR("warrior", false, true, false, 18, 4.0, 0.00, 2, 0.0, 1.00F, 10, 1, 3),
    ARCHER("archer", true, true, false, 14, 2.0, 0.00, 0, 0.0, 1.00F, 7, 1, 3),
    KNIGHT("knight", false, true, false, 28, 4.0, -0.03, 6, 0.3, 1.05F, 3, 1, 2),
    BERSERKER("berserker", false, false, false, 20, 8.0, 0.03, 0, 0.0, 1.05F, 4, 1, 2),
    SCOUT("scout", false, false, false, 12, 2.5, 0.07, 0, 0.0, 0.92F, 6, 1, 3),
    MAGE("mage", true, true, true, 12, 2.0, 0.00, 0, 0.0, 1.00F, 3, 1, 2),
    HEALER("healer", false, false, false, 14, 2.0, 0.01, 0, 0.0, 1.00F, 3, 1, 2),
    ASSASSIN("assassin", false, true, false, 12, 4.0, 0.06, 0, 0.0, 0.97F, 3, 1, 1),
    CAPTAIN("captain", false, true, true, 36, 5.0, 0.00, 4, 0.3, 1.12F, 1, 1, 1),
    BRUTE("brute", false, true, false, 50, 8.0, -0.06, 3, 0.7, 1.30F, 2, 1, 1);

    public final String id;
    /** Ranged roles shoot or cast instead of fighting in melee. */
    public final boolean ranged;
    /** Wears a helmet or hood. */
    public final boolean hat;
    /** Hat also has a tip (wizard hat point or captain's crest). */
    public final boolean hatTip;
    public final double health;
    /** Total damage per hit including the weapon (before the race multiplier). */
    public final double damage;
    public final double speedBonus;
    public final double armor;
    public final double knockbackResistance;
    public final float scale;
    public final int spawnWeight;
    public final int groupMin;
    public final int groupMax;

    Role(String id, boolean ranged, boolean hat, boolean hatTip, double health, double damage,
         double speedBonus, double armor, double knockbackResistance, float scale,
         int spawnWeight, int groupMin, int groupMax) {
        this.id = id;
        this.ranged = ranged;
        this.hat = hat;
        this.hatTip = hatTip;
        this.health = health;
        this.damage = damage;
        this.speedBonus = speedBonus;
        this.armor = armor;
        this.knockbackResistance = knockbackResistance;
        this.scale = scale;
        this.spawnWeight = spawnWeight;
        this.groupMin = groupMin;
        this.groupMax = groupMax;
    }
}
