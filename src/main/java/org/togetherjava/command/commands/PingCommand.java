package org.togetherjava.command.commands;

import com.mojang.brigadier.tree.CommandNode;
import net.dv8tion.jda.core.entities.Message;
import org.togetherjava.command.CommandGenericHelper;

public class PingCommand {

  public static CommandNode<Message> create() {
    return CommandGenericHelper.literal("ping")
        .executes(context -> {
          context.getSource().getChannel().sendMessage("Pong!").submit();
          return 0;
        })
        .build();
  }
}
