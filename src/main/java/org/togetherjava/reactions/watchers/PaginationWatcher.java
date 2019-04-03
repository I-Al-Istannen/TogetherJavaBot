package org.togetherjava.reactions.watchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.RestAction;
import org.togetherjava.autodiscovery.IgnoreAutoDiscovery;
import org.togetherjava.reactions.ReactionWatcher;
import org.togetherjava.util.Context;

/**
 * A watcher that facilitates pagination.
 *
 * <p>You should likely use {@link org.togetherjava.messaging.PaginatedMessage} instead of using
 * this class directly.</p>
 *
 * @param <T> the type of the elements that are displayed.
 */
@IgnoreAutoDiscovery
public class PaginationWatcher<T> implements ReactionWatcher {

  private List<T> elements;
  private int pageSize;
  private Function<T, String> elementToString;
  private Message message;
  private int currentPage;

  /**
   * Creates a new pagination watcher.
   *
   * @param elements the elements to display
   * @param pageSize the size of a page
   * @param elementToString the element to string function
   * @param message the message
   */
  public PaginationWatcher(List<T> elements, int pageSize, Function<T, String> elementToString,
      Message message) {
    this.elements = new ArrayList<>(elements);
    this.pageSize = pageSize;
    this.elementToString = elementToString;
    this.message = message;

    update();
  }

  @Override
  public ReactionResult reactionAdded(Long messageId, User user, MessageReaction reactionEmote,
      Context context) {

    if (messageId != message.getIdLong() || user.isBot()) {
      return ReactionResult.DO_NOTHING;
    }

    String name = reactionEmote.getReactionEmote().getName();

    Optional<NavigationEmoji> emoji = NavigationEmoji.getForEmoji(name);

    if (emoji.isEmpty()) {
      return ReactionResult.DO_NOTHING;
    }

    int offset = emoji.get().getOffset(currentPage);

    currentPage = currentPage + offset;

    update();

    return ReactionResult.DO_NOTHING;
  }

  @Override
  public ReactionResult reactionRemoved(Long messageId, User user, MessageReaction reactionEmote,
      Context context) {

    return ReactionResult.DO_NOTHING;
  }

  private void update() {
    String text = getElements(currentPage).stream()
        .map(elementToString)
        .collect(Collectors.joining("\n**\\*** ", "**\\*** ", ""));
    message.editMessage(
        new MessageBuilder().setEmbed(
            new EmbedBuilder()
                .setDescription(text)
                .build()
        ).build()
    ).queue();

    restoreReactions(message);
  }

  private List<T> getElements(int pageIndex) {
    int firstElementIndex = pageIndex * pageSize;
    int end = firstElementIndex + pageSize;

    return elements.subList(clampToElementCount(firstElementIndex), clampToElementCount(end));
  }

  private int clampToElementCount(int input) {
    return Math.max(0, Math.min(elements.size(), input));
  }

  private int getPageCount() {
    return (int) Math.ceil(elements.size() / (double) pageSize);
  }

  private void restoreReactions(Message message) {
    message.clearReactions().queue(ignored -> {
      if (currentPage > 0) {
        message.addReaction(NavigationEmoji.BACKWARD.getEmoji()).queue();
      }
      for (int i = 0; i < Math.min(getPageCount(), 3); i++) {
        if (i + currentPage >= getPageCount()) {
          break;
        }
        NavigationEmoji.getForNumber(i + currentPage + 1)
            .map(NavigationEmoji::getEmoji)
            .map(message::addReaction)
            .ifPresent(RestAction::queue);
      }

      if (currentPage < getPageCount() - 1) {
        message.addReaction(NavigationEmoji.FORWARD.getEmoji()).queue();
      }

    });
  }
}
