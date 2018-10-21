package org.togetherjava.messaging.sending;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import org.togetherjava.messaging.ComplexMessage;
import org.togetherjava.messaging.MessageCategory;
import org.togetherjava.messaging.SimpleMessage;

public interface MessageSender {

  /**
   * Sends a given message.
   *
   * @param message the message to send
   * @param category the {@link MessageCategory}
   * @param channel the channel to send it to
   */
  void sendMessage(Message message, MessageCategory category, MessageChannel channel);

  /**
   * Sends the given {@link SimpleMessage}.
   *
   * @param message the message to send
   * @param channel the channel to send it to
   */
  void sendMessage(SimpleMessage message, MessageChannel channel);

  /**
   * Sends the given {@link ComplexMessage}.
   *
   * @param message the message to send
   * @param channel the channel to send it to
   */
  default void sendMessage(ComplexMessage message, MessageChannel channel) {
    sendMessage(message.build(), message.getCategory(), channel);
  }
}
