package com.example.middleworld.reputation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Testing helper (operators only):
 *   /middleworld rep villagers set -500
 *   /middleworld rep goblins add 100
 */
public class ReputationCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("middleworld")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("rep")
                        .then(Commands.argument("faction", StringArgumentType.word())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                        Arrays.stream(Faction.values()).map(f -> f.id), builder))
                                .then(Commands.literal("add")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(-2000, 2000))
                                                .executes(ctx -> modify(ctx, false))))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(-1000, 1000))
                                                .executes(ctx -> modify(ctx, true)))))));
    }

    private static int modify(CommandContext<CommandSourceStack> ctx, boolean set) throws CommandSyntaxException {
        String id = StringArgumentType.getString(ctx, "faction");
        Faction faction = Faction.byId(id);
        if (faction == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown faction: " + id));
            return 0;
        }
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        int delta = set ? amount - ReputationManager.data(player).get(faction) : amount;
        ReputationManager.apply(player, faction, delta, "reason.middleworld.command", false);

        int now = ReputationManager.data(player).get(faction);
        Rank rank = Rank.of(now);
        ctx.getSource().sendSuccess(() -> Component.literal(faction.id + " reputation is now " + now + " (" + rank.key() + ")"), false);
        return 1;
    }
}
