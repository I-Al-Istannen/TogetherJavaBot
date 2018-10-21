package org.togetherjava.messaging.sending;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import org.togetherjava.messaging.ComplexMessage;
import org.togetherjava.messaging.SimpleMessage;
import org.togetherjava.messaging.transforming.Transformer;

public abstract class TransformingMessageSender implements MessageSender {

  private Transformer<SimpleMessage, Message> simpleMessageTransformer;
  private Transformer<ComplexMessage, Message> complexMessageTransformer;

  public TransformingMessageSender(
      Transformer<SimpleMessage, Message> simpleMessageTransformer,
      Transformer<ComplexMessage, Message> complexMessageTransformer) {
    this.simpleMessageTransformer = simpleMessageTransformer;
    this.complexMessageTransformer = complexMessageTransformer;
  }

  @Override
  public void sendMessage(SimpleMessage message, MessageChannel channel) {
    sendMessage(simpleMessageTransformer.transform(message), message.getCategory(), channel);
  }

  @Override
  public void sendMessage(ComplexMessage message, MessageChannel channel) {
    sendMessage(complexMessageTransformer.transform(message), message.getCategory(), channel);
  }
}
