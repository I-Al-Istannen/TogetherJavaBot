package org.togetherjava.reactions.watchers;

import static org.togetherjava.reactions.ReactionWatcher.ReactionResult.DO_NOTHING;

import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.User;
import org.togetherjava.messaging.SimpleMessage;
import org.togetherjava.reactions.ReactionWatcher;
import org.togetherjava.util.Context;

public class GreetingReactionWatcher implements ReactionWatcher {

  @Override
  public ReactionResult reactionAdded(Long messageId, User user, MessageReaction reactionEmote,
      Context context) {
    context.getMessageSender().sendMessage(
        SimpleMessage.information("Hello " + user.getAsMention()),
        reactionEmote.getChannel()
    );
    return DO_NOTHING;
  }

  @Override
  public ReactionResult reactionRemoved(Long messageId, User user, MessageReaction reactionEmote,
      Context context) {
    context.getMessageSender().sendMessage(
        SimpleMessage.information("Bye " + user.getAsMention()),
        reactionEmote.getChannel()
    );

    return DO_NOTHING;
  }
}
