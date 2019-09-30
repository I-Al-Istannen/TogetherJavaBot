package org.togetherjava.command.commands.javadoc;

import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.greedyPhrase;
import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.literal;

import de.ialistannen.commandprocrastination.autodiscovery.ActiveCommand;
import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import de.ialistannen.commandprocrastination.parsing.ParseException;
import de.ialistannen.commandprocrastination.parsing.SuccessParser;
import de.ialistannen.htmljavadocparser.model.properties.Invocable;
import de.ialistannen.htmljavadocparser.model.properties.JavadocElement;
import de.ialistannen.htmljavadocparser.model.types.JavadocClass;
import de.ialistannen.htmljavadocparser.model.types.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.togetherjava.command.CommandContext;
import org.togetherjava.command.commands.javadoc.formatting.JavadocMessageFormatter;
import org.togetherjava.command.commands.javadoc.formatting.JavadocMessageSender;
import org.togetherjava.command.exceptions.CommandException;
import org.togetherjava.messaging.BotMessage.MessageCategory;
import org.togetherjava.messaging.PaginatedMessage;
import org.togetherjava.messaging.SimpleMessage;
import org.togetherjava.reactions.ReactionListener;

@ActiveCommand(name = "list-methods", parentClass = JavadocBaseCommand.class)
public class ListMethods extends CommandNode<CommandContext> {

  private JavadocMessageSender javadocSender;

  public ListMethods() {
    super(SuccessParser.wrapping(literal("listMethods")));
    setCommand(this::execute);
    javadocSender = new JavadocMessageSender(new JavadocMessageFormatter());
  }

  private void execute(CommandContext context) throws ParseException {
    JavadocBaseCommand parent = (JavadocBaseCommand) getParent().orElseThrow();
    String typeName = context.shift(greedyPhrase());

    JavadocSelector selector = JavadocSelector.fromString(typeName);

    List<? extends JavadocElement> types = parent.getApi().find(selector);

    if (types.size() > 1) {
      javadocSender.sendMultipleFoundError(
          context.getRequestContext().getMessage().getChannel(),
          context.getMessageSender(),
          types
      );
      return;
    }

    if (types.isEmpty()) {
      throw new CommandException("Nothing found :(");
    }

    JavadocElement element = types.get(0);

    if (!(element instanceof Type)) {
      context.getMessageSender().sendMessage(
          SimpleMessage.error("You can only list methods of a type!"),
          context.getRequestContext().getMessage().getChannel()
      );
      return;
    }

    List<Invocable> methods = getMethods((Type) element);

    ReactionListener reactionListener = context.getReactionListener();

    PaginatedMessage<Invocable> paginatedMessage = new PaginatedMessage<Invocable>(
        MessageCategory.SUCCESS, context.getRequestContext().getUser().getIdLong(), reactionListener
    )
        .withElements(methods)
        .withPageSize(10)
        .withToStringConverter(invocable -> "`" + invocable.getDeclaration() + "`");

    context.getMessageSender().sendMessage(
        paginatedMessage, context.getRequestContext().getMessage().getChannel()
    );
    return;
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
