package org.togetherjava.commandrewrite;

import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.greedyPhrase;
import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.literal;

import de.ialistannen.commandprocrastination.autodiscovery.ActiveCommand;
import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import de.ialistannen.commandprocrastination.parsing.ParseException;
import de.ialistannen.commandprocrastination.parsing.SuccessParser;
import org.togetherjava.messaging.SimpleMessage;

@ActiveCommand(name = "ping", parentClass = PrefixedBaseCommand.class)
public class Ping2Command extends CommandNode<CommandContext> {

  public Ping2Command() {
    super(Ping2Command::callMe, SuccessParser.wrapping(literal("pingMe")));
  }

  private static void callMe(CommandContext context) throws ParseException {
    context.getSender().sendMessage(
        SimpleMessage.success(context.shift(greedyPhrase())),
        context.getRequestContext().getMessage().getChannel()
    );
  }
}
