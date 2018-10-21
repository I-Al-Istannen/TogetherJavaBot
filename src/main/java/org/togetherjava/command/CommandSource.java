package org.togetherjava.command;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import org.togetherjava.messaging.sending.MessageSender;

public class CommandSource {

  private User user;
  private Message message;
  private MessageSender messageSender;
  private MessageChannel channel;

  public CommandSource(Message message, MessageSender messageSender) {
    this.user = message.getAuthor();
    this.message = message;
    this.messageSender = messageSender;
    this.channel = message.getChannel();
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

  public MessageSender getMessageSender() {
    return messageSender;
  }
}
