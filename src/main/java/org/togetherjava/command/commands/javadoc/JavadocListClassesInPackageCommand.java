package org.togetherjava.command.commands.javadoc;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static org.togetherjava.command.CommandGenericHelper.argument;
import static org.togetherjava.command.CommandGenericHelper.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.ialistannen.htmljavadocparser.JavadocApi;
import de.ialistannen.htmljavadocparser.model.JavadocPackage;
import de.ialistannen.htmljavadocparser.model.types.Type;
import java.util.List;
import org.togetherjava.autodiscovery.IgnoreAutoDiscovery;
import org.togetherjava.command.CommandSource;
import org.togetherjava.command.TJCommand;
import org.togetherjava.command.exceptions.CommandException;
import org.togetherjava.messaging.BotMessage.MessageCategory;
import org.togetherjava.messaging.PaginatedMessage;
import org.togetherjava.reactions.ReactionListener;

@IgnoreAutoDiscovery
class JavadocListClassesInPackageCommand implements TJCommand {

  private JavadocApi javadocApi;

  JavadocListClassesInPackageCommand(JavadocApi javadocApi) {
    this.javadocApi = javadocApi;
  }

  @Override
  public LiteralCommandNode<CommandSource> getCommand(CommandDispatcher<CommandSource> dispatcher) {
    return literal("listInPackage")
        .shortDescription("Lists classes in a package")
        .then(
            argument("package name", greedyString())
                .shortDescription("Lists all classes in the given package")
                .executes(context -> {
                  CommandSource source = context.getSource();
                  String packageName = context.getArgument("package name", String.class);

                  JavadocPackage javadocPackage = javadocApi.getPackage(packageName)
                      .orElseThrow(() -> new CommandException("Package not found :("));

                  List<Type> types = javadocPackage.getContainedTypes();

                  ReactionListener reactionListener = source.getContext().getReactionListener();

                  PaginatedMessage<Type> message = new PaginatedMessage<Type>(
                      MessageCategory.SUCCESS, reactionListener)
                      .withElements(types)
                      .withPageSize(10)
                      .withToStringConverter(type -> "`" + type.getSimpleName() + "`");

                  source.getMessageSender().sendMessage(
                      message, source.getChannel()
                  );

                  return 0;
                })
        )
        .build();
  }
}
