package org.togetherjava.command.commands.tag;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static org.togetherjava.command.CommandGenericHelper.argument;
import static org.togetherjava.command.CommandGenericHelper.literal;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Optional;
import org.togetherjava.command.CommandSource;
import org.togetherjava.messaging.SimpleMessage;
import org.togetherjava.model.MessageTagAlias;
import org.togetherjava.storage.dao.TagDao;
import org.togetherjava.util.Context;

class AliasCommands {


  /**
   * Adds the commands that create aliases.
   *
   * @param parent the parent command to attach to
   */
  static void addCommands(LiteralArgumentBuilder<CommandSource> parent) {
    parent.then(
        literal("alias")
            .shortDescription("Allows you to create and modify aliases.")
            .longDescription(
                "Aliases are just redirections that point to existing tags. This command allows you"
                    + " to modify and create them. They can come in handy if you want to allow"
                    + " multiple ways to write something, but do not want to duplicate the tag a"
                    + " few times."
            )
            .then(
                literal("add")
                    .shortDescription("Allows you to add an alias")
                    .then(
                        argument("keyword", word()).then(
                            argument("target", word()).executes(
                                context -> addAlias(
                                    context.getSource(),
                                    getString(context, "keyword"),
                                    getString(context, "target")
                                )
                            )
                        )
                    )
            )
            .then(
                literal("delete")
                    .shortDescription("Deletes an existing alias.")
                    .then(
                        argument("keyword", word()).executes(
                            context -> deleteAlias(
                                context.getSource(),
                                getString(context, "keyword")
                            )
                        )
                    )
            )
    );
  }

  private static int addAlias(CommandSource source, String keyword, String target) {
    ModificationCommands.assertCanModify(source.getMember(), source.getContext().getConfig());

    Context context = source.getContext();
    TagDao tagDao = context.getDatabase().getTagDao();

    Optional<MessageTagAlias> existingAlias = tagDao.getAlias(keyword);
    if (existingAlias.isPresent()) {
      context.getMessageSender().sendMessage(
          SimpleMessage.error(
              "The alias '" + keyword + "' already points to '"
                  + existingAlias.get().target() + "'"
          ),
          source.getChannel()
      );
      return 0;
    }

    Optional<MessageTagAlias> targetAlias = tagDao.getAlias(target);
    if (targetAlias.isPresent()) {
      context.getMessageSender().sendMessage(
          SimpleMessage.error(
              "The target '" + target + "' is an alias to '" + targetAlias.get().target() + "'!"
          ),
          source.getChannel()
      );
      return 0;
    }

    if (tagDao.addAlias(keyword, target) == 1) {
      context.getMessageSender().sendMessage(
          SimpleMessage.success("Added the alias '" + keyword + "' for '" + target + "'"),
          source.getChannel()
      );
    } else {
      context.getMessageSender().sendMessage(
          SimpleMessage.error("Failed to add alias '" + keyword + "' for '" + target + "'"),
          source.getChannel()
      );
    }

    return 0;
  }

  private static int deleteAlias(CommandSource source, String keyword) {
    ModificationCommands.assertCanModify(source.getMember(), source.getContext().getConfig());

    boolean success = 1 == source.getContext().getDatabase().getTagDao().deleteAlias(keyword);

    if (success) {
      source.getMessageSender().sendMessage(
          SimpleMessage.success("Successfully deleted the alias '" + keyword + "'"),
          source.getChannel()
      );
    } else {
      source.getMessageSender().sendMessage(
          SimpleMessage.error("Could not delete the alias '" + keyword + "'. Did it exist?"),
          source.getChannel()
      );
    }

    return 0;
  }

}
