package org.togetherjava.util;

import com.moandjiezana.toml.Toml;
import org.togetherjava.command.CommandListener;
import org.togetherjava.messaging.sending.MessageSender;
import org.togetherjava.reactions.ReactionListener;

public class Context {

  private MessageSender messageSender;
  private ReactionListener reactionListener;
  private CommandListener commandListener;
  private Toml config;

  public Context(MessageSender messageSender,
      ReactionListener reactionListener, CommandListener commandListener, Toml config) {
    this.messageSender = messageSender;
    this.reactionListener = reactionListener;
    this.commandListener = commandListener;
    this.config = config;
  }

  public MessageSender getMessageSender() {
    return messageSender;
  }

  public ReactionListener getReactionListener() {
    return reactionListener;
  }

  public CommandListener getCommandListener() {
    return commandListener;
  }

  public Toml getConfig() {
    return config;
  }

  public void setReactionListener(ReactionListener reactionListener) {
    this.reactionListener = reactionListener;
  }

  public void setCommandListener(CommandListener commandListener) {
    this.commandListener = commandListener;
  }
}
