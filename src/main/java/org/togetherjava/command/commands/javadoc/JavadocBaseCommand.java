package org.togetherjava.command.commands.javadoc;

import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.greedyPhrase;
import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.literal;

import de.ialistannen.commandprocrastination.autodiscovery.ActiveCommand;
import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import de.ialistannen.commandprocrastination.parsing.ParseException;
import de.ialistannen.commandprocrastination.parsing.SuccessParser;
import de.ialistannen.htmljavadocparser.model.properties.JavadocElement;
import java.util.List;
import org.togetherjava.command.CommandContext;
import org.togetherjava.command.commands.PrefixedBaseCommand;
import org.togetherjava.command.commands.javadoc.formatting.JavadocDescriptionFormatter;
import org.togetherjava.command.commands.javadoc.formatting.JavadocMessageFormatter;
import org.togetherjava.command.commands.javadoc.formatting.JavadocMessageSender;
import org.togetherjava.docs.DocsApi;

@ActiveCommand(name = "javadoc-base", parentClass = PrefixedBaseCommand.class)
public class JavadocBaseCommand extends CommandNode<CommandContext> {

  private DocsApi api;

  public JavadocBaseCommand(CommandContext context) {
    super(SuccessParser.wrapping(literal("doc")));
    this.api = new DocsApi(context.getConfig());

    JavadocMessageFormatter formatter = new JavadocMessageFormatter(
        new JavadocDescriptionFormatter()
    );
    JavadocMessageSender sender = new JavadocMessageSender(formatter);
    setCommand(input -> showDoc(input, sender));
  }

  /**
   * Returns the docs api.
   *
   * @return the docs api
   */
  public DocsApi getApi() {
    return api;
  }

  private void showDoc(CommandContext context, JavadocMessageSender sender) throws ParseException {
    String selectorString = context.shift(greedyPhrase());

    JavadocSelector selector = JavadocSelector.fromString(selectorString);
    List<? extends JavadocElement> elements = api.find(selector);

    sender.sendResult(
        elements,
        context.getMessageSender(),
        context.getRequestContext().getMessage().getChannel()
    );
  }

  @ActiveCommand(name = "javadoc-short", parentClass = JavadocBaseCommand.class)
  public static class ShortCommand extends CommandNode<CommandContext> {

    private JavadocMessageSender javadocSender;

    public ShortCommand() {
      super(SuccessParser.wrapping(literal("short")));

      JavadocMessageFormatter messageFormatter = new JavadocMessageFormatter(
          new ShortDescriptionFormatter()
      );
      this.javadocSender = new JavadocMessageSender(messageFormatter);

      setCommand(context ->
          ((JavadocBaseCommand) getParent().orElseThrow())
              .showDoc(context, javadocSender)
      );
    }
  }

  private static class ShortDescriptionFormatter extends JavadocDescriptionFormatter {

    @Override
    protected String adjustDescription(String description) {
      return shortenToFirstParagraph(description);
    }

    private String shortenToFirstParagraph(String description) {
      int end = !description.contains("\n")
          ? description.length()
          : description.indexOf("\n");
      description = description.substring(0, end);
      return description;
    }
  }

}
