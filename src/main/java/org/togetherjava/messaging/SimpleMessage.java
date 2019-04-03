package org.togetherjava.messaging;

import net.dv8tion.jda.core.MessageBuilder;

public class SimpleMessage extends BotMessage<SimpleMessage> {

  private String content;

  public SimpleMessage(MessageCategory category, String content) {
    super(category);
    this.content = content;
  }

  /**
   * Returns the content of this message.
   *
   * @return the message content, i.e. the text that should be displayed
   */
  public String getContent() {
    return content;
  }

  @Override
  protected SimpleMessage getSelf() {
    return this;
  }

  @Override
  public MessageBuilder toDiscordMessage() {
    return new MessageBuilder(getContent());
  }

  /**
   * Creates an error message with the given text.
   *
   * @param message the message to display
   * @return the created message
   */
  public static SimpleMessage error(String message) {
    return new SimpleMessage(MessageCategory.ERROR, message);
  }

  /**
   * Creates an information message with the given text.
   *
   * @param message the message to display
   * @return the created message
   */
  public static SimpleMessage information(String message) {
    return new SimpleMessage(MessageCategory.INFORMATION, message);
  }

  /**
   * Creates a success message with the given text.
   *
   * @param message the message to display
   * @return the created message
   */
  public static SimpleMessage success(String message) {
    return new SimpleMessage(MessageCategory.SUCCESS, message);
  }
}
