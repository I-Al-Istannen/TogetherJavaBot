package org.togetherjava.command.commands.newtag;

import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.greedyPhrase;
import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.literal;

import de.ialistannen.commandprocrastination.autodiscovery.ActiveCommand;
import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import de.ialistannen.commandprocrastination.command.tree.data.DefaultDataKey;
import de.ialistannen.commandprocrastination.parsing.ParseException;
import de.ialistannen.commandprocrastination.parsing.SuccessParser;
import java.util.Optional;
import org.togetherjava.commandrewrite.CommandContext;
import org.togetherjava.messaging.SimpleMessage;
import org.togetherjava.model.MessageTag;
import org.togetherjava.storage.dao.jooq.dao.TagDao;

@ActiveCommand(name = "tag-delete", parentClass = TagCommand.class)
public class TagDeleteCommand extends CommandNode<CommandContext> {

  public TagDeleteCommand() {
    super(SuccessParser.wrapping(literal("delete")));
    setCommand(this::execute);
    setData(DefaultDataKey.PERMISSION, "tag.delete");
  }

  private void execute(CommandContext context) throws ParseException {
    String keyword = context.shift(greedyPhrase());

    TagDao tagDao = context.getDatabase().getTagDao();

    Optional<MessageTag> tag = tagDao.getTagForKeyword(keyword);

    if (tag.isEmpty()) {
      context.getMessageSender().sendMessage(
          SimpleMessage.error("Could not find the tag '" + keyword + "'"),
          context.getRequestContext().getChannel()
      );
      return;
    }

    tagDao.deleteTag(tag.get());

    context.getMessageSender().sendMessage(
        SimpleMessage.success("Deleted the tag '" + tag.get().keyword() + "'!"),
        context.getRequestContext().getChannel()
    );

  }
}
