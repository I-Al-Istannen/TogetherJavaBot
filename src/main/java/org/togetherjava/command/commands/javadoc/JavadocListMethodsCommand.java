package org.togetherjava.command.commands.javadoc;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static org.togetherjava.command.CommandGenericHelper.argument;
import static org.togetherjava.command.CommandGenericHelper.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.ialistannen.htmljavadocparser.JavadocApi;
import de.ialistannen.htmljavadocparser.model.properties.Invocable;
import de.ialistannen.htmljavadocparser.model.properties.JavadocElement;
import de.ialistannen.htmljavadocparser.model.types.JavadocClass;
import de.ialistannen.htmljavadocparser.model.types.Type;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.togetherjava.autodiscovery.IgnoreAutoDiscovery;
import org.togetherjava.command.CommandSource;
import org.togetherjava.command.TJCommand;
import org.togetherjava.command.exceptions.CommandException;
import org.togetherjava.messaging.BotMessage.MessageCategory;
import org.togetherjava.messaging.PaginatedMessage;
import org.togetherjava.messaging.SimpleMessage;
import org.togetherjava.reactions.ReactionListener;

/**
 * A command that allows users to list methods of a type.
 */
@IgnoreAutoDiscovery
class JavadocListMethodsCommand implements TJCommand {

  private JavadocApi javadocApi;

  JavadocListMethodsCommand(JavadocApi javadocApi) {
    this.javadocApi = javadocApi;
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

                  List<? extends JavadocElement> types = selector.select(javadocApi);

                  if (types.size() > 1) {
                    return JavadocCommand.sendMultipleFoundError(commandSource, types);
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
                      MessageCategory.SUCCESS, reactionListener
                  )
                      .withElements(methods)
                      .withPageSize(10)
                      .withToStringConverter(invocable -> "`" + invocable.getDeclaration() + "`")
                      .selfDestructing(Duration.ofSeconds(20));

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

