package org.togetherjava.command.commands.tag;

import static org.togetherjava.command.CommandGenericHelper.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.togetherjava.command.CommandSource;
import org.togetherjava.command.TJCommand;

/**
 * Provides a tag system (recallable snippets).
 */
public class TagCommand implements TJCommand {

  @Override
  public LiteralCommandNode<CommandSource> getCommand(CommandDispatcher<CommandSource> dispatcher) {
    return literal("tag")
        .then(new TagCreateCommand().getCommand())
        .then(new TagListCommand().getCommand())
        .then(new TagInfoCommand().getCommand())
        .then(new TagDeleteCommand().getCommand())
        .build();
  }
}
