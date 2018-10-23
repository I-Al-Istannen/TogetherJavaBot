package org.togetherjava.command;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import org.togetherjava.messaging.sending.MessageSender;
import org.togetherjava.util.Context;

public class CommandSource {

  private User user;
  private Message message;
  private MessageChannel channel;
  private Context context;

  public CommandSource(Message message, Context context) {
    this.user = message.getAuthor();
    this.message = message;
    this.channel = message.getChannel();
    this.context = context;
  }

  public User getUser() {
    return user;
  }

  public Message getMessage() {
    return message;
  }

  public MessageChannel getChannel() {
    return channel;
  }

  /**
   * Convenience method for {@link #getContext()#getMessageSender()}
   *
   * @return the {@link MessageSender} to use
   */
  public MessageSender getMessageSender() {
    return context.getMessageSender();
  }

  public Context getContext() {
    return context;
  }
}
