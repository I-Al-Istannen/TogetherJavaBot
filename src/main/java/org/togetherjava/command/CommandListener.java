package org.togetherjava.command;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.RootCommandNode;
import java.util.concurrent.ExecutionException;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togetherjava.autodiscovery.ClassDiscovery;
import org.togetherjava.command.exceptions.CommandException;
import org.togetherjava.messaging.BotMessage.MessageCategory;
import org.togetherjava.messaging.ComplexMessage;
import org.togetherjava.messaging.SimpleMessage;
import org.togetherjava.messaging.messages.CommandMessages;
import org.togetherjava.util.Context;


/**
 * An event listener that reads messages, finds commands in them, parses it and delegates them to
 * the registered commands..
 *
 * <P><strong>You must call {@link #setContext(Context)} before registering this listener.</strong>
 */
public class CommandListener extends ListenerAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommandListener.class);

  private Cache<String, ParseResults<CommandSource>> commandCache;

  private CommandDispatcher<CommandSource> dispatcher;
  private String prefix;
  private Context context;

  public CommandListener(String prefix) {
    this.prefix = prefix;
    this.dispatcher = new CommandDispatcher<>();
    this.commandCache = CacheBuilder.newBuilder()
        .maximumSize(30)
        .build();

    RootCommandNode<CommandSource> root = dispatcher.getRoot();
    ClassDiscovery.find(
        getClass().getClassLoader(),
        "org.togetherjava.command.commands",
        TJCommand.class
    ).stream()
        .map(tjCommand -> tjCommand.getCommand(dispatcher))
        .forEach(root::addChild);
  }

  public String getPrefix() {
    return prefix;
  }

  /**
   * Sets the {@link Context}. <strong>Must be invoked before this object can be used.</strong>
   *
   * @param context the context to use
   */
  public void setContext(Context context) {
    this.context = context;
  }

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    if (context == null) {
      throw new IllegalStateException("setContext not called!");
    }

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

    CommandSource source = new CommandSource(message, context);

    // Redirect to help, but without redirection
    if (command.isEmpty()) {
      command = "help";
    }

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
      ComplexMessage complexMessage = new ComplexMessage(MessageCategory.ERROR);
      EmbedBuilder embedBuilder = complexMessage.getEmbedBuilder();

      for (var parseResult : parseResults.getContext().getNodes()) {
        var childrenUsage = String
            .join(" || ", dispatcher.getSmartUsage(parseResult.getNode(), source).values());
        embedBuilder
            .addField(
                "Arguments for '" + parseResult.getNode().getName() + "'",
                childrenUsage,
                true
            );
      }
      context.getMessageSender().sendMessage(complexMessage, source.getChannel());
    } catch (CommandException e) {
      context.getMessageSender().sendMessage(
          SimpleMessage.error(e.getMessage()),
          source.getChannel()
      );
    }
  }

  private void sendCommandNotFound(ParseResults<CommandSource> parseResults, Message message) {
    LOGGER.info("Command not found for user {}", message.getAuthor().getAsMention());

    parseResults.getExceptions().values().forEach(Throwable::printStackTrace);

    context.getMessageSender().sendMessage(
        CommandMessages.commandNotFound(parseResults.getReader().getString()),
        message.getChannel()
    );
  }
}
