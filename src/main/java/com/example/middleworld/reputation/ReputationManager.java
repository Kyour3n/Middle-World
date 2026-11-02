package com.example.middleworld.reputation;

import com.example.middleworld.MWConfig;
import com.example.middleworld.network.ReputationSyncPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

/** All reputation changes go through here, so scaling, rivals, feedback and syncing stay consistent. */
public class ReputationManager {

    public static ReputationData data(ServerPlayer player) {
        return player.getData(ModAttachments.REPUTATION);
    }

    /** Works on both sides: the server reads the real data, the client reads the synced copy. */
    public static Rank rank(Player player, Faction faction) {
        if (player.level().isClientSide()) {
            return ClientReputation.get().rank(faction);
        }
        return player.getData(ModAttachments.REPUTATION).rank(faction);
    }

    public static void sync(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new ReputationSyncPayload(data(player)));
    }

    /**
     * Normal gameplay change: applies the config multipliers and spills over to rival factions.
     *
     * @param reason translation key shown in the history list
     */
    public static void change(ServerPlayer player, Faction faction, int rawDelta, String reason,
                              boolean spillToRivals, boolean silent) {
        double mult = rawDelta > 0 ? MWConfig.GAIN_MULTIPLIER.get() : MWConfig.LOSS_MULTIPLIER.get();
        int delta = (int) Math.round(rawDelta * mult);
        if (delta == 0) {
            return;
        }
        int applied = apply(player, faction, delta, reason, silent);
        if (applied != 0 && spillToRivals) {
            double pct = MWConfig.RIVAL_PERCENT.get();
            for (Faction rival : faction.rivals()) {
                // Helping X hurts X's rivals twice as much as hurting X pleases them.
                int spill = applied > 0
                        ? -(int) Math.round(applied * 0.5D * pct)
                        : (int) Math.round(-applied * 0.25D * pct);
                if (spill != 0) {
                    apply(player, rival, spill, "reason.middleworld.rival", true);
                }
            }
        }
    }

    public static void change(ServerPlayer player, Faction faction, int rawDelta, String reason, boolean spillToRivals) {
        change(player, faction, rawDelta, reason, spillToRivals, false);
    }

    /** Applies an exact delta (no multipliers, no rivals). Returns the change actually applied after clamping. */
    public static int apply(ServerPlayer player, Faction faction, int delta, String reason, boolean silent) {
        ReputationData d = data(player);
        int old = d.get(faction);
        Rank oldRank = Rank.of(old);
        d.set(faction, old + delta);
        int applied = d.get(faction) - old;
        if (applied == 0) {
            return 0;
        }
        d.addHistory(faction, applied, reason, player.level().getGameTime());

        if (!silent) {
            feedback(player, faction, applied, reason);
        }

        Rank newRank = d.rank(faction);
        if (newRank != oldRank) {
            announceRankChange(player, faction, oldRank, newRank);
        }
        sync(player);
        return applied;
    }

    private static void feedback(ServerPlayer player, Faction faction, int applied, String reason) {
        boolean gain = applied > 0;
        Component msg = Component.literal((gain ? "+" : "") + applied + " ")
                .append(faction.displayName())
                .append(Component.literal("  "))
                .append(Component.translatable(reason))
                .withStyle(gain ? ChatFormatting.GREEN : ChatFormatting.RED);
        player.displayClientMessage(msg, true);
        player.playNotifySound(gain ? SoundEvents.AMETHYST_BLOCK_CHIME : SoundEvents.VILLAGER_NO,
                SoundSource.PLAYERS, 0.6F, gain ? 1.4F : 0.8F);
        player.serverLevel().sendParticles(
                gain ? ParticleTypes.HAPPY_VILLAGER : ParticleTypes.ANGRY_VILLAGER,
                player.getX(), player.getY() + 2.2D, player.getZ(), 4, 0.3D, 0.1D, 0.3D, 0.0D);
    }

    private static void announceRankChange(ServerPlayer player, Faction faction, Rank oldRank, Rank newRank) {
        Component factionName = faction.displayName().copy().withStyle(s -> s.withColor(faction.color));
        Component rankName = newRank.displayName().copy().withStyle(s -> s.withColor(newRank.color));
        player.sendSystemMessage(Component.translatable("message.middleworld.rank_change", factionName, rankName));
        boolean up = newRank.ordinal() > oldRank.ordinal();
        player.playNotifySound(up ? SoundEvents.PLAYER_LEVELUP : SoundEvents.ANVIL_LAND,
                SoundSource.PLAYERS, 0.5F, up ? 1.0F : 0.7F);
    }
}
