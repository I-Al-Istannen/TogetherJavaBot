package org.togetherjava.command.commands.javadoc;

import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.literal;
import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.word;

import de.ialistannen.commandprocrastination.autodiscovery.ActiveCommand;
import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import de.ialistannen.commandprocrastination.parsing.ParseException;
import de.ialistannen.commandprocrastination.parsing.SuccessParser;
import de.ialistannen.htmljavadocparser.model.JavadocPackage;
import de.ialistannen.htmljavadocparser.model.types.Type;
import java.util.List;
import org.togetherjava.command.exceptions.CommandException;
import org.togetherjava.commandrewrite.CommandContext;
import org.togetherjava.messaging.BotMessage.MessageCategory;
import org.togetherjava.messaging.PaginatedMessage;
import org.togetherjava.reactions.ReactionListener;

@ActiveCommand(name = "doc-list-packages", parentClass = JavadocBaseCommand.class)
public class ListClassesInPackage extends CommandNode<CommandContext> {

  public ListClassesInPackage() {
    super(SuccessParser.wrapping(literal("list in package")));
    setCommand(this::executeCommand);
  }

  private void executeCommand(CommandContext context) throws ParseException {
    JavadocBaseCommand parent = (JavadocBaseCommand) getParent().orElseThrow();

    String packageName = context.shift(word());

    JavadocPackage javadocPackage = parent.getApi().getUnderlyingJavadocApi()
        .getPackage(packageName)
        .orElseThrow(() -> new CommandException("Package not found :("));

    List<Type> types = javadocPackage.getContainedTypes();

    System.out.println(packageName);

    ReactionListener reactionListener = context.getReactionListener();

    PaginatedMessage<Type> message = new PaginatedMessage<Type>(
        MessageCategory.SUCCESS, context.getRequestContext().getUser().getIdLong(), reactionListener
    )
        .withElements(types)
        .withPageSize(10)
        .withToStringConverter(type -> "`" + type.getSimpleName() + "`");

    context.getMessageSender().sendMessage(
        message, context.getRequestContext().getMessage().getChannel()
    );
  }
}
