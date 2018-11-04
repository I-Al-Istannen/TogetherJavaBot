package org.togetherjava.command.commands;

import static com.mojang.brigadier.arguments.LongArgumentType.getLong;
import static com.mojang.brigadier.arguments.LongArgumentType.longArg;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static org.togetherjava.command.CommandGenericHelper.argument;
import static org.togetherjava.command.CommandGenericHelper.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.List;
import java.util.Optional;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import org.togetherjava.command.CommandSource;
import org.togetherjava.command.TJCommand;
import org.togetherjava.messaging.SimpleMessage;

public class MessageLinkCommand implements TJCommand {

  private static final String BASE = "https://discordapp.com/channels/%d/%d/%d";

  @Override
  public LiteralCommandNode<CommandSource> getCommand(CommandDispatcher<CommandSource> dispatcher) {
    return literal("link")
        .shortDescription("Allows you to get permanent links to messages.")
        .then(
            argument("messageId", longArg(0))
                .shortDescription("Retrieves the full link for a given message id in this channel.")
                .executes(
                    context -> sendMessageFetchedById(
                        context.getSource(),
                        getLong(context, "messageId")
                    )
                )
        )
        .then(
            argument("channelId", longArg(0))
                .shortDescription("Retrieves the full link for a given message in a given channel.")
                .then(
                    argument("messageId", longArg(0)).executes(
                        context -> sendMessageFetchedById(
                            context.getSource(),
                            getLong(context, "channelId"),
                            getLong(context, "messageId")
                        )
                    )
                )
        )
        .then(
            literal("lastFrom")
                .shortDescription("Links the last message of the given person.")
                .then(argument("user", greedyString()).executes(
                    context -> sendMessageLastFrom(
                        context.getSource(),
                        getString(context, "user")
                    ))
                )
        )
        .build();
  }

  private static int sendMessageFetchedById(CommandSource source, long messageId) {
    return sendMessageFetchedById(source, source.getChannel().getIdLong(), messageId);
  }

  private static int sendMessageFetchedById(CommandSource source, long channelId, long messageId) {
    long serverId = source.getMember().getGuild().getIdLong();

    String url = String.format(BASE, serverId, channelId, messageId);

    source.getMessageSender().sendMessage(
        SimpleMessage.success("Your link is: " + url + "."),
        source.getChannel()
    );

    return 0;
  }

  private static int sendMessageLastFrom(CommandSource source, String user) {
    if (!source.getMessage().getMentionedMembers().isEmpty()) {
      return sendMessageLastFrom(source, source.getMessage().getMentionedMembers().get(0));
    }

    List<Member> users = source.getMember().getGuild().getMembersByName(user, true);

    if (users.isEmpty()) {
      source.getMessageSender().sendMessage(
          SimpleMessage.error("User '" + user + "' not found. Maybe try a mention?"),
          source.getChannel()
      );
      return 0;
    }

    return sendMessageLastFrom(source, users.get(0));
  }

  private static int sendMessageLastFrom(CommandSource source, Member user) {
    source.getChannel()
        .getHistory()
        .retrievePast(60)
        .queue(messages -> {
          Optional<Message> lastMessage = messages.stream()
              .filter(message -> message.getMember().equals(user))
              .findFirst();

          if (!lastMessage.isPresent()) {
            sendMessageNotFound(source, user);
          } else {
            sendMessageFetchedById(source, lastMessage.get().getIdLong());
          }
        });

    return 0;
  }

  private static void sendMessageNotFound(CommandSource source, Member user) {
    source.getMessageSender().sendMessage(
        SimpleMessage.error(
            "Could not retrieve the last message for '" + user.getEffectiveName() + "' in <#"
                + source.getChannel().getId() + ">. Maybe it is too old?"
        ),
        source.getChannel()
    );
  }
}
