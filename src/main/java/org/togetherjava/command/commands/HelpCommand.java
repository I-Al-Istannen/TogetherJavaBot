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
import org.togetherjava.messaging.BotMessage.MessageCategory;
import org.togetherjava.messaging.ComplexMessage;
import org.togetherjava.messaging.messages.CommandMessages;
import org.togetherjava.messaging.transforming.VerionInfoFooterTransformer;

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

    String prefix = source.getContext().getCommandListener().getPrefix();

    ComplexMessage complexMessage = new ComplexMessage(MessageCategory.ERROR)
        .editEmbed(it -> it.setTitle("Available commands:"))
        .applyTransformer(new VerionInfoFooterTransformer())
        .notSelfDestructing();

    for (String smartUsage : dispatcher.getSmartUsage(dispatcher.getRoot(), source).values()) {
      String usage = prefix + smartUsage.replace("|", " | ");
      complexMessage.editEmbed(it ->
          it.addField(usage, "", false)
      );
    }

    source.getMessageSender().sendMessage(
        complexMessage,
        source.getChannel()
    );

    return 0;
  }

  private int showOneCommandHelp(CommandDispatcher<CommandSource> dispatcher, CommandSource source,
      String commandName) {

    List<String> path = Arrays.asList(commandName.split(CommandDispatcher.ARGUMENT_SEPARATOR));
    CommandNode<CommandSource> node = dispatcher.findNode(path);

    if (node == null) {
      source.getMessageSender()
          .sendMessage(CommandMessages.commandNotFound(commandName), source.getChannel());
      return 1;
    }

    String content = "";

    Collection<String> smartUsages = dispatcher.getSmartUsage(node, source).values();
    if (!smartUsages.isEmpty()) {
      content += "**Usage:**" + smartUsages.stream()
          .map(s -> "`" + commandName + " " + s + "`")
          .collect(Collectors.joining("\n", "\n", ""));
    }

    content += "\n\n**Usage text:**\n" + node.getUsageText();

    final String finalContent = content;

    ComplexMessage complexMessage = new ComplexMessage(MessageCategory.SUCCESS)
        .editEmbed(it -> it.setTitle("Help for " + commandName))
        .editEmbed(it -> it.setDescription(finalContent))
        .notSelfDestructing();

    source.getMessageSender().sendMessage(
        complexMessage,
        source.getChannel()
    );

    return 0;
  }
}
