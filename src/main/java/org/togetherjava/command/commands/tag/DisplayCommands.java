package org.togetherjava.command.commands.tag;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static org.togetherjava.command.CommandGenericHelper.argument;
import static org.togetherjava.command.CommandGenericHelper.literal;
import static org.togetherjava.command.commands.tag.TagCommand.sendTagAlreadyExists;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Optional;
import java.util.stream.Collectors;
import org.togetherjava.command.CommandSource;
import org.togetherjava.messaging.BotMessage.MessageCategory;
import org.togetherjava.messaging.ComplexMessage;
import org.togetherjava.messaging.SimpleMessage;
import org.togetherjava.model.MessageTag;
import org.togetherjava.storage.dao.TagDao;

class DisplayCommands {

  /**
   * Adds the commands that display the tags
   *
   * @param parent the parent command to attach to
   */
  static void addCommands(LiteralArgumentBuilder<CommandSource> parent) {
    parent.then(
        argument("keyword", word()).executes(
            context ->
                displayTag(context.getSource(), getString(context, "keyword"))
        )
    ).then(
        literal("info").then(
            argument("keyword", word()).executes(
                context -> displayTagInfo(context.getSource(), getString(context, "keyword"))
            )
        )
    ).then(
        literal("list").executes(
            context -> listTags(context.getSource())
        )
    );
  }

  private static int displayTag(CommandSource source, String keyword) {
    TagDao tagDao = source.getContext().getDatabase().getTagDao();

    Optional<MessageTag> tagOptional = tagDao.getByKeyword(keyword);
    if (!tagOptional.isPresent()) {
      return sendTagAlreadyExists(source, keyword);
    }

    MessageTag tag = tagOptional.get();

    source.getMessageSender().sendMessage(
        new SimpleMessage(MessageCategory.NONE, tag.value()),
        source.getChannel()
    );

    return 0;
  }


  static int displayTagInfo(CommandSource source, String keyword) {
    TagDao tagDao = source.getContext().getDatabase().getTagDao();

    Optional<MessageTag> tagOptional = tagDao.getByKeyword(keyword);
    if (!tagOptional.isPresent()) {
      return sendTagAlreadyExists(source, keyword);
    }

    MessageTag tag = tagOptional.get();

    source.getMessageSender().sendMessage(
        new ComplexMessage(MessageCategory.INFORMATION)
            .editEmbed(it -> it.addField("Keyword", tag.keyword(), true))
            .editEmbed(it -> it.addField("Description", tag.description(), true))
            .editEmbed(it -> it.addField("Value", tag.value(), false))
            .notSelfDestructing(),
        source.getChannel()
    );

    return 0;
  }

  private static int listTags(CommandSource source) {
    String tags = source.getContext().getDatabase().getTagDao().getAll()
        .stream()
        .map(MessageTag::keyword)
        .sorted()
        .collect(Collectors.joining(", "));

    if (tags.isEmpty()) {
      tags = "There are no tags configured yet :/";
    }

    source.getMessageSender().sendMessage(
        SimpleMessage.success(tags),
        source.getChannel()
    );

    return 0;
  }

}
