package org.togetherjava.command.commands;

import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.greedyPhrase;
import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.literal;

import de.ialistannen.commandprocrastination.autodiscovery.ActiveCommand;
import de.ialistannen.commandprocrastination.command.tree.CommandFinder.FindResult;
import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import de.ialistannen.commandprocrastination.command.tree.data.DefaultDataKey;
import de.ialistannen.commandprocrastination.parsing.ParseException;
import de.ialistannen.commandprocrastination.parsing.SuccessParser;
import de.ialistannen.commandprocrastination.util.StringReader;
import java.util.Optional;
import org.togetherjava.command.CommandContext;
import org.togetherjava.messaging.BotMessage.MessageCategory;
import org.togetherjava.messaging.ComplexMessage;

@ActiveCommand(name = "help", parentClass = PrefixedBaseCommand.class)
public class HelpCommand extends CommandNode<CommandContext> {

  public HelpCommand() {
    super(SuccessParser.wrapping(literal("help")));
    setCommand(this::execute);
  }

  private void execute(CommandContext context) throws ParseException {
    String path = context.shift(greedyPhrase());

    FindResult<CommandContext> foundCommands = context.getCommandFinder()
        // Remove the need to specify the prefix
        .find(getParent().orElseThrow(), new StringReader(path));

    CommandNode<CommandContext> finalNode = foundCommands.getChain().getFinalNode();

    ComplexMessage message = new ComplexMessage(MessageCategory.INFORMATION);

    finalNode.getOptionalData(DefaultDataKey.IDENTIFIER).ifPresent(name ->
        message.editEmbed(it -> it.setTitle(name.toString()))
    );

    finalNode.getHeadParser().getName().ifPresent(name ->
        message.editEmbed(it -> it.addField("Keyword", '`' + name + '`', true))
    );

    finalNode.getOptionalData(DefaultDataKey.USAGE)
        .map(usage -> "`" + usage + "`")
        .or(() -> fetchFromMessages("usage", context, finalNode))
        .or(() -> Optional.of("*(approx)* `" + foundCommands.getChain().buildUsage() + "`"))
        .ifPresent(usage ->
            message.editEmbed(it -> it.addField("Usage", usage, true))
        );

    finalNode.getOptionalData(DefaultDataKey.SHORT_DESCRIPTION)
        .or(() -> fetchFromMessages("short-description", context, finalNode))
        .ifPresent(desc ->
            message.editEmbed(it -> it.addField("Short description", desc.toString(), true))
        );
    finalNode.getOptionalData(DefaultDataKey.LONG_DESCRIPTION)
        .or(() -> fetchFromMessages("long-description", context, finalNode))
        .ifPresent(desc ->
            message.editEmbed(it -> it.setDescription(desc.toString()))
        );

    finalNode.getOptionalData(DefaultDataKey.PERMISSION).ifPresent(perm ->
        message.editEmbed(it -> it.addField("Permission", "`" + perm + "`", true))
    );

    context.getMessageSender().sendMessage(
        message, context.getRequestContext().getChannel()
    );
  }

  private Optional<String> fetchFromMessages(String restPath, CommandContext commandContext,
      CommandNode<CommandContext> node) {
    if (!node.hasOptionalData(DefaultDataKey.IDENTIFIER)) {
      return Optional.empty();
    }
    String identifier = node.<String>getOptionalData(DefaultDataKey.IDENTIFIER).orElseThrow();
    String lookupPath = "commands." + identifier + "." + restPath;

    return commandContext.getMessages().trOptional(lookupPath);
  }
}
