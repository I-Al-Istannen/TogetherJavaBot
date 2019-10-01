package org.togetherjava.command.commands;

import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.literal;

import com.moandjiezana.toml.Toml;
import de.ialistannen.commandprocrastination.autodiscovery.ActiveCommand;
import de.ialistannen.commandprocrastination.command.Command;
import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import de.ialistannen.commandprocrastination.command.tree.data.DefaultDataKey;
import de.ialistannen.commandprocrastination.parsing.SuccessParser;
import de.ialistannen.commandprocrastination.parsing.defaults.OptionParser;
import java.util.List;
import org.togetherjava.command.CommandContext;

@ActiveCommand(name = "prefixed-base")
public class PrefixedBaseCommand extends CommandNode<CommandContext> {

  public PrefixedBaseCommand(CommandContext context) {
    super(Command.nop(), getParserForPrefix(context.getConfig()));

    setData(DefaultDataKey.NO_ARGUMENT_SEPARATOR, true);
  }

  private static SuccessParser getParserForPrefix(Toml config) {
    List<String> prefixes = config.getList("commands.prefixes");
    OptionParser<Void> parser = new OptionParser<>();

    for (String prefix : prefixes) {
      parser = parser.or(literal(prefix));
    }

    return SuccessParser.wrapping(parser);
  }
}
