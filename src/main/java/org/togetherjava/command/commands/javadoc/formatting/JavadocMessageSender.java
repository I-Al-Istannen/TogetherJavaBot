package org.togetherjava.command.commands.javadoc.formatting;

import de.ialistannen.htmljavadocparser.model.doc.JavadocComment;
import de.ialistannen.htmljavadocparser.model.properties.JavadocElement;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.togetherjava.command.CommandSource;
import org.togetherjava.messaging.BotMessage.MessageCategory;
import org.togetherjava.messaging.ComplexMessage;
import org.togetherjava.messaging.SimpleMessage;

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
   * @param source the command source to send it to
   */
  public void sendResult(List<? extends JavadocElement> elements, CommandSource source) {
    if (elements.isEmpty()) {
      sendNothingFound(source);
      return;
    }
    if (elements.size() == 1) {
      sendSingleJavadoc(source, elements.get(0));
      return;
    }
    sendMultipleFoundError(source, elements);
  }

  /**
   * Sends a single javadoc result.
   *
   * @param source the source
   * @param element the element
   */
  public void sendSingleJavadoc(CommandSource source, JavadocElement element) {
    ComplexMessage message = new ComplexMessage(MessageCategory.NONE);
    Optional<JavadocComment> javadoc = element.getJavadoc();
    if (javadoc.isEmpty()) {
      message.setCategory(MessageCategory.ERROR)
          .editEmbed(eb -> eb.setDescription("No javadoc found on that element"));
    } else {
      messageFormatter.format(message, element);
    }

    source.getMessageSender().sendMessage(
        message, source.getChannel()
    );
  }

  /**
   * Sends an error message saying that no types were found.
   *
   * @param commandSource the command source
   */
  public void sendNothingFound(CommandSource commandSource) {
    commandSource.getMessageSender().sendMessage(
        SimpleMessage.error("Nothing found :("),
        commandSource.getChannel()
    );
  }

  /**
   * Sends an error with a list of the found elements.
   *
   * @param source the command source
   * @param foundElements the found elements
   */
  public void sendMultipleFoundError(CommandSource source,
      List<? extends JavadocElement> foundElements) {
    String types = foundElements.stream()
        .map(JavadocElement::getFullyQualifiedName)
        .map(s -> "`" + s + "`")
        .limit(10)
        .collect(Collectors.joining("\n**\\*** ", "**\\*** ", ""));

    ComplexMessage message = new ComplexMessage(MessageCategory.ERROR)
        .editEmbed(eb -> eb.setTitle("I found at least the following types:"))
        .editEmbed(eb -> eb.setDescription(types));

    source.getMessageSender().sendMessage(
        message, source.getChannel()
    );
  }
}
