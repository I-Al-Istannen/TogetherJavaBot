package org.togetherjava.messaging;

import java.util.function.Consumer;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;

public class ComplexMessage extends BotMessage<ComplexMessage> {

  private EmbedBuilder embedBuilder;
  private MessageBuilder messageBuilder;

  public ComplexMessage(MessageCategory category) {
    super(category);
    this.embedBuilder = new EmbedBuilder();
    this.messageBuilder = new MessageBuilder();
  }

  @Override
  protected ComplexMessage getSelf() {
    return this;
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
   * Sets the embed builder.
   *
   * @param embedBuilder the embed builder
   * @return this object
   */
  public ComplexMessage withEmbedBuilder(EmbedBuilder embedBuilder) {
    this.embedBuilder = embedBuilder;

    return this;
  }

  /**
   * Builds this Message as a discord {@link MessageBuilder}.
   *
   * @return the created discord message builder
   */
  public MessageBuilder toDiscordMessage() {
    if (embedBuilder.isEmpty()) {
      return messageBuilder;
    }
    return messageBuilder
        .setEmbed(embedBuilder.build());
  }
}
