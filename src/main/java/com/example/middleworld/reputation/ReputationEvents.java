package com.example.middleworld.reputation;

import com.example.middleworld.MWConfig;
import com.example.middleworld.MiddleWorld;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import com.example.middleworld.entity.FactionMob;
import com.example.middleworld.entity.Role;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Everything that changes or reacts to reputation during gameplay. */
@EventBusSubscriber(modid = MiddleWorld.MODID)
public class ReputationEvents {

    // ------------------------------------------------------------------
    // Syncing
    // ------------------------------------------------------------------

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ReputationManager.sync(player);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ReputationManager.sync(player);
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ReputationManager.sync(player);
        }
    }

    // ------------------------------------------------------------------
    // Killing faction members
    // ------------------------------------------------------------------

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        LivingEntity victim = event.getEntity();
        Faction faction = Factions.of(victim);
        if (faction == null) {
            return;
        }

        ServerLevel level = player.serverLevel();
        Raid raid = level.getRaidAt(victim.blockPosition());
        boolean raidActive = raid != null && raid.isStarted() && !raid.isOver();
        boolean selfDefense = victim instanceof Mob mob && mob.getTarget() == player;

        switch (faction) {
            case VILLAGERS -> {
                int base = victim instanceof IronGolem ? -60 : victim instanceof WanderingTrader ? -40 : -50;
                String reason = victim instanceof IronGolem ? "reason.middleworld.kill_golem"
                        : victim instanceof WanderingTrader ? "reason.middleworld.kill_trader"
                        : "reason.middleworld.kill_villager";
                ReputationManager.change(player, Faction.VILLAGERS, penalty(base, selfDefense), reason, true);
                if (raidActive && !selfDefense) {
                    // Helping the raiders earns their respect.
                    ReputationManager.change(player, Faction.PILLAGERS, 20, "reason.middleworld.helped_raiders", false);
                }
            }
            case PILLAGERS -> {
                boolean captain = victim instanceof Raider raider && raider.isPatrolLeader();
                boolean raiding = raidActive || (victim instanceof Raider r && r.hasActiveRaid());
                if (raiding) {
                    ReputationManager.change(player, Faction.VILLAGERS, captain ? 30 : 12,
                            "reason.middleworld.defend_raid", false);
                    ReputationManager.change(player, Faction.PILLAGERS, penalty(captain ? -35 : -15, selfDefense),
                            captain ? "reason.middleworld.kill_captain" : "reason.middleworld.kill_pillager", false);
                } else {
                    ReputationManager.change(player, Faction.PILLAGERS, penalty(captain ? -35 : -12, selfDefense),
                            captain ? "reason.middleworld.kill_captain" : "reason.middleworld.kill_pillager", true);
                }
            }
            case PIGLINS -> ReputationManager.change(player, Faction.PIGLINS,
                    penalty(victim instanceof PiglinBrute ? -30 : -20, selfDefense),
                    "reason.middleworld.kill_piglin", true);
            case GOBLINS, ELVES, DWARVES -> {
                boolean leader = victim instanceof FactionMob mob && mob.role() == Role.CAPTAIN;
                int base = faction == Faction.GOBLINS ? -10 : -15;
                if (leader) {
                    base *= 3;
                }
                String reason = faction == Faction.GOBLINS ? "reason.middleworld.kill_goblin"
                        : faction == Faction.ELVES ? "reason.middleworld.kill_elf"
                        : "reason.middleworld.kill_dwarf";
                ReputationManager.change(player, faction, penalty(base, selfDefense), reason, true);
            }
            default -> { }
        }
    }

    /** Killing something that was already attacking you costs a quarter as much. */
    private static int penalty(int base, boolean selfDefense) {
        return selfDefense ? Math.min(-1, base / 4) : base;
    }

    // ------------------------------------------------------------------
    // Hitting faction members
    // ------------------------------------------------------------------

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        LivingEntity victim = event.getEntity();
        Faction faction = Factions.of(victim);
        if (faction == null || victim == player) {
            return;
        }
        if (victim instanceof Mob mob && mob.getTarget() == player) {
            return; // self-defense
        }
        if (victim instanceof Raider raider && raider.hasActiveRaid()) {
            return; // fighting raiders is encouraged
        }
        int penalty = MWConfig.HIT_PENALTY.get();
        if (penalty > 0) {
            ReputationManager.change(player, faction, -penalty, "reason.middleworld.hit_member", false);
        }
    }

    // ------------------------------------------------------------------
    // Trading, raids, theft
    // ------------------------------------------------------------------

    @SubscribeEvent
    public static void onTrade(TradeWithVillagerEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ReputationData d = ReputationManager.data(player);
        long day = player.level().getDayTime() / 24000L;
        if (d.tradeDay != day) {
            d.tradeDay = day;
            d.tradeGain = 0;
        }
        if (d.tradeGain >= MWConfig.TRADE_DAILY_CAP.get()) {
            return;
        }
        d.tradeGain++;
        ReputationManager.change(player, Faction.VILLAGERS, 1, "reason.middleworld.trade", false);
    }

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if (event.getEntity() instanceof ServerPlayer player
                && event.getEffectInstance().getEffect().equals(MobEffects.HERO_OF_THE_VILLAGE)) {
            ReputationManager.change(player, Faction.VILLAGERS, 40, "reason.middleworld.hero", false);
            ReputationManager.change(player, Faction.PILLAGERS, -25, "reason.middleworld.hero_raiders", false);
        }
    }

    /** Opening an unlooted loot chest while villagers are close by counts as stealing from the village. */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        Level level = event.getLevel();
        BlockEntity be = level.getBlockEntity(event.getPos());
        if (be instanceof RandomizableContainer container && container.getLootTable() != null) {
            boolean witnessed = !level.getEntitiesOfClass(Villager.class,
                    new AABB(event.getPos()).inflate(20.0D)).isEmpty();
            if (witnessed) {
                ReputationManager.change(player, Faction.VILLAGERS, -15, "reason.middleworld.theft", true);
            }
        }
    }

    // ------------------------------------------------------------------
    // Villager interaction: refusal when Hostile, price changes otherwise
    // ------------------------------------------------------------------

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Entity target = event.getTarget();
        if (!(target instanceof Villager) && !(target instanceof WanderingTrader)) {
            return;
        }
        Player player = event.getEntity();
        Rank rank = ReputationManager.rank(player, Faction.VILLAGERS);

        if (rank == Rank.HOSTILE && MWConfig.VILLAGERS_REFUSE_HOSTILE.get()) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            if (!player.level().isClientSide() && event.getHand() == InteractionHand.MAIN_HAND) {
                target.playSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
                player.displayClientMessage(Component.translatable("message.middleworld.villager_refuses"), true);
            }
            return;
        }

        // Vanilla villager prices already react to "gossip" about a player. We feed it our reputation.
        if (!player.level().isClientSide() && event.getHand() == InteractionHand.MAIN_HAND
                && target instanceof Villager villager) {
            applyPriceGossip(villager, player.getUUID(), rank);
        }
    }

    private static void applyPriceGossip(Villager villager, UUID playerId, Rank rank) {
        GossipContainer gossips = villager.getGossips();
        if (rank.atLeast(Rank.FRIENDLY)) {
            int target = rank == Rank.FRIENDLY ? 5 : rank == Rank.HONORED ? 15 : 25; // discount strength
            int current = gossips.getReputation(playerId, type -> type == GossipType.TRADING);
            if (current < target) {
                gossips.add(playerId, GossipType.TRADING, target - current);
            }
        } else if (rank == Rank.UNFRIENDLY) {
            int target = 15; // surcharge strength
            int current = -gossips.getReputation(playerId, type -> type == GossipType.MINOR_NEGATIVE);
            if (current < target) {
                gossips.add(playerId, GossipType.MINOR_NEGATIVE, target - current);
            }
        }
    }

    // ------------------------------------------------------------------
    // Mob behavior
    // ------------------------------------------------------------------

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof IronGolem golem)) {
            return;
        }
        boolean alreadyAdded = golem.targetSelector.getAvailableGoals().stream()
                .anyMatch(wrapped -> wrapped.getGoal() instanceof ReputationTargetGoal);
        if (!alreadyAdded) {
            golem.targetSelector.addGoal(2, new ReputationTargetGoal(golem));
        }
    }

    /** Friendly (or better) players are left alone by that faction's mobs, unless they attacked first. */
    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (!MWConfig.FRIENDLY_MOBS_STAY_CALM.get()) {
            return;
        }
        LivingEntity mob = event.getEntity();
        if (mob.level().isClientSide() || !(event.getNewAboutToBeSetTarget() instanceof ServerPlayer player)) {
            return;
        }
        Faction faction = Factions.of(mob);
        if (faction == null || !ReputationManager.rank(player, faction).atLeast(Rank.FRIENDLY)) {
            return;
        }
        boolean provoked = mob.getLastHurtByMob() == player
                && mob.tickCount - mob.getLastHurtByMobTimestamp() < 200;
        if (!provoked) {
            event.setNewAboutToBeSetTarget(null);
        }
    }

    // ------------------------------------------------------------------
    // Recovery: bad reputation slowly heals, so nothing is permanent
    // ------------------------------------------------------------------

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 100 != 0) {
            return;
        }
        long day = player.level().getDayTime() / 24000L;
        ReputationData d = ReputationManager.data(player);
        if (day <= d.lastRecoveryDay) {
            return;
        }
        d.lastRecoveryDay = day;
        int recovery = MWConfig.RECOVERY_PER_DAY.get();
        if (recovery <= 0) {
            return;
        }
        for (Faction f : Faction.values()) {
            int value = d.get(f);
            if (value < 0) {
                ReputationManager.apply(player, f, Math.min(recovery, -value), "reason.middleworld.recovery", true);
            }
        }
    }

    // ------------------------------------------------------------------
    // Commands
    // ------------------------------------------------------------------

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ReputationCommand.register(event.getDispatcher());
    }
}
