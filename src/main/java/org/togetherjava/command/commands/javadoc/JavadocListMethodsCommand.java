package org.togetherjava.command.commands.javadoc;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static org.togetherjava.command.CommandGenericHelper.argument;
import static org.togetherjava.command.CommandGenericHelper.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.ialistannen.htmljavadocparser.model.properties.Invocable;
import de.ialistannen.htmljavadocparser.model.properties.JavadocElement;
import de.ialistannen.htmljavadocparser.model.types.JavadocClass;
import de.ialistannen.htmljavadocparser.model.types.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.togetherjava.autodiscovery.IgnoreAutoDiscovery;
import org.togetherjava.command.CommandSource;
import org.togetherjava.command.TJCommand;
import org.togetherjava.command.commands.javadoc.formatting.JavadocDescriptionFormatter;
import org.togetherjava.command.commands.javadoc.formatting.JavadocMessageFormatter;
import org.togetherjava.command.commands.javadoc.formatting.JavadocMessageSender;
import org.togetherjava.command.exceptions.CommandException;
import org.togetherjava.docs.DocsApi;
import org.togetherjava.messaging.BotMessage.MessageCategory;
import org.togetherjava.messaging.PaginatedMessage;
import org.togetherjava.messaging.SimpleMessage;
import org.togetherjava.reactions.ReactionListener;

/**
 * A command that allows users to list methods of a type.
 */
@IgnoreAutoDiscovery
class JavadocListMethodsCommand implements TJCommand {

  private final DocsApi javadocApi;
  private final JavadocMessageSender javadocMessageSender;


  JavadocListMethodsCommand(DocsApi javadocApi) {
    this.javadocApi = javadocApi;
    javadocMessageSender = new JavadocMessageSender(new JavadocMessageFormatter(
        new JavadocDescriptionFormatter()));
  }

  @Override
  public LiteralCommandNode<CommandSource> getCommand(CommandDispatcher<CommandSource> dispatcher) {
    return literal("listMethods")
        .shortDescription("Lists all methods and constructors in a type.")
        .then(
            argument("type", greedyString())
                .shortDescription("Lists all methods and constructors for the given type.")
                .executes(context -> {
                  CommandSource commandSource = context.getSource();
                  String typeName = context.getArgument("type", String.class);

                  JavadocSelector selector = JavadocSelector.fromString(typeName);

                  List<? extends JavadocElement> types = javadocApi.find(selector);

                  if (types.size() > 1) {
                    javadocMessageSender.sendMultipleFoundError(commandSource, types);
                    return 0;
                  }

                  if (types.isEmpty()) {
                    throw new CommandException("Nothing found :(");
                  }

                  JavadocElement element = types.get(0);

                  if (!(element instanceof Type)) {
                    commandSource.getMessageSender().sendMessage(
                        SimpleMessage.error("You can only list methods of a type!"),
                        commandSource.getChannel()
                    );
                    return 0;
                  }

                  List<Invocable> methods = getMethods((Type) element);

                  ReactionListener reactionListener = commandSource.getContext()
                      .getReactionListener();

                  PaginatedMessage<Invocable> paginatedMessage = new PaginatedMessage<Invocable>(
                      MessageCategory.SUCCESS, commandSource.getUser().getIdLong(), reactionListener
                  )
                      .withElements(methods)
                      .withPageSize(10)
                      .withToStringConverter(invocable -> "`" + invocable.getDeclaration() + "`");

                  commandSource.getMessageSender().sendMessage(
                      paginatedMessage, commandSource.getChannel()
                  );

                  return 0;
                })
        )
        .build();
  }

  private List<Invocable> getMethods(Type type) {
    List<Invocable> list = new ArrayList<>(type.getMethods());

    if (type instanceof JavadocClass) {
      list.addAll(0, ((JavadocClass) type).getConstructors());
    }

    list.sort(
        Comparator.comparing(Invocable::getSimpleName)
            .thenComparing(it -> it.getParameters().size())
    );

    return list;
  }
}

