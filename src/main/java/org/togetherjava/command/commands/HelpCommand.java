package org.togetherjava.command.commands;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static org.togetherjava.command.CommandGenericHelper.argument;
import static org.togetherjava.command.CommandGenericHelper.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.togetherjava.command.CommandSource;
import org.togetherjava.command.TJCommand;
import org.togetherjava.messaging.ComplexMessage;
import org.togetherjava.messaging.MessageCategory;
import org.togetherjava.messaging.messages.CommandMessages;

public class HelpCommand implements TJCommand {

  @Override
  public LiteralCommandNode<CommandSource> getCommand(CommandDispatcher<CommandSource> dispatcher) {
    return literal("help")
        .executes(context -> showAllCommandsHelp(dispatcher, context.getSource()))
        .then(
            argument("name", greedyString())
                .executes(context -> showOneCommandHelp(
                    dispatcher,
                    context.getSource(),
                    context.getArgument("name", String.class)
                ))
        )
        .build();
  }

  private int showAllCommandsHelp(CommandDispatcher<CommandSource> dispatcher,
      CommandSource source) {
    String usage = dispatcher.getSmartUsage(dispatcher.getRoot(), source).values().stream()
        .map(s -> "`" + s + "`")
        .collect(Collectors.joining("\n"));

    source.getMessageSender().sendMessage(
        new ComplexMessage(MessageCategory.ERROR)
            .editEmbed(it -> it.setTitle("Available commands:"))
            .editEmbed(it -> it.setDescription(usage)),
        source.getChannel()
    );

    return 0;
  }

  private int showOneCommandHelp(CommandDispatcher<CommandSource> dispatcher, CommandSource source,
      String commandName) {

    List<String> path = Arrays.asList(commandName.split(CommandDispatcher.ARGUMENT_SEPARATOR));
    CommandNode<CommandSource> node = dispatcher.findNode(path);

    if (node == null) {
      source.getMessageSender().sendMessage(CommandMessages.commandNotFound(), source.getChannel());
      return 1;
    }

    String content = "";

    Collection<String> smartUsages = dispatcher.getSmartUsage(node, source).values();
    if (!smartUsages.isEmpty()) {
      content += "**Arguments:**" + smartUsages.stream()
          .map(s -> "`" + s + "`")
          .collect(Collectors.joining(" || ", "\n", ""));
    }

    content += "\n\n**Usage text:**\n" + node.getUsageText();

    if (!node.getExamples().isEmpty()) {
      content += "\n\n**Examples:**\n" + node.getExamples().stream()
          .map(s -> "`" + s + "`")
          .collect(Collectors.joining("\n"));
    }

    final String finalContent = content;

    ComplexMessage complexMessage = new ComplexMessage(MessageCategory.SUCCESS)
        .editEmbed(it -> it.setTitle("Help for " + commandName))
        .editEmbed(it -> it.setDescription(finalContent));

    source.getMessageSender().sendMessage(
        complexMessage,
        source.getChannel()
    );

    return 0;
  }
}
