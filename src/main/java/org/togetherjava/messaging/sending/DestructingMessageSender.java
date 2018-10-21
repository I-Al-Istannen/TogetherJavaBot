package org.togetherjava.messaging.sending;

import com.moandjiezana.toml.Toml;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import org.togetherjava.messaging.ComplexMessage;
import org.togetherjava.messaging.MessageCategory;
import org.togetherjava.messaging.SimpleMessage;
import org.togetherjava.messaging.transforming.Transformer;

public class DestructingMessageSender extends TransformingMessageSender {

  private Toml toml;

  public DestructingMessageSender(Toml toml,
      Transformer<SimpleMessage, Message> simpleMessageTransformer,
      Transformer<ComplexMessage, Message> complexMessageTransformer) {

    super(simpleMessageTransformer, complexMessageTransformer);

    this.toml = toml;
  }

  @Override
  public void sendMessage(Message message, MessageCategory category, MessageChannel channel) {
    if (toml.getBoolean("messages." + category.name().toLowerCase() + ".self-destruct")) {
      Long delay = toml
          .getLong("messages." + category.name().toLowerCase() + ".destruction-delay");

      channel.sendMessage(message)
          .queue(sentMessage -> sentMessage.delete().queueAfter(delay, TimeUnit.SECONDS));
    } else {
      channel.sendMessage(message).submit();
    }
  }
}
