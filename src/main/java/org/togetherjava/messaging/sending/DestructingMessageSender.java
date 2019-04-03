package org.togetherjava.messaging.sending;

import com.moandjiezana.toml.Toml;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import org.togetherjava.messaging.BotMessage;
import org.togetherjava.messaging.BotMessage.DestructionState;
import org.togetherjava.messaging.BotMessage.MessageCategory;
import org.togetherjava.messaging.transforming.Transformer;

/**
 * A Message sender that can delete messages after a timeout.
 */
public class DestructingMessageSender implements MessageSender {

  private Toml toml;
  private final Transformer<BotMessage, Message> messageTransformer;

  public DestructingMessageSender(Toml toml, Transformer<BotMessage, Message> messageTransformer) {
    this.toml = toml;
    this.messageTransformer = messageTransformer;
  }

  @Override
  public void sendMessage(BotMessage message, MessageChannel channel) {
    Message discordMessage = messageTransformer.transform(message);

    channel.sendMessage(discordMessage).queue(destructingConsumer(message));
  }

  private Consumer<Message> destructingConsumer(BotMessage botMessage) {
    return sentMessage -> {
      botMessage.afterSend(sentMessage);

      if (botMessage.getSelfDestructingState() == DestructionState.NOT_DESTRUCTING) {
        return;
      }

      Duration destructionDelay;
      if (botMessage.getSelfDestructingState() == DestructionState.SELF_DESTRUCTING) {
        destructionDelay = botMessage.getSelfDestructDelay();
      } else {
        destructionDelay = defaultDestructionDelay(botMessage.getCategory());
      }

      if (destructionDelay != null) {
        deleteAfter(botMessage, sentMessage, destructionDelay);
      }
    };
  }

  private Duration defaultDestructionDelay(MessageCategory category) {
    if (!toml.getBoolean("messages." + category.name().toLowerCase() + ".self-destruct")) {
      return null;
    }

    Long delay = toml
        .getLong("messages." + category.name().toLowerCase() + ".destruction-delay");

    return Duration.ofSeconds(delay);
  }

  private void deleteAfter(BotMessage botMessage, Message message, Duration duration) {
    message.delete().queueAfter(
        duration.getSeconds(),
        TimeUnit.SECONDS,
        ignored -> botMessage.afterDestruction(message)
    );
  }
}
