package org.togetherjava.command.commands;

import static org.togetherjava.command.CommandGenericHelper.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.togetherjava.command.CommandSource;
import org.togetherjava.command.TJCommand;
import org.togetherjava.messaging.BotMessage.MessageCategory;
import org.togetherjava.messaging.SimpleMessage;

public class PingCommand implements TJCommand {

  @Override
  public LiteralCommandNode<CommandSource> getCommand(CommandDispatcher<CommandSource> dispatcher) {
    return literal("ping")
        .shortDescription("Pongs! you, if the bot is online.")
        .then(
            literal("error")
                .shortDescription("Displays a pong with the 'error' category.")
                .executes(sendMessage(MessageCategory.ERROR))
        )
        .then(
            literal("information")
                .shortDescription("Displays a pong with the 'information' category.")
                .executes(sendMessage(MessageCategory.INFORMATION))
        )
        .then(
            literal("success")
                .shortDescription("Displays a pong with the 'success' category.")
                .executes(sendMessage(MessageCategory.SUCCESS))
        )
        .then(
            literal("none")
                .shortDescription("Displays a pong with the 'none' category.")
                .executes(sendMessage(MessageCategory.NONE))
        )
        .build();
  }

  private static Command<CommandSource> sendMessage(MessageCategory category) {
    return context -> sendMessage(category, context);
  }

  private static int sendMessage(MessageCategory category, CommandContext<CommandSource> context) {
    CommandSource source = context.getSource();
    source.getMessageSender()
        .sendMessage(new SimpleMessage(category, "Pong!"), source.getChannel());
    return 0;
  }
}
