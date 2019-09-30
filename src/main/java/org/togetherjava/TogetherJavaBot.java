package org.togetherjava;

import com.moandjiezana.toml.Toml;
import java.sql.SQLException;
import java.util.Objects;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import org.togetherjava.commandrewrite.CommandContext;
import org.togetherjava.commandrewrite.NewCommandListener;
import org.togetherjava.messaging.BotMessage;
import org.togetherjava.messaging.ComplexMessage;
import org.togetherjava.messaging.SimpleMessage;
import org.togetherjava.messaging.sending.DestructingMessageSender;
import org.togetherjava.messaging.transforming.CategoryColorTransformer;
import org.togetherjava.messaging.transforming.EmbedTransformer;
import org.togetherjava.messaging.transforming.Transformer;
import org.togetherjava.reactions.ReactionListener;
import org.togetherjava.storage.sql.Database;

public class TogetherJavaBot {

  private Toml config;
  private JDA jda;

  public TogetherJavaBot(Toml config) {
    this.config = Objects.requireNonNull(config, "config can not be null!");
  }

  /**
   * Starts the bot and waits for it to be ready.
   *
   * @throws InterruptedException if an error occurs while waiting
   * @throws LoginException if the token is invalid or another error prevents login
   */
  public void start(String token) throws InterruptedException, LoginException, SQLException {
    Transformer<BotMessage, BotMessage> simpleMessageTransformer = Transformer
        .defaultTypeSwitch(
            SimpleMessage.class,
            new EmbedTransformer(),
            message -> message
        );

    Transformer<BotMessage, BotMessage> embedColorTransformer = Transformer
        .defaultTypeSwitch(
            ComplexMessage.class,
            new CategoryColorTransformer(),
            it -> it
        );

    Transformer<BotMessage, Message> toMessageTransformer = message -> message.toDiscordMessage()
        .build();

    DestructingMessageSender messageSender = new DestructingMessageSender(
        config,
        simpleMessageTransformer.then(embedColorTransformer).then(toMessageTransformer)
    );

    ReactionListener reactionListener = new ReactionListener();
    Database database = new Database(config.getString("database.connection-url"));

    reactionListener.setContext(
        new CommandContext(null, config, messageSender, reactionListener, database)
    );

    jda = new JDABuilder(AccountType.BOT)
        .setToken(token)
        .addEventListeners(reactionListener)
        .addEventListeners(
            new NewCommandListener(config, messageSender, reactionListener, database)
        )
        .build()
        .awaitReady();
  }

  /**
   * Stops this bot, after all actions were run.
   */
  public void stop() {
    jda.shutdown();
  }
}
