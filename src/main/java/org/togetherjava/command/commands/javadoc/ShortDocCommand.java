package org.togetherjava.command.commands.javadoc;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static org.togetherjava.command.CommandGenericHelper.argument;
import static org.togetherjava.command.CommandGenericHelper.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.ialistannen.htmljavadocparser.model.properties.JavadocElement;
import java.util.List;
import org.togetherjava.autodiscovery.IgnoreAutoDiscovery;
import org.togetherjava.command.CommandSource;
import org.togetherjava.command.TJCommand;
import org.togetherjava.command.commands.javadoc.formatting.JavadocDescriptionFormatter;
import org.togetherjava.command.commands.javadoc.formatting.JavadocMessageFormatter;
import org.togetherjava.command.commands.javadoc.formatting.JavadocMessageSender;
import org.togetherjava.command.exceptions.CommandException;
import org.togetherjava.docs.DocsApi;

@IgnoreAutoDiscovery
public class ShortDocCommand implements TJCommand {

  private final DocsApi docsApi;
  private final JavadocMessageSender javadocMessageSender;

  public ShortDocCommand(DocsApi docsApi) {
    this.docsApi = docsApi;

    JavadocMessageFormatter messageFormatter = new JavadocMessageFormatter(
        new ShortDescriptionFormatter()
    );
    this.javadocMessageSender = new JavadocMessageSender(messageFormatter);
  }

  @Override
  public LiteralCommandNode<CommandSource> getCommand(CommandDispatcher<CommandSource> dispatcher) {
    return literal("short")
        .shortDescription("Shows abbreviated javadoc")
        .longDescription("Allows you to view the first paragraph of a javadoc element")
        .then(argument("pathToType", greedyString())
            .shortDescription("Shows javadoc for a given class, package or member.")
            .longDescription(
                "The format is `[package.]Type[#member]`."
                    + " To tell it you want to resolve a method and not a field,"
                    + " please always include at least an opening brace after the member name,"
                    + " e.g. `String#String(`"
            )
            .executes(context -> {
              String selectorString = context.getArgument("pathToType", String.class);
              CommandSource commandSource = context.getSource();

              JavadocSelector selector = JavadocSelector.fromString(selectorString);

              try {
                List<? extends JavadocElement> foundElements = docsApi.find(selector);

                javadocMessageSender.sendResult(foundElements, commandSource);

                return 0;
              } catch (IllegalArgumentException e) {
                throw new CommandException(e.getMessage());
              }
            })
        )
        .build();
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
