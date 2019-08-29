package org.togetherjava.command.commands.javadoc;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static org.togetherjava.command.CommandGenericHelper.argument;
import static org.togetherjava.command.CommandGenericHelper.literal;

import com.moandjiezana.toml.Toml;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.ialistannen.htmljavadocparser.model.properties.JavadocElement;
import java.util.List;
import org.togetherjava.command.CommandSource;
import org.togetherjava.command.TJCommand;
import org.togetherjava.command.commands.javadoc.formatting.JavadocDescriptionFormatter;
import org.togetherjava.command.commands.javadoc.formatting.JavadocMessageFormatter;
import org.togetherjava.command.commands.javadoc.formatting.JavadocMessageSender;
import org.togetherjava.command.exceptions.CommandException;
import org.togetherjava.docs.DocsApi;

/**
 * A command serving javadoc.
 */
public class JavadocCommand implements TJCommand {

  private final DocsApi javadocApi;
  private final JavadocMessageSender javadocMessageSender;

  public JavadocCommand(Toml config) {
    this.javadocApi = new DocsApi(config);
    this.javadocMessageSender = new JavadocMessageSender(new JavadocMessageFormatter(
        new JavadocDescriptionFormatter()));
  }

  @Override
  public LiteralCommandNode<CommandSource> getCommand(CommandDispatcher<CommandSource> dispatcher) {
    return literal("doc")
        .shortDescription("Shows javadoc")
        .longDescription("Allows you to view the javadoc of the standard library right in discord.")
        .then(
            argument("pathToType", greedyString())
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
                    List<? extends JavadocElement> foundElements = javadocApi.find(selector);

                    javadocMessageSender.sendResult(foundElements, commandSource);

                    return 0;
                  } catch (IllegalArgumentException e) {
                    throw new CommandException(e.getMessage());
                  }
                })
        )
        .then(
            new JavadocListMethodsCommand(javadocApi).getCommand(dispatcher)
        )
        .then(
            new JavadocListClassesInPackageCommand(javadocApi).getCommand(dispatcher)
        )
        .then(
            new ShortDocCommand(javadocApi).getCommand(dispatcher)
        )
        .build();
  }
}
