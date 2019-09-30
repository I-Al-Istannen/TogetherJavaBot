package org.togetherjava.commandrewrite;

import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.literal;

import com.moandjiezana.toml.Toml;
import de.ialistannen.commandprocrastination.command.execution.CommandExecutor;
import de.ialistannen.commandprocrastination.command.tree.CommandFinder;
import de.ialistannen.commandprocrastination.parsing.SuccessParser;
import org.togetherjava.commandrewrite.CommandContext.JdaRequestContext;
import org.togetherjava.messaging.sending.MessageSender;
import org.togetherjava.reactions.ReactionListener;

/**
 * A command executor for JDA.
 */
class JdaExecutor extends CommandExecutor<CommandContext, JdaRequestContext> {

  private Toml config;
  private MessageSender sender;
  private final ReactionListener reactionListener;

  JdaExecutor(CommandFinder<CommandContext> finder, Toml config, MessageSender sender,
      ReactionListener reactionListener) {
    super(finder, SuccessParser.wrapping(literal(" ")));
    this.config = config;
    this.sender = sender;
    this.reactionListener = reactionListener;
  }

  @Override
  protected CommandContext createContext(JdaRequestContext requestContext) {
    return new CommandContext(requestContext, config, sender, reactionListener);
  }
}
