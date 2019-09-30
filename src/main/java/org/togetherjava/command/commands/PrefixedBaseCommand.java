package org.togetherjava.command.commands;

import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.literal;

import de.ialistannen.commandprocrastination.autodiscovery.ActiveCommand;
import de.ialistannen.commandprocrastination.command.Command;
import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import de.ialistannen.commandprocrastination.command.tree.data.DefaultDataKey;
import de.ialistannen.commandprocrastination.parsing.SuccessParser;
import org.togetherjava.commandrewrite.CommandContext;

@ActiveCommand(name = "prefixed-base")
public class PrefixedBaseCommand extends CommandNode<CommandContext> {

  public PrefixedBaseCommand() {
    super(Command.nop(), SuccessParser.wrapping(literal("!!")));
    setData(DefaultDataKey.NO_ARGUMENT_SEPARATOR, true);
  }
}
