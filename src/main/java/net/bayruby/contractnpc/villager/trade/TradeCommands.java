package net.bayruby.contractnpc.villager.trade;

import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "villager")
public final class TradeCommands {

    private TradeCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("trade")
                        .requires(source -> source.hasPermission(2))
                        .then(
                                Commands.literal("unlock")
                                        .then(
                                                Commands.argument(
                                                                "player",
                                                                EntityArgument.player()
                                                        )
                                                        .then(
                                                                Commands.argument(
                                                                                "tradeid",
                                                                                StringArgumentType.greedyString()
                                                                        )
                                                                        .executes(context -> {
                                                                            ServerPlayer target =
                                                                                    EntityArgument.getPlayer(
                                                                                            context,
                                                                                            "player"
                                                                                    );

                                                                            String tradeId =
                                                                                    StringArgumentType.getString(
                                                                                            context,
                                                                                            "tradeid"
                                                                                    ).trim();

                                                                            return unlockTrade(
                                                                                    context.getSource(),
                                                                                    target,
                                                                                    tradeId
                                                                            );
                                                                        })
                                                        )
                                        )
                        )
        );
    }

    private static int unlockTrade(
            net.minecraft.commands.CommandSourceStack source,
            ServerPlayer target,
            String tradeId
    ) {
        NpcTrade trade = NpcTrades.find(tradeId);

        if (trade == null) {
            source.sendFailure(
                    Component.literal("Unknown trade ID: " + tradeId)
            );
            return 0;
        }

        if (trade.isLocal()) {
            source.sendFailure(
                    Component.literal(
                            "Trade '" + tradeId
                                    + "' is local and unlocks automatically "
                                    + "when its NPC reaches "
                                    + trade.requiredAffection()
                                    + " hearts."
                    )
            );
            return 0;
        }

        PlayerTradeData data =
                PlayerTradeData.get(source.getServer());

        boolean added = data.unlock(target.getUUID(), tradeId);

        if (!added) {
            source.sendSuccess(
                    () -> Component.literal(
                            "Player already has global trade unlocked: "
                                    + tradeId
                    ),
                    false
            );
            return 0;
        }

        source.sendSuccess(
                () -> Component.literal(
                        "Unlocked global trade '" + tradeId
                                + "' for " + target.getGameProfile().getName()
                ),
                true
        );

        return 1;
    }
}