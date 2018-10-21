package org.togetherjava.messaging.messages;

import org.togetherjava.messaging.ComplexMessage;
import org.togetherjava.messaging.MessageCategory;
import org.togetherjava.messaging.SimpleMessage;

public class CommandMessages {

  public static SimpleMessage commandNotFound() {
    return SimpleMessage.error("Command not found");
  }

  public static ComplexMessage commandError(String error) {
    return new ComplexMessage(MessageCategory.ERROR)
        .editMessage(it -> it.setContent("Error executing command:"))
        .editEmbed(it -> it.setDescription(error));
  }
}
