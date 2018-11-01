package org.togetherjava.command.commands;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static org.togetherjava.command.CommandGenericHelper.argument;
import static org.togetherjava.command.CommandGenericHelper.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Optional;
import java.util.stream.Collectors;
import org.togetherjava.command.CommandSource;
import org.togetherjava.command.TJCommand;
import org.togetherjava.messaging.BotMessage.MessageCategory;
import org.togetherjava.messaging.ComplexMessage;
import org.togetherjava.messaging.SimpleMessage;
import org.togetherjava.model.ImmutableMessageTag;
import org.togetherjava.model.MessageTag;
import org.togetherjava.storage.dao.TagDao;

public class TagCommand implements TJCommand {

  @Override
  public LiteralCommandNode<CommandSource> getCommand(CommandDispatcher<CommandSource> dispatcher) {
    return literal("tag")
        .then(
            argument("keyword", word())
                .executes(context ->
                    displayTag(context.getSource(), context.getArgument("keyword", String.class))
                )
        )
        .then(
            literal("add")
                .then(
                    argument("keyword", word())
                        .then(
                            argument("quoted description", string())
                                .then(
                                    argument("value", greedyString())
                                        .executes(context ->
                                            addTag(
                                                context.getSource(),
                                                context.getArgument("keyword", String.class),
                                                context.getArgument("quoted description",
                                                    String.class),
                                                context.getArgument("value", String.class)
                                            )
                                        )
                                )
                        )
                )
        ).then(
            literal("list")
                .executes(context -> listTags(context.getSource()))
        ).then(
            literal("delete")
                .then(
                    argument("tag keyword", word())
                        .executes(context ->
                            deleteTag(
                                context.getSource(),
                                context.getArgument("tag keyword", String.class)
                            )
                        )
                )
        ).then(
            literal("edit")
                .then(
                    argument("tag keyword", word())
                        .then(
                            argument("new value", greedyString())
                                .executes(context ->
                                    editTag(
                                        context.getSource(),
                                        context.getArgument("tag keyword", String.class),
                                        context.getArgument("new value", String.class)
                                    )
                                )
                        )
                )
        )
        .build();
  }

  private int addTag(CommandSource source, String keyword, String description, String value) {
    TagDao tagDao = source.getContext().getDatabase().getTagDao();

    if (tagDao.getByKeyword(keyword).isPresent()) {
      return sendTagAlreadyExists(source, keyword);
    }

    tagDao.addTag(
        ImmutableMessageTag.builder()
            .keyword(keyword)
            .description(description)
            .value(value)
            .creator(source.getUser().getIdLong())
            .build()
    );

    displayTag(source, keyword);
    return 0;
  }

  private int displayTag(CommandSource source, String keyword) {
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

  private int sendTagAlreadyExists(CommandSource source, String keyword) {
    source.getMessageSender().sendMessage(
        SimpleMessage.error("Tag '" + keyword + "' already exists"),
        source.getChannel()
    );
    return -1;
  }

  private int listTags(CommandSource source) {
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

  private int deleteTag(CommandSource source, String keyword) {
    TagDao tagDao = source.getContext().getDatabase().getTagDao();

    if (tagDao.deleteTag(keyword) <= 0) {
      return sendTagNotFound(source, keyword);
    }

    source.getMessageSender().sendMessage(
        SimpleMessage.success("Tag '" + keyword + "' deleted."),
        source.getChannel()
    );
    return 0;
  }

  private int sendTagNotFound(CommandSource source, String keyword) {
    source.getMessageSender().sendMessage(
        SimpleMessage.error("Tag '" + keyword + "' not found."),
        source.getChannel()
    );
    return -1;
  }

  private int editTag(CommandSource source, String keyword, String value) {
    TagDao tagDao = source.getContext().getDatabase().getTagDao();

    Optional<MessageTag> tagOptional = tagDao.getByKeyword(keyword);
    if (!tagOptional.isPresent()) {
      return sendTagNotFound(source, keyword);
    }

    tagDao.editTag(
        ImmutableMessageTag.copyOf(tagOptional.get())
            .withValue(value)
            .withCreator(source.getUser().getIdLong())
    );

    displayTag(source, keyword);
    return 0;
  }
}
