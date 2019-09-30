package org.togetherjava.commandrewrite;

import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.greedyPhrase;
import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.literal;
import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.word;

import de.ialistannen.commandprocrastination.autodiscovery.ActiveCommand;
import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import de.ialistannen.commandprocrastination.command.tree.data.DefaultDataKey;
import de.ialistannen.commandprocrastination.parsing.ParseException;
import de.ialistannen.commandprocrastination.parsing.SuccessParser;
import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import org.togetherjava.messaging.SimpleMessage;

@ActiveCommand(name = "fetchId", parentClass = PrefixedBaseCommand.class)
public class FetchIdCommand extends CommandNode<CommandContext> {

  public FetchIdCommand() {
    super(FetchIdCommand::runCommand, SuccessParser.wrapping(literal("fetch id")));
    setData(DefaultDataKey.SHORT_DESCRIPTION, "Fetches the ID for a role, channel or user");
    setData(DefaultDataKey.USAGE, "fetch id <channel|user|role> <name>");
  }

  private static void runCommand(CommandContext context) throws ParseException {
    String type = context.shift(word());

    switch (type.toLowerCase()) {
      case "channel":
        List<TextChannel> channels = context.getRequestContext()
            .getGuild()
            .getTextChannelsByName(context.shift(greedyPhrase()), true);
        formatAndSendList(context, channels);
      case "user":
        List<Member> members = context.getRequestContext()
            .getGuild()
            .getMembersByName(context.shift(greedyPhrase()), true);
        formatAndSendList(context, members);
        break;
      case "role":
        List<Role> roles = context.getRequestContext()
            .getGuild()
            .getRolesByName(context.shift(greedyPhrase()), true);
        formatAndSendList(context, roles);
        break;
      default:
        context.getSender().sendMessage(
            SimpleMessage.error("I don't understand the type '" + type + "'"),
            context.getRequestContext().getMessage().getChannel()
        );
    }
  }

  private static void formatAndSendList(CommandContext context,
      List<? extends IMentionable> channels) {
    String messageBody = channels.stream()
        .map(it -> "**" + it.getAsMention() + "**: " + it.getId())
        .collect(Collectors.joining("\n"));
    context.getSender().sendMessage(
        SimpleMessage.success(messageBody),
        context.getRequestContext().getMessage().getChannel()
    );
  }
}
