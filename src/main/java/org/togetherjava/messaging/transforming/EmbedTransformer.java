package org.togetherjava.messaging.transforming;

import org.togetherjava.messaging.ComplexMessage;
import org.togetherjava.messaging.SimpleMessage;

public class EmbedTransformer implements Transformer<SimpleMessage, ComplexMessage> {

  @Override
  public ComplexMessage transform(SimpleMessage simpleMessage) {
    return new ComplexMessage(simpleMessage.getCategory())
        .editEmbed(it -> it.setDescription(simpleMessage.getContent()));
  }
}
