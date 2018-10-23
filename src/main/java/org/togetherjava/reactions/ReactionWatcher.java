package org.togetherjava.reactions;

import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.User;
import org.togetherjava.util.Context;

public interface ReactionWatcher {

  /**
   * Called when a reaction was added.
   *
   * @param messageId the id of the message that was reacted on
   * @param user the user who reacted
   * @param reactionEmote the reacted emote
   * @param context the context to use
   */
  ReactionResult reactionAdded(Long messageId, User user, MessageReaction reactionEmote,
      Context context);

  /**
   * Called when a reaction was removed.
   *
   * @param messageId the id of the message that was reacted on
   * @param user the user who reacted
   * @param reactionEmote the reacted emote
   * @param context the context to use
   */
  ReactionResult reactionRemoved(Long messageId, User user, MessageReaction reactionEmote,
      Context context);

  /**
   * The result of processing a reaction.
   */
  enum ReactionResult {
    /**
     * Unregister this listener.
     */
    UNREGISTER,
    /**
     * Just do nothing.
     */
    DO_NOTHING
  }
}
