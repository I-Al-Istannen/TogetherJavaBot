package org.togetherjava.command;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.concurrent.ExecutionException;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togetherjava.command.commands.PingCommand;

public class CommandListener extends ListenerAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommandListener.class);

  private Cache<String, ParseResults<Message>> commandCache;

  private CommandDispatcher<Message> dispatcher;
  private String prefix;

  public CommandListener(String prefix) {
    this.prefix = prefix;
    this.dispatcher = new CommandDispatcher<>();
    this.commandCache = CacheBuilder.newBuilder()
        .maximumSize(30)
        .build();

    dispatcher.getRoot().addChild(PingCommand.create());
  }

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    if (event.getAuthor().isBot() || event.getAuthor().equals(event.getJDA().getSelfUser())) {
      return;
    }

    Message message = event.getMessage();
    String command = message.getContentRaw();

    if (!command.startsWith(prefix)) {
      return;
    }

    command = command.substring(prefix.length())
        .trim();

    ParseResults<Message> parseResults = parse(command, message);

    if (commandFound(parseResults)) {
      executeCommand(parseResults, message);
    } else {
      sendCommandNotFound(parseResults, message);
    }
  }

  private ParseResults<Message> parse(String command, Message message) {
    try {
      return commandCache.get(command, () -> dispatcher.parse(command, message));
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean commandFound(ParseResults<Message> parseResults) {
    return !parseResults.getReader().canRead();
  }

  private void executeCommand(ParseResults<Message> parseResults, Message message) {
    try {
      dispatcher.execute(parseResults);
    } catch (CommandSyntaxException e) {
      e.printStackTrace();
    }
  }

  private void sendCommandNotFound(ParseResults<Message> parseResults, Message message) {
    LOGGER.info("Command not found for user {}", message.getAuthor().getAsMention());

    parseResults.getExceptions().values().forEach(Throwable::printStackTrace);

    message.getChannel().sendMessage(
        new EmbedBuilder()
            .setDescription("Command not found :(")
            .build()
    ).submit();
  }
}
