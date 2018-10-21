package org.togetherjava.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.togetherjava.command.CommandGenericHelper;
import org.togetherjava.command.CommandSource;
import org.togetherjava.command.TJCommand;
import org.togetherjava.messaging.SimpleMessage;

public class EchoCommand implements TJCommand {

  @Override
  public LiteralCommandNode<CommandSource> getCommand(CommandDispatcher<CommandSource> dispatcher) {
    return CommandGenericHelper.literal("echo")
        .then(
            CommandGenericHelper.argument("message", StringArgumentType.greedyString())
                .executes(context -> {
                  CommandSource source = context.getSource();
                  String argument = context.getArgument("message", String.class);

                  source.getMessageSender()
                      .sendMessage(SimpleMessage.information(argument), source.getChannel());
                  return 0;
                })
        )
        .build();
  }
}
