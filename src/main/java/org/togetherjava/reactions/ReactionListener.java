package org.togetherjava.reactions;

import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.togetherjava.autodiscovery.ClassDiscovery;
import org.togetherjava.reactions.ReactionWatcher.ReactionResult;
import org.togetherjava.util.Context;

/**
 * An event listener that watches for reactions and relays that information to subscribed watchers.
 *
 * <P><strong>You must call {@link #setContext(Context)} before registering this listener.</strong>
 */
public class ReactionListener extends ListenerAdapter {

  private List<ReactionWatcher> watchers;
  private Context context;

  public ReactionListener() {
    this.watchers = new ArrayList<>();

    ClassDiscovery.find(
        getClass().getClassLoader(),
        "org.togetherjava.reactions.watchers",
        ReactionWatcher.class
    )
        .forEach(this::registerWatcher);
  }

  /**
   * Sets the contect for this listener. <strong>Must be called before the object can be
   * used.</strong>.
   *
   * @param context the context
   */
  public void setContext(Context context) {
    this.context = context;
  }

  /**
   * Adds a new {@link ReactionWatcher}.
   *
   * @param watcher the watcher to add
   */
  public void registerWatcher(ReactionWatcher watcher) {
    watchers.add(watcher);
  }

  /**
   * Unregisters the given watcher.
   *
   * @param watcher the watcher to remove
   */
  public void unregisterWatcher(ReactionWatcher watcher) {
    watchers.remove(watcher);
  }

  @Override
  public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
    watchers.removeIf(watcher ->
        watcher.reactionRemoved(event.getMessageIdLong(), event.getUser(), event.getReaction(),
            context)
            == ReactionResult.UNREGISTER
    );
  }

  @Override
  public void onMessageReactionAdd(MessageReactionAddEvent event) {
    watchers.removeIf(watcher ->
        watcher
            .reactionAdded(event.getMessageIdLong(), event.getUser(), event.getReaction(), context)
            == ReactionResult.UNREGISTER
    );
  }
}
