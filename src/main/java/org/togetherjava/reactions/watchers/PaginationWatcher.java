package org.togetherjava.reactions.watchers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import org.togetherjava.autodiscovery.IgnoreAutoDiscovery;
import org.togetherjava.commandrewrite.CommandContext;
import org.togetherjava.reactions.ReactionWatcher;

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

  // Store them here so we are not dependent on the enum order and can disable certain ones
  private static final Collection<NavigationEmoji> EMOJIS = List.of(
      NavigationEmoji.BEGINNING, NavigationEmoji.BACKWARD,
      NavigationEmoji.DELETE,
      NavigationEmoji.FORWARD, NavigationEmoji.END
  );

  private List<T> elements;
  private int pageSize;
  private Function<T, String> elementToString;
  private Message message;
  private int currentPage;
  private long ownerId;

  /**
   * Creates a new pagination watcher.
   *
   * @param elements the elements to display
   * @param pageSize the size of a page
   * @param elementToString the element to string function
   * @param message the message
   * @param ownerId the id of the owner
   */
  public PaginationWatcher(List<T> elements, int pageSize, Function<T, String> elementToString,
      Message message, long ownerId) {
    this.elements = new ArrayList<>(elements);
    this.pageSize = pageSize;
    this.elementToString = elementToString;
    this.message = message;
    this.ownerId = ownerId;

    update();
    applyReactions(message);
  }

  private void applyReactions(Message message) {
    for (NavigationEmoji emoji : EMOJIS) {
      message.addReaction(emoji.getEmoji()).queue();
    }
  }

  @Override
  public ReactionResult reactionAdded(Long messageId, User user, MessageReaction reactionEmote,
      CommandContext context) {

    if (messageId != message.getIdLong() || user.getIdLong() != ownerId) {
      return ReactionResult.DO_NOTHING;
    }

    String name = reactionEmote.getReactionEmote().getName();

    Optional<NavigationEmoji> emoji = NavigationEmoji.getForEmoji(name);

    if (emoji.isEmpty()) {
      return ReactionResult.DO_NOTHING;
    }

    if (emoji.get() == NavigationEmoji.DELETE) {
      message.delete().queue();
      return ReactionResult.UNREGISTER;
    }

    currentPage = clamp(
        emoji.get().getNewPageIndex(currentPage, getPageCount() - 1),
        getPageCount() - 1
    );

    // remove the reaction again
    reactionEmote.removeReaction(user).queue();

    update();

    return ReactionResult.DO_NOTHING;
  }

  @Override
  public ReactionResult reactionRemoved(Long messageId, User user, MessageReaction reactionEmote,
      CommandContext context) {

    return ReactionResult.DO_NOTHING;
  }

  private void update() {
    String text = getElements(currentPage).stream()
        .map(elementToString)
        .collect(Collectors.joining("\n**\\*** ", "**\\*** ", ""));
    message.editMessage(
        new MessageBuilder().setEmbed(
            new EmbedBuilder()
                .setTitle(String.format("Page %d of %d", currentPage + 1, getPageCount()))
                .setDescription(text)
                .build()
        ).build()
    ).queue();
  }

  private int getPageCount() {
    return (int) Math.ceil(elements.size() / (double) pageSize);
  }

  private List<T> getElements(int pageIndex) {
    int firstElementIndex = pageIndex * pageSize;
    int end = firstElementIndex + pageSize;

    return elements.subList(clamp(firstElementIndex, elements.size()), clamp(end, elements.size()));
  }

  private int clamp(int input, int max) {
    return Math.max(0, Math.min(max, input));
  }
}
