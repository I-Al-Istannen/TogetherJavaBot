package org.togetherjava.commandrewrite;

import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.literal;

import com.moandjiezana.toml.Toml;
import de.ialistannen.commandprocrastination.command.execution.CommandExecutor;
import de.ialistannen.commandprocrastination.command.tree.CommandFinder;
import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import de.ialistannen.commandprocrastination.parsing.SuccessParser;
import de.ialistannen.commandprocrastination.util.StringReader;
import org.togetherjava.messaging.sending.MessageSender;

public class JdaExecutor extends CommandExecutor<CommandContext, JdaRequestContext> {

  private Toml config;
  private MessageSender sender;

  public JdaExecutor(CommandFinder<CommandContext> finder, Toml config, MessageSender sender) {
    super(finder, SuccessParser.wrapping(literal(" ")));
    this.config = config;
    this.sender = sender;
  }

  @Override
  protected CommandContext createContext(StringReader stringReader,
      CommandNode<CommandContext> commandNode, JdaRequestContext requestContext) {
    return new CommandContext(stringReader, commandNode, config, sender, requestContext);
  }
}
