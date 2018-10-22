package org.togetherjava;

import com.moandjiezana.toml.Toml;
import java.util.Objects;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Message;
import org.togetherjava.command.CommandListener;
import org.togetherjava.messaging.ComplexMessage;
import org.togetherjava.messaging.SimpleMessage;
import org.togetherjava.messaging.sending.DestructingMessageSender;
import org.togetherjava.messaging.transforming.CategoryColorTransformer;
import org.togetherjava.messaging.transforming.EmbedTransformer;
import org.togetherjava.messaging.transforming.Transformer;

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
  public void start(String token) throws InterruptedException, LoginException {
    Transformer<ComplexMessage, Message> complexTransformer = new CategoryColorTransformer()
        .then(ComplexMessage::build);
    Transformer<SimpleMessage, Message> simpleTransformer = new EmbedTransformer()
        .then(new CategoryColorTransformer())
        .then(ComplexMessage::build);

    jda = new JDABuilder(AccountType.BOT)
        .setToken(token)
        .addEventListener(
            new CommandListener(
                config.getString("commands.prefix"),
                new DestructingMessageSender(config, simpleTransformer, complexTransformer)
            )
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
