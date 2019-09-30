package org.togetherjava.command;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
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

  /**
   * Returns the {@link #getUser()} as a guild {@link Member}.
   *
   * @return the user as a Member
   */
  public Member getMember() {
    return getMessage().getGuild().getMember(getUser());
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
