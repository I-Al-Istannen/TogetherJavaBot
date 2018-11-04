package org.togetherjava.command.commands.tag;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static org.togetherjava.command.CommandGenericHelper.argument;
import static org.togetherjava.command.CommandGenericHelper.literal;
import static org.togetherjava.command.commands.tag.DisplayCommands.displayTagInfo;
import static org.togetherjava.command.commands.tag.TagCommand.sendTagAlreadyExists;
import static org.togetherjava.command.commands.tag.TagCommand.sendTagNotFound;

import com.moandjiezana.toml.Toml;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.dv8tion.jda.core.entities.ISnowflake;
import net.dv8tion.jda.core.entities.Member;
import org.togetherjava.command.CommandSource;
import org.togetherjava.command.exceptions.CommonExceptions;
import org.togetherjava.messaging.SimpleMessage;
import org.togetherjava.model.ImmutableMessageTag;
import org.togetherjava.model.MessageTag;
import org.togetherjava.storage.dao.TagDao;

class ModificationCommands {

  /**
   * Adds the commands that modify the tags
   *
   * @param parent the parent command to attach to
   */
  static void addCommands(LiteralArgumentBuilder<CommandSource> parent) {
    parent.then(
        literal("add")
            .shortDescription("Creates a new tag.")
            .longDescription("Adds a new tag with the given keyword, tag and"
                + " description. The description has to be in quotes, so the bot is"
                + " able to discern it from the actual tag value. The tag has to be"
                + " unique."
            )
            .then(
                argument("keyword", word()).then(
                    argument("quoted description", string()).then(
                        argument("value", greedyString()).executes(
                            context ->
                                addTag(
                                    context.getSource(),
                                    getString(context, "keyword"),
                                    getString(context, "quoted description"),
                                    getString(context, "value")
                                )
                        )
                    )
                )
            )
    ).then(
        literal("delete")
            .shortDescription("Deletes a tag.")
            .then(
                argument("tag keyword", word()).executes(
                    context ->
                        deleteTag(
                            context.getSource(),
                            getString(context, "tag keyword")
                        )
                )
            )
    ).then(
        literal("edit")
            .shortDescription("Edits a tag.")
            .longDescription(
                "Edits a given tag. You need to supply it with the tag keyword,"
                    + " the new description in quotes and the new value"
            )
            .then(
                argument("tag keyword", word()).then(
                    argument("new description", string()).then(
                        argument("new value", greedyString()).executes(
                            context ->
                                editTag(
                                    context.getSource(),
                                    getString(context, "tag keyword"),
                                    getString(context, "new description"),
                                    getString(context, "new value")
                                )
                        )
                    )
                )
            )
    );
  }

  private static int addTag(CommandSource source, String keyword, String description,
      String value) {
    assertCanModify(source.getMember(), source.getContext().getConfig());

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

    displayTagInfo(source, keyword);
    return 0;
  }

  private static int deleteTag(CommandSource source, String keyword) {
    assertCanModify(source.getMember(), source.getContext().getConfig());

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

  private static int editTag(CommandSource source, String keyword, String desc, String value) {
    assertCanModify(source.getMember(), source.getContext().getConfig());

    TagDao tagDao = source.getContext().getDatabase().getTagDao();

    Optional<MessageTag> tagOptional = tagDao.getByKeyword(keyword);
    if (!tagOptional.isPresent()) {
      return sendTagNotFound(source, keyword);
    }

    tagDao.editTag(
        ImmutableMessageTag.copyOf(tagOptional.get())
            .withValue(value)
            .withDescription(desc)
            .withCreator(source.getUser().getIdLong())
    );

    displayTagInfo(source, keyword);
    return 0;
  }


  private static boolean hasPermissionToModify(Member member, Toml config) {
    Set<Long> roleIds = member.getRoles()
        .stream()
        .map(ISnowflake::getIdLong)
        .collect(Collectors.toSet());

    return config.<Long>getList("commands.tag.modify.allowed-roles")
        .stream()
        .anyMatch(roleIds::contains);
  }

  private static void assertCanModify(Member member, Toml config) {
    if (!hasPermissionToModify(member, config)) {
      throw CommonExceptions.noPermission("Not in a needed role.");
    }
  }
}
