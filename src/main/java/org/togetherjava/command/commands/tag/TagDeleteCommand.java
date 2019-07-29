package org.togetherjava.command.commands.tag;

import static org.togetherjava.command.CommandGenericHelper.argument;
import static org.togetherjava.command.CommandGenericHelper.literal;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Optional;
import org.togetherjava.command.CommandSource;
import org.togetherjava.messaging.SimpleMessage;
import org.togetherjava.messaging.sending.MessageSender;
import org.togetherjava.model.MessageTag;
import org.togetherjava.storage.dao.jooq.dao.TagDao;

/**
 * Allows deleting a tag.
 */
class TagDeleteCommand {

  /**
   * Returns the delete command.
   *
   * @return the delete command
   */
  LiteralCommandNode<CommandSource> getCommand() {
    return literal("delete")
        .then(argument("name", StringArgumentType.greedyString())
            .executes(context -> {
              CommandSource source = context.getSource();
              MessageSender sender = source.getMessageSender();
              TagDao tagDao = source.getContext().getDatabase().getTagDao();
              String keyword = context.getArgument("name", String.class);

              Optional<MessageTag> tag = tagDao.getTagForKeyword(keyword);

              if (tag.isEmpty()) {
                sender.sendMessage(
                    SimpleMessage.error("Could not find the tag '" + keyword + "'"),
                    source.getChannel()
                );
                return 0;
              }

              tagDao.deleteTag(tag.get());

              sender.sendMessage(
                  SimpleMessage.success("Deleted the tag '" + tag.get().keyword() + "'!"),
                  source.getChannel()
              );

              return 0;
            })
        )
        .build();
  }

}
