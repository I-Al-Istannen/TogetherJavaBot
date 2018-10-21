package org.togetherjava.messaging;

public class SimpleMessage {

  private String content;
  private MessageCategory category;

  public SimpleMessage(MessageCategory category, String content) {
    this.content = content;
    this.category = category;
  }

  public String getContent() {
    return content;
  }

  public MessageCategory getCategory() {
    return category;
  }

  public static SimpleMessage error(String message) {
    return new SimpleMessage(MessageCategory.ERROR, message);
  }

  public static SimpleMessage information(String message) {
    return new SimpleMessage(MessageCategory.INFORMATION, message);
  }

  public static SimpleMessage success(String message) {
    return new SimpleMessage(MessageCategory.SUCCESS, message);
  }

  @Override
  public String toString() {
    return "SimpleMessage{" +
        "content='" + content + '\'' +
        ", category=" + category +
        '}';
  }
}
