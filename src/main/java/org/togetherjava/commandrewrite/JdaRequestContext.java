package org.togetherjava.commandrewrite;

import de.ialistannen.commandprocrastination.context.RequestContext;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

public class JdaRequestContext implements RequestContext {

  private Message message;
  private User user;
  private Guild guild;

  public JdaRequestContext(Message message, User user, Guild guild) {
    this.message = message;
    this.user = user;
    this.guild = guild;
  }

  public Message getMessage() {
    return message;
  }

  public User getUser() {
    return user;
  }

  public Guild getGuild() {
    return guild;
  }
}
