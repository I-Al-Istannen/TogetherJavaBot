package org.togetherjava.command.commands.newtag;

import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.greedyPhrase;
import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.literal;

import de.ialistannen.commandprocrastination.autodiscovery.ActiveCommand;
import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import de.ialistannen.commandprocrastination.parsing.ParseException;
import de.ialistannen.commandprocrastination.parsing.SuccessParser;
import org.togetherjava.command.CommandContext;
import org.togetherjava.command.commands.PrefixedBaseCommand;
import org.togetherjava.messaging.SimpleMessage;
import org.togetherjava.messaging.sending.MessageSender;
import org.togetherjava.storage.dao.jooq.dao.TagDao;

@ActiveCommand(name = "tag-base", parentClass = PrefixedBaseCommand.class)
public class TagCommand extends CommandNode<CommandContext> {

  public TagCommand() {
    super(SuccessParser.wrapping(literal("tag")));
    setCommand(this::showTag);
  }

  private void showTag(CommandContext context) throws ParseException {
    String keyword = context.shift(greedyPhrase());
    MessageSender sender = context.getMessageSender();

    TagDao tagDao = context.getDatabase().getTagDao();

    tagDao.getTagForKeyword(keyword).ifPresent(tag -> sender.sendMessage(
        SimpleMessage.information(tag.value()),
        context.getRequestContext().getChannel()
    ));
  }
}
