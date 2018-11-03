package org.togetherjava.command.commands.tag;

import static org.togetherjava.command.CommandGenericHelper.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.togetherjava.command.CommandSource;
import org.togetherjava.command.TJCommand;
import org.togetherjava.messaging.SimpleMessage;

public class TagCommand implements TJCommand {

  @Override
  public LiteralCommandNode<CommandSource> getCommand(CommandDispatcher<CommandSource> dispatcher) {
    LiteralArgumentBuilder<CommandSource> parent = literal("tag");
    ModificationCommands.addCommands(parent);
    DisplayCommands.addCommands(parent);

    return parent.build();
  }

  static int sendTagAlreadyExists(CommandSource source, String keyword) {
    source.getMessageSender().sendMessage(
        SimpleMessage.error("Tag '" + keyword + "' already exists"),
        source.getChannel()
    );
    return -1;
  }

  static int sendTagNotFound(CommandSource source, String keyword) {
    source.getMessageSender().sendMessage(
        SimpleMessage.error("Tag '" + keyword + "' not found."),
        source.getChannel()
    );
    return -1;
  }
}
