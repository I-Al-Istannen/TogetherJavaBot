package org.togetherjava.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;

/**
 * All implementing class need to have a no-args constructor.
 */
public interface TJCommand {

  /**
   * Returns the command node for this command.
   *
   * @param dispatcher the {@link CommandDispatcher} to use
   * @return the created command node
   */
  LiteralCommandNode<CommandSource> getCommand(CommandDispatcher<CommandSource> dispatcher);
}
