package org.togetherjava.commandrewrite;

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
import org.togetherjava.commandrewrite.CommandContext.JdaRequestContext;
import org.togetherjava.messaging.SimpleMessage;
import org.togetherjava.messaging.sending.MessageSender;
import org.togetherjava.reactions.ReactionListener;
import org.togetherjava.storage.sql.Database;

/**
 * A command listener for the new command system.
 */
public class NewCommandListener extends ListenerAdapter {

  private final MessageSender sender;
  private JdaExecutor executor;

  public NewCommandListener(Toml config, MessageSender sender, ReactionListener reactionListener,
      Database database) {
    this.sender = sender;

    CommandNode<CommandContext> rootCommand = new CommandDiscovery().findCommands(
        new CommandContext(null, config, sender, reactionListener, database)
    );
    CommandFinder<CommandContext> finder = new CommandFinder<>(rootCommand);

    this.executor = new JdaExecutor(finder, config, sender, reactionListener, database);
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
    } catch (CommandNotFoundException e) {
      sender.sendMessage(
          SimpleMessage.error(
              "Command not found! Usage: " + e.getResult().getChain().buildUsage().trim()
          ),
          event.getChannel()
      );
    } catch (ParseException | AbnormalCommandResultException | CommandException e) {
      sender.sendMessage(SimpleMessage.error(e.getMessage()), event.getChannel());
    }
  }
}
