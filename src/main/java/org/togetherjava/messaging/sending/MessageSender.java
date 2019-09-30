package org.togetherjava.messaging.sending;

import net.dv8tion.jda.api.entities.MessageChannel;
import org.togetherjava.messaging.BotMessage;

@FunctionalInterface
public interface MessageSender {

  /**
   * Sends the given {@link BotMessage}.
   *
   * @param message the message to send
   * @param channel the channel to send it to
   */
  void sendMessage(BotMessage message, MessageChannel channel);
}
