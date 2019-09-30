package org.togetherjava.command.commands.newtag;

import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.greedyPhrase;
import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.literal;
import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.phrase;

import de.ialistannen.commandprocrastination.autodiscovery.ActiveCommand;
import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import de.ialistannen.commandprocrastination.command.tree.data.DefaultDataKey;
import de.ialistannen.commandprocrastination.parsing.ParseException;
import de.ialistannen.commandprocrastination.parsing.SuccessParser;
import org.togetherjava.commandrewrite.CommandContext;
import org.togetherjava.messaging.SimpleMessage;
import org.togetherjava.model.ImmutableMessageTag;
import org.togetherjava.model.MessageTag;

@ActiveCommand(name = "tag-create", parentClass = TagCommand.class)
public class TagCreateCommand extends CommandNode<CommandContext> {

  public TagCreateCommand() {
    super(SuccessParser.wrapping(literal("create")));
    setCommand(this::execute);
    setData(DefaultDataKey.PERMISSION, "tag.create");
  }

  private void execute(CommandContext context) throws ParseException {
    String name = context.shift(phrase());
    String description = context.shift(phrase());
    String value = context.shift(greedyPhrase());

    MessageTag tag = ImmutableMessageTag.builder()
        .keyword(name)
        .description(description)
        .value(value)
        .creator(context.getRequestContext().getUser().getIdLong())
        .build();

    context.getDatabase().getTagDao().addOrUpdate(tag);

    context.getMessageSender().sendMessage(
        SimpleMessage.success("Added the tag " + name),
        context.getRequestContext().getChannel()
    );
  }
}
