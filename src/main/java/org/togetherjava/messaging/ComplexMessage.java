package org.togetherjava.messaging;

import java.util.function.Consumer;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;

public class ComplexMessage {

  private EmbedBuilder embedBuilder;
  private MessageBuilder messageBuilder;
  private MessageCategory category;

  public ComplexMessage(MessageCategory category) {
    this.category = category;
    this.embedBuilder = new EmbedBuilder();
    this.messageBuilder = new MessageBuilder();
  }

  /**
   * Returns the underlying {@link EmbedBuilder}.
   *
   * @return the used {@link EmbedBuilder}.
   */
  public EmbedBuilder getEmbedBuilder() {
    return embedBuilder;
  }

  /**
   * Returns the underlying {@link MessageBuilder}.
   *
   * @return the used {@link MessageBuilder}.
   */
  public MessageBuilder getMessageBuilder() {
    return messageBuilder;
  }

  /**
   * Applies the given action to the {@link #getMessageBuilder()}
   *
   * @param action the action to apply
   * @return this object
   */
  public ComplexMessage editMessage(Consumer<MessageBuilder> action) {
    action.accept(getMessageBuilder());
    return this;
  }

  /**
   * Applies the given action to the {@link #getEmbedBuilder()}
   *
   * @param action the action to apply
   * @return this object
   */
  public ComplexMessage editEmbed(Consumer<EmbedBuilder> action) {
    action.accept(getEmbedBuilder());
    return this;
  }

  /**
   * Sets the category for this message.
   *
   * @param category the {@link MessageCategory}
   */
  public void setCategory(MessageCategory category) {
    this.category = category;
  }

  public MessageCategory getCategory() {
    return category;
  }

  public Message build() {
    if (embedBuilder.isEmpty()) {
      return messageBuilder.build();
    }
    return messageBuilder
        .setEmbed(embedBuilder.build())
        .build();
  }
}
