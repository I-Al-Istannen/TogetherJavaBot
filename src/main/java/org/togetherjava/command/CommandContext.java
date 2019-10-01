package org.togetherjava.command;

import com.moandjiezana.toml.Toml;
import de.ialistannen.commandprocrastination.command.tree.CommandFinder;
import de.ialistannen.commandprocrastination.context.GlobalContext;
import de.ialistannen.commandprocrastination.context.RequestContext;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import org.togetherjava.messaging.sending.MessageSender;
import org.togetherjava.reactions.ReactionListener;
import org.togetherjava.storage.sql.Database;
import org.togetherjava.util.Messages;

/**
 * The context for commands.
 */
public class CommandContext extends GlobalContext {

  private Toml config;
  private MessageSender sender;
  private ReactionListener reactionListener;
  private JdaRequestContext requestContext;
  private Database database;
  private CommandFinder<CommandContext> commandFinder;
  private Messages messages;

  /**
   * Creates a new command context.
   *
   * @param requestContext the request context that is specific this this request
   * @param config the config
   * @param sender the sender
   * @param reactionListener the reaction listener
   * @param database the database
   * @param commandFinder the command finder
   */
  public CommandContext(JdaRequestContext requestContext, Toml config,
      MessageSender sender,
      ReactionListener reactionListener, Database database,
      CommandFinder<CommandContext> commandFinder) {
    super(requestContext);
    this.config = config;
    this.sender = sender;
    this.reactionListener = reactionListener;
    this.requestContext = requestContext;
    this.database = database;
    this.commandFinder = commandFinder;
    this.messages = new Messages();
  }

  /**
   * Returns the config.
   *
   * @return the config used by the bot
   */
  public Toml getConfig() {
    return config;
  }

  /**
   * Returns the message sender.
   *
   * @return the message sender
   */
  public MessageSender getMessageSender() {
    return sender;
  }

  /**
   * Returns the reaction listener
   *
   * @return th reaction listener
   */
  public ReactionListener getReactionListener() {
    return reactionListener;
  }

  /**
   * Returns the request context.
   *
   * @return the request context
   */
  public JdaRequestContext getRequestContext() {
    return requestContext;
  }

  /**
   * Returns the database.
   *
   * @return the database
   */
  public Database getDatabase() {
    return database;
  }

  /**
   * Returns the command finder.
   *
   * @return the command finder
   */
  public CommandFinder<CommandContext> getCommandFinder() {
    return commandFinder;
  }

  /**
   * Returns the bot's messages.
   *
   * @return the messages
   */
  public Messages getMessages() {
    return messages;
  }

  /**
   * The context for a single request.
   */
  public static class JdaRequestContext extends RequestContext {

    private Message message;
    private User user;
    private Guild guild;
    private MessageChannel channel;

    JdaRequestContext(Message message, User user, Guild guild) {
      this.message = message;
      this.user = user;
      this.guild = guild;
      this.channel = message.getChannel();
    }

    public Message getMessage() {
      return message;
    }

    public MessageChannel getChannel() {
      return channel;
    }

    public User getUser() {
      return user;
    }

    public Guild getGuild() {
      return guild;
    }
  }

}
