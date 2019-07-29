package org.togetherjava.command.commands.tag;

import static org.togetherjava.command.CommandGenericHelper.literal;

import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.togetherjava.command.CommandSource;
import org.togetherjava.messaging.SimpleMessage;
import org.togetherjava.messaging.sending.MessageSender;
import org.togetherjava.model.MessageTag;

/**
 * Lists all tags and their aliases.
 */
class TagListCommand {

  /**
   * Returns the list command.
   *
   * @return the list command
   */
  LiteralCommandNode<CommandSource> getCommand() {
    return literal("list")
        .shortDescription("Lists all tags")
        .longDescription(
            "Lists all tags and their aliases. The tags are bold and their aliases follow."
        )
        .executes(context -> {
          CommandSource commandSource = context.getSource();
          MessageSender sender = commandSource.getMessageSender();

          Collection<MessageTag> tags = commandSource.getContext()
              .getDatabase()
              .getTagDao()
              .getAllTags();

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

          sender.sendMessage(
              SimpleMessage.information("I know:\n" + tagString),
              commandSource.getChannel()
          );

          return 0;
        })
        .build();
  }
}
