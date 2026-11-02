package com.example.middleworld.reputation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;

/** Per-player reputation: one value per faction plus a short history of changes. */
public class ReputationData {
    public static final int MIN = -1000;
    public static final int MAX = 1000;
    public static final int MAX_HISTORY = 60;

    public record HistoryEntry(int faction, int delta, String reason, long time) {
        public static final Codec<HistoryEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("faction").forGetter(HistoryEntry::faction),
                Codec.INT.fieldOf("delta").forGetter(HistoryEntry::delta),
                Codec.STRING.fieldOf("reason").forGetter(HistoryEntry::reason),
                Codec.LONG.fieldOf("time").forGetter(HistoryEntry::time)
        ).apply(i, HistoryEntry::new));
    }

    public static final Codec<ReputationData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.listOf().fieldOf("values").forGetter(ReputationData::valueList),
            HistoryEntry.CODEC.listOf().fieldOf("history").forGetter(d -> d.history),
            Codec.LONG.fieldOf("lastRecoveryDay").forGetter(d -> d.lastRecoveryDay),
            Codec.LONG.fieldOf("tradeDay").forGetter(d -> d.tradeDay),
            Codec.INT.fieldOf("tradeGain").forGetter(d -> d.tradeGain)
    ).apply(i, ReputationData::new));

    private final int[] values = new int[Faction.values().length];
    private final List<HistoryEntry> history = new ArrayList<>();
    public long lastRecoveryDay = 0L;
    public long tradeDay = -1L;
    public int tradeGain = 0;

    public ReputationData() {
    }

    private ReputationData(List<Integer> vals, List<HistoryEntry> hist, long lastRecoveryDay, long tradeDay, int tradeGain) {
        for (int n = 0; n < values.length && n < vals.size(); n++) {
            values[n] = clamp(vals.get(n));
        }
        history.addAll(hist);
        this.lastRecoveryDay = lastRecoveryDay;
        this.tradeDay = tradeDay;
        this.tradeGain = tradeGain;
    }

    private static int clamp(int v) {
        return Math.max(MIN, Math.min(MAX, v));
    }

    public int get(Faction f) {
        return values[f.ordinal()];
    }

    public void set(Faction f, int value) {
        values[f.ordinal()] = clamp(value);
    }

    public Rank rank(Faction f) {
        return Rank.of(get(f));
    }

    public List<HistoryEntry> history() {
        return history;
    }

    public List<Integer> valueList() {
        List<Integer> list = new ArrayList<>(values.length);
        for (int v : values) {
            list.add(v);
        }
        return list;
    }

    /** Adds a history entry, merging rapid repeats (e.g. several hits in a row) into one line. */
    public void addHistory(Faction f, int delta, String reason, long time) {
        if (!history.isEmpty()) {
            int lastIndex = history.size() - 1;
            HistoryEntry last = history.get(lastIndex);
            if (last.faction() == f.ordinal() && last.reason().equals(reason)
                    && time - last.time() < 200L && Integer.signum(last.delta()) == Integer.signum(delta)) {
                history.set(lastIndex, new HistoryEntry(last.faction(), last.delta() + delta, reason, time));
                return;
            }
        }
        history.add(new HistoryEntry(f.ordinal(), delta, reason, time));
        while (history.size() > MAX_HISTORY) {
            history.remove(0);
        }
    }

    // ---- Networking ----

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(values.length);
        for (int v : values) {
            buf.writeVarInt(v - MIN); // shift so the value is never negative
        }
        buf.writeVarInt(history.size());
        for (HistoryEntry e : history) {
            buf.writeVarInt(e.faction());
            buf.writeInt(e.delta());
            buf.writeUtf(e.reason());
            buf.writeLong(e.time());
        }
    }

    public static ReputationData read(FriendlyByteBuf buf) {
        ReputationData d = new ReputationData();
        int count = buf.readVarInt();
        for (int n = 0; n < count; n++) {
            int v = buf.readVarInt() + MIN;
            if (n < d.values.length) {
                d.values[n] = clamp(v);
            }
        }
        int hist = buf.readVarInt();
        for (int n = 0; n < hist; n++) {
            d.history.add(new HistoryEntry(buf.readVarInt(), buf.readInt(), buf.readUtf(), buf.readLong()));
        }
        return d;
    }
}
