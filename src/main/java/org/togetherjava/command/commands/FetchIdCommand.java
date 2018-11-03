package org.togetherjava.command.commands;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static org.togetherjava.command.CommandGenericHelper.argument;
import static org.togetherjava.command.CommandGenericHelper.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.stream.Collectors;
import org.togetherjava.command.CommandSource;
import org.togetherjava.command.TJCommand;
import org.togetherjava.messaging.SimpleMessage;

public class FetchIdCommand implements TJCommand {

  @Override
  public LiteralCommandNode<CommandSource> getCommand(CommandDispatcher<CommandSource> dispatcher) {
    return literal("fetchId")
        .then(
            literal("role")
                .then(
                    argument("name", greedyString())
                        .executes(context -> showRoleId(
                            getString(context, "name"),
                            context.getSource()
                        ))
                )
        )
        .then(
            literal("channel")
                .then(
                    argument("name", greedyString())
                        .executes(context -> showChannelId(
                            getString(context, "name"),
                            context.getSource()
                        ))
                )
        )
        .build();
  }

  private int showRoleId(String name, CommandSource source) {
    String roles = source.getMessage().getGuild().getRolesByName(name, true)
        .stream()
        .map(role -> "**" + role.getName() + "**: `" + role.getId() + "`")
        .collect(Collectors.joining("\n", "**__Matching roles:__**\n\n", ""));

    source.getContext().getMessageSender().sendMessage(
        SimpleMessage.information(roles),
        source.getChannel()
    );

    return 0;
  }

  private int showChannelId(String name, CommandSource source) {
    String roles = source.getMessage().getGuild().getChannels(false)
        .stream()
        .filter(channel -> channel.getName().equalsIgnoreCase(name))
        .map(channel ->
            "**" + channel.getName() + "**: `" + channel.getId() + "` (" + channel.getType() + ")"
        )
        .collect(Collectors.joining("\n", "**__Matching channels:__**\n\n", ""));

    source.getContext().getMessageSender().sendMessage(
        SimpleMessage.information(roles),
        source.getChannel()
    );

    return 0;
  }
}
