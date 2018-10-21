package org.togetherjava;

import com.moandjiezana.toml.Toml;
import java.util.Objects;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import org.togetherjava.command.CommandListener;

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
  public void start() throws InterruptedException, LoginException {
    jda = new JDABuilder(AccountType.BOT)
        .setToken(config.getString("login.token"))
        .addEventListener(new CommandListener(config.getString("commands.prefix")))
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
