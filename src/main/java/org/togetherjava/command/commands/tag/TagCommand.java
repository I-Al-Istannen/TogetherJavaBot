package org.togetherjava.command.commands.tag;

import static org.togetherjava.command.CommandGenericHelper.argument;
import static org.togetherjava.command.CommandGenericHelper.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.togetherjava.command.CommandSource;
import org.togetherjava.command.TJCommand;
import org.togetherjava.messaging.SimpleMessage;
import org.togetherjava.messaging.sending.MessageSender;
import org.togetherjava.storage.dao.jooq.dao.TagDao;

/**
 * Provides a tag system (recallable snippets).
 */
public class TagCommand implements TJCommand {

  @Override
  public LiteralCommandNode<CommandSource> getCommand(CommandDispatcher<CommandSource> dispatcher) {
    return literal("tag")
        .shortDescription("Manages tags (short text snippets)")
        .longDescription("Allows you to create, delete, view and modify tags.")
        .then(new TagCreateCommand().getCommand())
        .then(new TagListCommand().getCommand())
        .then(new TagInfoCommand().getCommand())
        .then(new TagDeleteCommand().getCommand())
        .then(argument("name", StringArgumentType.greedyString())
            .shortDescription("Recalls a given tag.")
            .longDescription("Recalls a given tag if it exits, else fails silently.")
            .executes(context -> {
              MessageSender sender = context.getSource().getMessageSender();
              TagDao tagDao = context.getSource().getContext().getDatabase().getTagDao();
              String keyword = context.getArgument("name", String.class);

              tagDao.getTagForKeyword(keyword).ifPresent(tag -> sender.sendMessage(
                  SimpleMessage.information(tag.value()),
                  context.getSource().getChannel()
              ));

              return 0;
            }))
        .build();
  }
}
