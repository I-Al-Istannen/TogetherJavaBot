package org.togetherjava.command.commands.javadoc.formatting;

import de.ialistannen.htmljavadocparser.model.doc.JavadocComment;
import de.ialistannen.htmljavadocparser.model.properties.JavadocElement;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.togetherjava.messaging.BotMessage.MessageCategory;
import org.togetherjava.messaging.ComplexMessage;
import org.togetherjava.messaging.SimpleMessage;
import org.togetherjava.messaging.sending.MessageSender;

/**
 * Sends result messages for javadoc queries.
 */
public class JavadocMessageSender {

  private final JavadocMessageFormatter messageFormatter;

  /**
   * Creates a new Javadoc message sender.
   *
   * @param messageFormatter the message formatter to use
   */
  public JavadocMessageSender(JavadocMessageFormatter messageFormatter) {
    this.messageFormatter = messageFormatter;
  }

  /**
   * Sends a message containing the javadoc results. Either a single javadoc, a list with found
   * classes or an error.
   *
   * @param elements the found elements
   * @param sender the message sender
   * @param channel the channel to send it to
   */
  public void sendResult(List<? extends JavadocElement> elements, MessageSender sender,
      MessageChannel channel) {
    if (elements.isEmpty()) {
      sendNothingFound(channel, sender);
      return;
    }
    if (elements.size() == 1) {
      sendSingleJavadoc(channel, sender, elements.get(0));
      return;
    }
    sendMultipleFoundError(channel, sender, elements);
  }

  /**
   * Sends a single javadoc result.
   *
   * @param channel the channel to send it to
   * @param sender the message sender
   * @param element the element
   */
  public void sendSingleJavadoc(MessageChannel channel, MessageSender sender,
      JavadocElement element) {
    ComplexMessage message = new ComplexMessage(MessageCategory.NONE);
    Optional<JavadocComment> javadoc = element.getJavadoc();
    if (javadoc.isEmpty()) {
      message.setCategory(MessageCategory.ERROR)
          .editEmbed(eb -> eb.setDescription("No javadoc found on that element"));
    } else {
      messageFormatter.format(message, element);
    }

    sender.sendMessage(message, channel);
  }

  /**
   * Sends an error message saying that no types were found.
   *
   * @param channel the channel to send it to
   * @param sender the message sender
   */
  public void sendNothingFound(MessageChannel channel, MessageSender sender) {
    sender.sendMessage(SimpleMessage.error("Nothing found :("), channel);
  }

  /**
   * Sends an error with a list of the found elements.
   *
   * @param channel the channel to send it to
   * @param sender the message sender
   * @param foundElements the found elements
   */
  public void sendMultipleFoundError(MessageChannel channel, MessageSender sender,
      List<? extends JavadocElement> foundElements) {
    String types = foundElements.stream()
        .map(JavadocElement::getFullyQualifiedName)
        .map(s -> "`" + s + "`")
        .limit(10)
        .collect(Collectors.joining("\n**\\*** ", "**\\*** ", ""));

    ComplexMessage message = new ComplexMessage(MessageCategory.ERROR)
        .editEmbed(eb -> eb.setTitle("I found at least the following types:"))
        .editEmbed(eb -> eb.setDescription(types));

    sender.sendMessage(message, channel);
  }
}
