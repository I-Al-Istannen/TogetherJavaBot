package org.togetherjava.util;

import com.moandjiezana.toml.Toml;
import org.togetherjava.command.CommandListener;
import org.togetherjava.messaging.sending.MessageSender;
import org.togetherjava.reactions.ReactionListener;
import org.togetherjava.storage.sql.Database;

public class Context {

  private MessageSender messageSender;
  private ReactionListener reactionListener;
  private CommandListener commandListener;
  private Toml config;
  private Database database;

  public Context(MessageSender messageSender,
      ReactionListener reactionListener, CommandListener commandListener, Toml config,
      Database database) {
    this.messageSender = messageSender;
    this.reactionListener = reactionListener;
    this.commandListener = commandListener;
    this.config = config;
    this.database = database;
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

  public Database getDatabase() {
    return database;
  }
}
