package org.togetherjava.command.commands;

import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.literal;

import de.ialistannen.commandprocrastination.autodiscovery.ActiveCommand;
import de.ialistannen.commandprocrastination.command.execution.AbnormalCommandResultException;
import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import de.ialistannen.commandprocrastination.command.tree.data.DefaultDataKey;
import de.ialistannen.commandprocrastination.parsing.SuccessParser;
import org.togetherjava.commandrewrite.CommandContext;
import org.togetherjava.messaging.BotMessage.MessageCategory;
import org.togetherjava.messaging.SimpleMessage;

@ActiveCommand(name = "ping", parentClass = PrefixedBaseCommand.class)
public class PingCommand extends CommandNode<CommandContext> {

  public PingCommand() {
    super(SuccessParser.wrapping(literal("ping")));
    setCommand(context -> {
      throw AbnormalCommandResultException.showUsage();
    });

    addSubCommand()
        .head("error")
        .data(DefaultDataKey.SHORT_DESCRIPTION, "A message with error level")
        .executes(context -> sendMessage(context, MessageCategory.ERROR))
        .finish();
    addSubCommand()
        .head("information")
        .data(DefaultDataKey.SHORT_DESCRIPTION, "A message with information level")
        .executes(context -> sendMessage(context, MessageCategory.INFORMATION))
        .finish();
    addSubCommand()
        .head("success")
        .data(DefaultDataKey.SHORT_DESCRIPTION, "A message with success level")
        .executes(context -> sendMessage(context, MessageCategory.SUCCESS))
        .finish();
    addSubCommand()
        .head("none")
        .data(DefaultDataKey.SHORT_DESCRIPTION, "A message with no level")
        .executes(context -> sendMessage(context, MessageCategory.NONE))
        .finish();
  }

  private void sendMessage(CommandContext context, MessageCategory category) {
    context.getMessageSender().sendMessage(
        new SimpleMessage(category, "Pong!"),
        context.getRequestContext().getChannel()
    );
  }
}
