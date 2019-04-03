package org.togetherjava.command.commands;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static org.togetherjava.command.CommandGenericHelper.argument;
import static org.togetherjava.command.CommandGenericHelper.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.togetherjava.command.CommandSource;
import org.togetherjava.command.TJCommand;
import org.togetherjava.messaging.BotMessage.MessageCategory;
import org.togetherjava.messaging.ComplexMessage;
import org.togetherjava.messaging.messages.CommandMessages;
import org.togetherjava.messaging.transforming.VersionInfoFooterTransformer;
import org.togetherjava.util.StringUtils;

public class HelpCommand implements TJCommand {

  @Override
  public LiteralCommandNode<CommandSource> getCommand(CommandDispatcher<CommandSource> dispatcher) {
    return literal("help")
        .shortDescription("Shows this help page or help for a single command.")
        .executes(context -> showAllCommandsHelp(dispatcher, context.getSource()))
        .then(
            argument("name", greedyString())
                .shortDescription("Shows a help page for the given command.")
                .executes(context -> showOneCommandHelp(
                    dispatcher,
                    context.getSource(),
                    context.getArgument("name", String.class)
                ))
        )
        .build();
  }

  private static int showAllCommandsHelp(CommandDispatcher<CommandSource> dispatcher,
      CommandSource source) {

    String prefix = source.getContext().getCommandListener().getPrefix();

    ComplexMessage complexMessage = new ComplexMessage(MessageCategory.ERROR)
        .notSelfDestructing()
        .editEmbed(it -> it.setTitle("Available commands:"))
        .editEmbed(it -> new VersionInfoFooterTransformer().transform(it));

    for (var usageEntry : dispatcher.getSmartUsage(dispatcher.getRoot(), source).entrySet()) {
      String usage = prefix + usageEntry.getValue().replace("|", " | ");
      complexMessage.editEmbed(it ->
          it.addField(usage, usageEntry.getKey().getShortDescription(), false)
      );
    }

    source.getMessageSender().sendMessage(
        complexMessage,
        source.getChannel()
    );

    return 0;
  }

  private static int showOneCommandHelp(CommandDispatcher<CommandSource> dispatcher,
      CommandSource source,
      String commandName) {

    List<String> path = Arrays.asList(commandName.split(CommandDispatcher.ARGUMENT_SEPARATOR));
    CommandNode<CommandSource> node = dispatcher.findNode(path);

    return showOneCommandHelp(dispatcher, source, commandName, node);
  }

  public static int showOneCommandHelp(CommandDispatcher<CommandSource> dispatcher,
      CommandSource source, String commandName, CommandNode<CommandSource> node) {
    if (node == null) {
      source.getMessageSender()
          .sendMessage(CommandMessages.commandNotFound(commandName), source.getChannel());
      return 1;
    }

    ComplexMessage complexMessage = new ComplexMessage(MessageCategory.SUCCESS)
        .notSelfDestructing();

    complexMessage.editEmbed(eb -> eb.setDescription(
        "__**Help for " + commandName + "**:__\n\n" + StringUtils.ZERO_WIDTH_SPACE
    ));

    Map<CommandNode<CommandSource>, String> smartUsages = dispatcher.getSmartUsage(node, source);
    if (!smartUsages.isEmpty()) {
      for (var entry : smartUsages.entrySet()) {
        complexMessage.editEmbed(eb -> eb.addField(
            commandName + " " + entry.getValue(),
            entry.getKey().getShortDescription(),
            false
        ));
      }
    }

    if (!node.getLongDescription().isEmpty() || !node.getShortDescription().isEmpty()) {
      String description = node.getLongDescription().isEmpty()
          ? node.getShortDescription()
          : node.getLongDescription();

      complexMessage.editEmbed(eb -> eb.addField("Description", description, false));
    }

    source.getMessageSender().sendMessage(
        complexMessage,
        source.getChannel()
    );

    return 0;
  }
}
