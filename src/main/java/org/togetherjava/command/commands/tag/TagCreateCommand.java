package org.togetherjava.command.commands.tag;

import static org.togetherjava.command.CommandGenericHelper.argument;
import static org.togetherjava.command.CommandGenericHelper.literal;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.togetherjava.command.CommandSource;
import org.togetherjava.messaging.SimpleMessage;
import org.togetherjava.messaging.sending.MessageSender;
import org.togetherjava.model.ImmutableMessageTag;
import org.togetherjava.model.MessageTag;
import org.togetherjava.storage.sql.Database;

/**
 * A command to create a tag.
 */
class TagCreateCommand {

  /**
   * Returns the create command.
   *
   * @return the create command
   */
  LiteralCommandNode<CommandSource> getCommand() {
    return literal("add")
        .then(argument("name", StringArgumentType.string())
            .then(argument("description", StringArgumentType.string())
                .then(argument("value", StringArgumentType.greedyString())
                    .executes(context -> {
                      Database database = context.getSource().getContext().getDatabase();
                      MessageSender sender = context.getSource().getMessageSender();
                      String name = context.getArgument("name", String.class);
                      String description = context.getArgument("description", String.class);
                      String value = context.getArgument("value", String.class);

                      MessageTag tag = ImmutableMessageTag.builder()
                          .keyword(name)
                          .description(description)
                          .value(value)
                          .creator(context.getSource().getUser().getIdLong())
                          .build();

                      database.getTagDao().addOrUpdate(tag);

                      sender.sendMessage(
                          SimpleMessage.success("Added the tag " + name),
                          context.getSource().getChannel()
                      );

                      return 0;
                    })
                ))
        )
        .build();
  }
}
