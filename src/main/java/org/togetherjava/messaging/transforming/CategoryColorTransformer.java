package org.togetherjava.messaging.transforming;

import java.awt.Color;
import org.togetherjava.messaging.ComplexMessage;

public class CategoryColorTransformer implements Transformer<ComplexMessage, ComplexMessage> {

  @Override
  public ComplexMessage transform(ComplexMessage complexMessage) {
    return complexMessage.editEmbed(it -> {
      switch (complexMessage.getCategory()) {
        case ERROR:
          it.setColor(new Color(255, 99, 71));
          break;
        case SUCCESS:
          it.setColor(new Color(77, 252, 2));
          break;
        case INFORMATION:
          it.setColor(new Color(65, 105, 225));
          break;
        case NONE:
          break;
      }
    });
  }
}
