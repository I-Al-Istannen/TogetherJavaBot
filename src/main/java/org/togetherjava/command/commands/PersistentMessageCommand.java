package org.togetherjava.command.commands;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static org.togetherjava.command.CommandGenericHelper.argument;
import static org.togetherjava.command.CommandGenericHelper.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.time.Duration;
import org.togetherjava.command.CommandSource;
import org.togetherjava.command.TJCommand;
import org.togetherjava.messaging.SimpleMessage;

public class PersistentMessageCommand implements TJCommand {

  @Override
  public LiteralCommandNode<CommandSource> getCommand(
      CommandDispatcher<CommandSource> dispatcher) {
    return literal("persist")
        .executes(context -> {
          context.getSource().getMessageSender().sendMessage(
              SimpleMessage.information("This will stay!").notSelfDestructing(),
              context.getSource().getChannel()
          );

          return 0;
        })
        .then(
            argument("seconds", integer())
                .executes(context -> {
                  CommandSource source = context.getSource();

                  Integer seconds = context.getArgument("seconds", Integer.class);

                  source.getMessageSender().sendMessage(
                      SimpleMessage.information("This will self-destruct after " + seconds + "s.")
                          .selfDestructing(Duration.ofSeconds(seconds)),
                      source.getChannel()
                  );
                  return 0;
                })
        )
        .build();
  }
}
