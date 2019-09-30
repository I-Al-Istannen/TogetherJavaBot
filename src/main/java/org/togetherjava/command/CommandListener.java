package org.togetherjava.command;

import com.moandjiezana.toml.Toml;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togetherjava.autodiscovery.ClassDiscovery;
import org.togetherjava.command.commands.HelpCommand;
import org.togetherjava.command.exceptions.CommandException;
import org.togetherjava.messaging.SimpleMessage;
import org.togetherjava.permission.PermissionManager;
import org.togetherjava.util.Context;
import org.togetherjava.util.StringUtils;


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
    String command = message.getContentRaw()
        // If this becomes too prevalent it will need to be refactored into a message cleaner system
        .replace(Character.toString(StringUtils.ZERO_WIDTH_SPACE), "");

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
      if (!hasPermission(source, parseResults)) {
        source.getMessageSender().sendMessage(
            SimpleMessage.error("You lack the necessary permissions!"),
            source.getChannel()
        );
        return;
      }
      executeCommand(parseResults, source);
    }
  }

  private boolean hasPermission(CommandSource source, ParseResults<CommandSource> parseResults) {
    PermissionManager permissionManager = context.getPermissionManager();

    for (ParsedCommandNode<CommandSource> parseNode : parseResults.getContext().getNodes()) {
      CommandNode<CommandSource> node = parseNode.getNode();
      if (!permissionManager.hasPermission(source.getMember(), node.getPermission())) {
        return false;
      }
    }
    return true;
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
