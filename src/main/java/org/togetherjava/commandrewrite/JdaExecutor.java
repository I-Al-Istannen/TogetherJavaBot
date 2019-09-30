package org.togetherjava.commandrewrite;

import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.literal;

import com.moandjiezana.toml.Toml;
import de.ialistannen.commandprocrastination.command.execution.CommandExecutor;
import de.ialistannen.commandprocrastination.command.tree.CommandFinder;
import de.ialistannen.commandprocrastination.parsing.SuccessParser;
import org.togetherjava.commandrewrite.CommandContext.JdaRequestContext;
import org.togetherjava.messaging.sending.MessageSender;
import org.togetherjava.reactions.ReactionListener;
import org.togetherjava.storage.sql.Database;

/**
 * A command executor for JDA.
 */
class JdaExecutor extends CommandExecutor<CommandContext, JdaRequestContext> {

  private final Toml config;
  private final MessageSender sender;
  private final ReactionListener reactionListener;
  private final Database database;

  JdaExecutor(CommandFinder<CommandContext> finder, Toml config, MessageSender sender,
      ReactionListener reactionListener, Database database) {
    super(finder, SuccessParser.wrapping(literal(" ")));
    this.config = config;
    this.sender = sender;
    this.reactionListener = reactionListener;
    this.database = database;
  }

  @Override
  protected CommandContext createContext(JdaRequestContext requestContext) {
    return new CommandContext(requestContext, config, sender, reactionListener, database);
  }
}
