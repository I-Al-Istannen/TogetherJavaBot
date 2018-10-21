package org.togetherjava.command;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togetherjava.command.commands.PingCommand;
import org.togetherjava.messaging.ComplexMessage;
import org.togetherjava.messaging.MessageCategory;
import org.togetherjava.messaging.messages.CommandMessages;
import org.togetherjava.messaging.sending.MessageSender;

public class CommandListener extends ListenerAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommandListener.class);

  private Cache<String, ParseResults<CommandSource>> commandCache;

  private CommandDispatcher<CommandSource> dispatcher;
  private String prefix;
  private MessageSender messageSender;

  public CommandListener(String prefix, MessageSender messageSender) {
    this.prefix = prefix;
    this.messageSender = messageSender;
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

    CommandSource source = new CommandSource(message, messageSender);
    ParseResults<CommandSource> parseResults = parse(command, source);

    if (commandFound(parseResults)) {
      executeCommand(parseResults, source);
    } else {
      sendCommandNotFound(parseResults, message);
    }
  }

  private ParseResults<CommandSource> parse(String command, CommandSource source) {
    try {
      return commandCache.get(command, () -> dispatcher.parse(command, source));
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean commandFound(ParseResults<CommandSource> parseResults) {
    return !parseResults.getReader().canRead();
  }

  private void executeCommand(ParseResults<CommandSource> parseResults, CommandSource source) {
    try {
      dispatcher.execute(parseResults);
    } catch (CommandSyntaxException e) {
      String error = e.getMessage();

      ComplexMessage complexMessage = new ComplexMessage(MessageCategory.ERROR);
      EmbedBuilder embedBuilder = complexMessage.getEmbedBuilder();

      for (var entry : parseResults.getContext().getNodes().entrySet()) {
        var childrenUsage = dispatcher.getSmartUsage(entry.getKey(), source).values()
            .stream()
            .collect(Collectors.joining(" || "));
        embedBuilder
            .addField(
                "Arguments for '" + entry.getKey().getName() + "'",
                childrenUsage,
                true
            );
      }
      messageSender.sendMessage(complexMessage, source.getChannel());
    }
  }

  private void sendCommandNotFound(ParseResults<CommandSource> parseResults, Message message) {
    LOGGER.info("Command not found for user {}", message.getAuthor().getAsMention());

    parseResults.getExceptions().values().forEach(Throwable::printStackTrace);

    messageSender.sendMessage(CommandMessages.commandNotFound(), message.getChannel());
  }
}
