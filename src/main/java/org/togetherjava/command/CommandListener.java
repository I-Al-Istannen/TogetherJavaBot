package org.togetherjava.command;

import com.moandjiezana.toml.Toml;
import de.ialistannen.commandprocrastination.autodiscovery.CommandDiscovery;
import de.ialistannen.commandprocrastination.command.execution.AbnormalCommandResultException;
import de.ialistannen.commandprocrastination.command.execution.CommandException;
import de.ialistannen.commandprocrastination.command.execution.CommandNotFoundException;
import de.ialistannen.commandprocrastination.command.tree.CommandFinder;
import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import de.ialistannen.commandprocrastination.parsing.ParseException;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.togetherjava.command.CommandContext.JdaRequestContext;
import org.togetherjava.messaging.SimpleMessage;
import org.togetherjava.messaging.sending.MessageSender;
import org.togetherjava.reactions.ReactionListener;
import org.togetherjava.storage.sql.Database;

/**
 * A command listener for the new command system.
 */
public class CommandListener extends ListenerAdapter {

  private final Toml config;
  private final MessageSender sender;
  private final ReactionListener reactionListener;
  private final Database database;
  private JdaExecutor executor;
  private final CommandFinder<CommandContext> commandFinder;

  public CommandListener(Toml config, MessageSender sender, ReactionListener reactionListener,
      Database database) {
    this.config = config;
    this.sender = sender;
    this.reactionListener = reactionListener;
    this.database = database;

    CommandNode<CommandContext> rootCommand = new CommandDiscovery().findCommands(
        createBaseContext()
    );
    commandFinder = new CommandFinder<>(rootCommand);

    this.executor = new JdaExecutor(commandFinder, config, sender, reactionListener, database);
  }

  /**
   * Creates a base context. The request context will be null.
   *
   * @return the created base context.
   */
  public CommandContext createBaseContext() {
    // command finder is null only when called from the constructor
    return new CommandContext(null, config, sender, reactionListener, database, commandFinder);
  }

  @Override
  public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
    if (event.getAuthor().isBot()) {
      return;
    }
    String content = event.getMessage().getContentRaw();
    try {
      JdaRequestContext context = new JdaRequestContext(
          event.getMessage(),
          event.getAuthor(),
          event.getGuild()
      );
      executor.execute(content, context);
    } catch (CommandNotFoundException ignored) {
    } catch (ParseException | AbnormalCommandResultException | CommandException e) {
      sender.sendMessage(SimpleMessage.error(e.getMessage()), event.getChannel());
    }
  }
}
