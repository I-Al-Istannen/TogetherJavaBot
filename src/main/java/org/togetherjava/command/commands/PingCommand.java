package org.togetherjava.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.togetherjava.command.CommandGenericHelper;
import org.togetherjava.command.CommandSource;
import org.togetherjava.command.TJCommand;
import org.togetherjava.messaging.MessageCategory;
import org.togetherjava.messaging.SimpleMessage;

public class PingCommand implements TJCommand {

  @Override
  public LiteralCommandNode<CommandSource> getCommand(CommandDispatcher<CommandSource> dispatcher) {
    return CommandGenericHelper.literal("ping")
        .then(
            CommandGenericHelper.literal("error")
                .executes(sendMessage(MessageCategory.ERROR))
        )
        .then(
            CommandGenericHelper.literal("information")
                .executes(sendMessage(MessageCategory.INFORMATION))
        )
        .then(
            CommandGenericHelper.literal("success")
                .executes(sendMessage(MessageCategory.SUCCESS))
        )
        .then(
            CommandGenericHelper.literal("none")
                .executes(sendMessage(MessageCategory.NONE))
        )
        .then(
            RequiredArgumentBuilder.<CommandSource, Integer>argument("number",
                IntegerArgumentType.integer())
                .executes(context ->
                    sendMessage(
                        MessageCategory.SUCCESS,
                        context,
                        "Pong " + context.getArgument("number", Integer.class) + "!"
                    )
                )
        )
        .build();
  }

  private static Command<CommandSource> sendMessage(MessageCategory category) {
    return context -> sendMessage(category, context, "Pong!");
  }

  private static int sendMessage(MessageCategory category, CommandContext<CommandSource> context,
      String text) {
    CommandSource source = context.getSource();
    source.getMessageSender()
        .sendMessage(new SimpleMessage(category, text), source.getChannel());
    return 0;
  }
}
