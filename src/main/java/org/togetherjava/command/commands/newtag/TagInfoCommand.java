package org.togetherjava.command.commands.newtag;

import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.greedyPhrase;
import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.literal;

import de.ialistannen.commandprocrastination.autodiscovery.ActiveCommand;
import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import de.ialistannen.commandprocrastination.parsing.ParseException;
import de.ialistannen.commandprocrastination.parsing.SuccessParser;
import java.util.Optional;
import org.togetherjava.commandrewrite.CommandContext;
import org.togetherjava.messaging.BotMessage.MessageCategory;
import org.togetherjava.messaging.ComplexMessage;
import org.togetherjava.messaging.SimpleMessage;
import org.togetherjava.model.MessageTag;

@ActiveCommand(name = "tag-info", parentClass = TagCommand.class)
public class TagInfoCommand extends CommandNode<CommandContext> {

  public TagInfoCommand() {
    super(SuccessParser.wrapping(literal("info")));
    setCommand(this::execute);
  }

  private void execute(CommandContext context) throws ParseException {
    String keyword = context.shift(greedyPhrase());

    Optional<MessageTag> tag = context.getDatabase().getTagDao().getTagForKeyword(keyword);

    if (tag.isEmpty()) {
      context.getMessageSender().sendMessage(
          SimpleMessage.error("Couldn't find that tag (" + keyword + ")"),
          context.getRequestContext().getChannel()
      );
      return;
    }

    MessageTag messageTag = tag.get();

    String ownerName = "<@!" + messageTag.creator() + ">";

    ComplexMessage message = new ComplexMessage(MessageCategory.INFORMATION)
        .editEmbed(it -> it.setDescription(messageTag.value()))
        .editEmbed(it -> it.addField("Creator", ownerName, true))
        .editEmbed(it -> it.addField("Description", messageTag.description(), true))
        .editEmbed(it -> it.setTitle(messageTag.keyword()));

    context.getMessageSender().sendMessage(message, context.getRequestContext().getChannel());
  }
}
