package com.example.middleworld.reputation;

import java.util.Locale;
import net.minecraft.network.chat.Component;

public enum Rank {
    HOSTILE(0xE05050),
    UNFRIENDLY(0xE8923A),
    NEUTRAL(0xB8B8B8),
    FRIENDLY(0x8BD05A),
    HONORED(0x4FC3F7),
    ALLIED(0xFFD54F);

    /** Values at which the rank changes: Unfriendly starts above -400, Neutral above -100, and so on. */
    public static final int[] THRESHOLDS = {-400, -100, 100, 400, 700};

    public final int color;

    Rank(int color) {
        this.color = color;
    }

    public Component displayName() {
        return Component.translatable("rank.middleworld." + name().toLowerCase(Locale.ROOT));
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    public boolean atLeast(Rank other) {
        return this.ordinal() >= other.ordinal();
    }

    public static Rank of(int value) {
        if (value <= -400) return HOSTILE;
        if (value <= -100) return UNFRIENDLY;
        if (value < 100) return NEUTRAL;
        if (value < 400) return FRIENDLY;
        if (value < 700) return HONORED;
        return ALLIED;
    }
}
