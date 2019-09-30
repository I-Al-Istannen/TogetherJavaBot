package org.togetherjava.command.commands.newtag;

import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.literal;

import de.ialistannen.commandprocrastination.autodiscovery.ActiveCommand;
import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import de.ialistannen.commandprocrastination.parsing.SuccessParser;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.togetherjava.commandrewrite.CommandContext;
import org.togetherjava.messaging.SimpleMessage;
import org.togetherjava.model.MessageTag;

@ActiveCommand(name = "tag-list", parentClass = TagCommand.class)
public class TagListCommand extends CommandNode<CommandContext> {

  public TagListCommand() {
    super(SuccessParser.wrapping(literal("list")));
    setCommand(this::execute);
  }

  private void execute(CommandContext context) {
    Collection<MessageTag> tags = context.getDatabase().getTagDao().getAllTags();

    Function<MessageTag, String> tagWithAliasesToString = tag -> {
      String result = "**" + tag.keyword() + "**";

      if (!tag.aliases().isEmpty()) {
        result += " (" + String.join(", ", tag.aliases()) + ")";
      }
      return result;
    };

    String tagString = tags.stream()
        .map(tagWithAliasesToString)
        .collect(Collectors.joining(", "));

    context.getMessageSender().sendMessage(
        SimpleMessage.information("I know:\n" + tagString),
        context.getRequestContext().getChannel()
    );

  }
}
