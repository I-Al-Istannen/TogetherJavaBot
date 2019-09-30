package org.togetherjava.commandrewrite;

import com.moandjiezana.toml.Toml;
import de.ialistannen.commandprocrastination.context.GlobalContext;
import de.ialistannen.commandprocrastination.context.RequestContext;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import org.togetherjava.messaging.sending.MessageSender;
import org.togetherjava.reactions.ReactionListener;

/**
 * The context for commands.
 */
public class CommandContext extends GlobalContext {

  private Toml config;
  private MessageSender sender;
  private ReactionListener reactionListener;
  private JdaRequestContext requestContext;

  /**
   * Creates a new command context.
   *
   * @param requestContext the request context that is specific this this request
   * @param config the config
   * @param sender the sender
   * @param reactionListener the reaction listener
   */
  public CommandContext(JdaRequestContext requestContext, Toml config,
      MessageSender sender,
      ReactionListener reactionListener) {
    super(requestContext);
    this.config = config;
    this.sender = sender;
    this.reactionListener = reactionListener;
    this.requestContext = requestContext;
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
