package org.togetherjava.commandrewrite;

import com.moandjiezana.toml.Toml;
import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import de.ialistannen.commandprocrastination.context.Context;
import de.ialistannen.commandprocrastination.context.RequestContext;
import de.ialistannen.commandprocrastination.util.StringReader;
import org.togetherjava.messaging.sending.MessageSender;

public class CommandContext extends Context {

  private Toml config;
  private MessageSender sender;
  private JdaRequestContext requestContext;

  public CommandContext(StringReader reader, CommandNode<?> finalNode, Toml config,
      MessageSender sender,
      JdaRequestContext requestContext) {
    super(reader, finalNode);
    this.config = config;
    this.sender = sender;
    this.requestContext = requestContext;
  }

  public Toml getConfig() {
    return config;
  }

  public MessageSender getSender() {
    return sender;
  }

  public JdaRequestContext getRequestContext() {
    return requestContext;
  }
}
