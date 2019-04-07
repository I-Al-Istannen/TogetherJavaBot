package org.togetherjava.command;

import com.moandjiezana.toml.Toml;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.RootCommandNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togetherjava.autodiscovery.ClassDiscovery;
import org.togetherjava.command.commands.HelpCommand;
import org.togetherjava.command.exceptions.CommandException;
import org.togetherjava.messaging.SimpleMessage;
import org.togetherjava.util.Context;


/**
 * An event listener that reads messages, finds commands in them, parses it and delegates them to
 * the registered commands..
 *
 * <P><strong>You must call {@link #setContext(Context)} before registering this listener.</strong>
 */
public class CommandListener extends ListenerAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommandListener.class);

  private CommandDispatcher<CommandSource> dispatcher;
  private Collection<String> prefixes;
  private Context context;

  public CommandListener(Collection<String> prefixes, Toml config) {
    this.prefixes = new ArrayList<>(prefixes);
    this.dispatcher = new CommandDispatcher<>();

    RootCommandNode<CommandSource> root = dispatcher.getRoot();
    ClassDiscovery.find(
        getClass().getClassLoader(),
        "org.togetherjava.command.commands",
        TJCommand.class,
        config
    ).stream()
        .map(tjCommand -> tjCommand.getCommand(dispatcher))
        .forEach(root::addChild);
  }

  /**
   * Returns the first (main) prefix.
   *
   * @return the main prefix
   */
  public String getPrefix() {
    return prefixes.iterator().next();
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

    Optional<String> matchingPrefix = prefixes.stream().filter(command::startsWith).findFirst();
    if (matchingPrefix.isEmpty()) {
      return;
    }

    command = command.substring(matchingPrefix.get().length()).trim();

    executeCommand(message, command);
  }

  private void executeCommand(Message message, String command) {
    CommandSource source = new CommandSource(message, context);

    LOGGER.info("Executing command {} for {}", command, message.getAuthor().getName());

    if (command.isEmpty()) {
      // we didn't find anything
      return;
    }

    ParseResults<CommandSource> parseResults = dispatcher.parse(command, source);

    if (commandFound(parseResults)) {
      executeCommand(parseResults, source);
    }
  }

  private boolean commandFound(ParseResults<CommandSource> parseResults) {
    return !parseResults.getReader().canRead();
  }

  private void executeCommand(ParseResults<CommandSource> parseResults, CommandSource source) {
    try {
      dispatcher.execute(parseResults);
    } catch (CommandSyntaxException e) {
      List<ParsedCommandNode<CommandSource>> nodes = parseResults.getContext().getNodes();
      ParsedCommandNode<CommandSource> lastNode = nodes.get(nodes.size() - 1);

      HelpCommand.showOneCommandHelp(
          dispatcher,
          source,
          lastNode.getNode().getName(),
          lastNode.getNode()
      );
    } catch (CommandException e) {
      context.getMessageSender().sendMessage(
          SimpleMessage.error(e.getMessage()),
          source.getChannel()
      );
    }
  }
}
