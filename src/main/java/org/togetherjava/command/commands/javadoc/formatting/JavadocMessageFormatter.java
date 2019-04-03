package org.togetherjava.command.commands.javadoc.formatting;

import de.ialistannen.htmljavadocparser.model.JavadocField;
import de.ialistannen.htmljavadocparser.model.JavadocPackage;
import de.ialistannen.htmljavadocparser.model.properties.Invocable;
import de.ialistannen.htmljavadocparser.model.properties.JavadocElement;
import de.ialistannen.htmljavadocparser.model.types.JavadocClass;
import de.ialistannen.htmljavadocparser.model.types.JavadocEnum;
import de.ialistannen.htmljavadocparser.model.types.JavadocInterface;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.togetherjava.messaging.ComplexMessage;

/**
 * A formatter for javadoc element messages.
 */
public class JavadocMessageFormatter {

  private List<Formatter> formatters;

  /**
   * Creates a new javadoc message formatter.
   */
  public JavadocMessageFormatter() {
    this.formatters = new ArrayList<>();

    formatters.add(Formatter.of(
        element -> element instanceof JavadocClass,
        setColorAndImage(
            new Color(255, 99, 71), // tomato
            "https://www.jetbrains.com/help/img/idea/2019.1/Groovy.icons.groovy.class@2x.png"
        )
    ));

    formatters.add(Formatter.of(
        element -> element instanceof Invocable && !((Invocable) element).isStatic(),
        setColorAndImage(
            Color.YELLOW,
            "https://www.jetbrains.com/help/img/idea/2019.1/icons.nodes.method.svg@2x.png"
        )
    ));
    formatters.add(Formatter.of(
        element -> element instanceof Invocable && ((Invocable) element).isStatic(),
        setColorAndImage(
            Color.ORANGE,
            "https://www.jetbrains.com/help/img/idea/2019.1/icons.nodes.method.svg@2x.png"
        )
    ));

    formatters.add(Formatter.of(
        element -> element instanceof JavadocEnum,
        setColorAndImage(
            new Color(102, 51, 153), // rebecca purple
            "https://www.jetbrains.com/help/img/idea/2019.1/icons.nodes.enum.svg@2x.png"
        )
    ));

    formatters.add(Formatter.of(
        element -> element instanceof JavadocInterface,
        setColorAndImage(
            Color.GREEN,
            "https://www.jetbrains.com/help/img/idea/2019.1/icons.nodes.interface.svg@2x.png"
        )
    ));

    formatters.add(Formatter.of(
        element -> element instanceof JavadocField,
        setColorAndImage(
            new Color(65, 105, 225), // royal blue,
            "https://www.jetbrains.com/help/img/idea/2019.1/icons.nodes.field.svg@2x.png"
        )
    ));

    formatters.add(Formatter.of(
        element -> element instanceof JavadocPackage,
        (message, element) -> setImage(
            message,
            element,
            "https://www.jetbrains.com/help/img/idea/2019.1/icons.nodes.package.svg@2x.png"
        )
    ));

    formatters.add(Formatter.of(
        element -> element instanceof Invocable && ((Invocable) element).isAbstract(),
        (message, element) -> setImage(
            message,
            element,
            "https://www.jetbrains.com/help/img/idea/2019.1/Groovy.icons.groovy.abstractClass@2x.png"
        )
    ));

    formatters.add(Formatter.of(
        element -> element instanceof JavadocClass && element.getSimpleName().endsWith("Exception"),
        (message, element) -> setImage(
            message,
            element,
            "https://www.jetbrains.com/help/img/idea/2019.1/icons.nodes.exceptionClass.svg@2x.png"
        )
    ));

    formatters.add(Formatter.of(
        element -> true,
        (message, element) -> message.getEmbedBuilder()
            .setFooter("Tip: Click the declaration to go to the online javadoc", null)
    ));

    formatters.add(new JavadocDescriptionFormatter());
  }

  private BiConsumer<ComplexMessage, JavadocElement> setColorAndImage(Color color, String url) {
    return (message, element) -> {
      setImage(message, element, url);
      setEmbedColor(message, color);
    };
  }

  private void setEmbedColor(ComplexMessage message, Color color) {
    message.getEmbedBuilder().setColor(color);
  }

  private void setImage(ComplexMessage message, JavadocElement element, String url) {
    message.getEmbedBuilder().setAuthor(element.getDeclaration(), element.getUrl(), url);
  }

  /**
   * Formats a message.
   *
   * @param message the message to format
   * @param element the element to display
   */
  public void format(ComplexMessage message, JavadocElement element) {
    formatters.stream()
        .filter(formatter -> formatter.applies(element))
        .forEach(formatter -> formatter.format(message, element));
  }

  /**
   * A message formatter.
   */
  interface Formatter {

    /**
     * Returns whether the formatter can handle the element.
     *
     * @param element the element to check
     * @return true if the formatter can handle the element
     */
    boolean applies(JavadocElement element);

    /**
     * Formats the given message.
     *
     * @param message the message to format
     * @param element the element that is formatted
     */
    void format(ComplexMessage message, JavadocElement element);

    static Formatter of(Predicate<JavadocElement> predicate, Consumer<ComplexMessage> action) {
      return of(predicate, (message, element) -> action.accept(message));
    }

    static Formatter of(Predicate<JavadocElement> predicate,
        BiConsumer<ComplexMessage, JavadocElement> action) {
      return new Formatter() {
        @Override
        public boolean applies(JavadocElement element) {
          return predicate.test(element);
        }

        @Override
        public void format(ComplexMessage message, JavadocElement element) {
          action.accept(message, element);
        }
      };
    }
  }

}
