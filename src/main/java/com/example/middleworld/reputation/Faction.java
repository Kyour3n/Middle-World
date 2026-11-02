package com.example.middleworld.reputation;

import java.util.List;
import net.minecraft.network.chat.Component;

public enum Faction {
    ELVES("elves", 0x55D17A),
    GOBLINS("goblins", 0x8FBF3A),
    DWARVES("dwarves", 0xD39A5A),
    VILLAGERS("villagers", 0x4FD1A5),
    PILLAGERS("pillagers", 0xA0A6B4),
    PIGLINS("piglins", 0xF0B040);

    public final String id;
    public final int color;

    Faction(String id, int color) {
        this.id = id;
        this.color = color;
    }

    public Component displayName() {
        return Component.translatable("faction.middleworld." + id);
    }

    /** Helping a faction hurts its rivals; hurting a faction slightly pleases its rivals. */
    public List<Faction> rivals() {
        return switch (this) {
            case ELVES -> List.of(GOBLINS);
            case GOBLINS -> List.of(ELVES, DWARVES);
            case DWARVES -> List.of(GOBLINS, PIGLINS);
            case VILLAGERS -> List.of(PILLAGERS);
            case PILLAGERS -> List.of(VILLAGERS);
            case PIGLINS -> List.of(DWARVES);
        };
    }

    public static Faction byId(String id) {
        for (Faction f : values()) {
            if (f.id.equals(id)) {
                return f;
            }
        }
        return null;
    }

    public static Faction byIndex(int index) {
        Faction[] all = values();
        return index >= 0 && index < all.length ? all[index] : null;
    }
}
