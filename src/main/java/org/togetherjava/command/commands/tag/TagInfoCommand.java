package org.togetherjava.command.commands.tag;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static org.togetherjava.command.CommandGenericHelper.argument;
import static org.togetherjava.command.CommandGenericHelper.literal;

import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Optional;
import org.togetherjava.command.CommandSource;
import org.togetherjava.messaging.BotMessage.MessageCategory;
import org.togetherjava.messaging.ComplexMessage;
import org.togetherjava.messaging.SimpleMessage;
import org.togetherjava.messaging.sending.MessageSender;
import org.togetherjava.model.MessageTag;

/**
 * Displays information about a tag.
 */
class TagInfoCommand {

  /**
   * Returns the info command.
   *
   * @return the info command
   */
  LiteralCommandNode<CommandSource> getCommand() {
    return literal("info")
        .shortDescription("Returns information about a tag")
        .longDescription("Lists a tag's value, description and creator.")
        .then(argument("name", greedyString())
            .executes(context -> {
              String keyword = context.getArgument("name", String.class);
              MessageSender sender = context.getSource().getMessageSender();
              Optional<MessageTag> tag = context.getSource()
                  .getContext()
                  .getDatabase()
                  .getTagDao()
                  .getTagForKeyword(keyword);

              if (tag.isEmpty()) {
                sender.sendMessage(
                    SimpleMessage.error("Couldn't find that tag (" + keyword + ")"),
                    context.getSource().getChannel()
                );
                return 0;
              }

              MessageTag messageTag = tag.get();

              String ownerName = "<@!" + messageTag.creator() + ">";

              ComplexMessage message = new ComplexMessage(MessageCategory.INFORMATION)
                  .editEmbed(it -> it.setDescription(messageTag.value()))
                  .editEmbed(it -> it.addField("Creator", ownerName, true))
                  .editEmbed(it -> it.addField("Description", messageTag.description(), true))
                  .editEmbed(it -> it.setTitle(messageTag.keyword()));

              sender.sendMessage(message, context.getSource().getChannel());

              return 0;
            })
        )
        .build();
  }
}
