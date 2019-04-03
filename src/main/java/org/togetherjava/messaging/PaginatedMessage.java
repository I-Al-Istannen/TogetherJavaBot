package org.togetherjava.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import org.togetherjava.reactions.ReactionListener;
import org.togetherjava.reactions.ReactionWatcher;
import org.togetherjava.reactions.watchers.PaginationWatcher;

/**
 * A paginated message.
 *
 * @param <T> the type of the displayed elements
 */
public class PaginatedMessage<T> extends BotMessage<PaginatedMessage<T>> {

  private List<T> elements;
  private int pageSize;
  private Function<T, String> toStringFunction;

  private EmbedBuilder embedBuilder;

  private ReactionListener reactionListener;
  private ReactionWatcher reactionWatcher;

  /**
   * Creates a new paginated message
   *
   * @param category the category
   * @param reactionListener the reaction listener to register to
   */
  public PaginatedMessage(MessageCategory category, ReactionListener reactionListener) {
    super(category);
    this.reactionListener = reactionListener;

    this.elements = new ArrayList<>();
    this.pageSize = 10;
    this.toStringFunction = Objects::toString;
    this.embedBuilder = new EmbedBuilder();
  }

  /**
   * Sets the elements this message displays.
   *
   * @param elements the elements
   * @return this object
   */
  public PaginatedMessage<T> withElements(List<T> elements) {
    this.elements = new ArrayList<>(elements);

    return getSelf();
  }

  /**
   * Sets the amount of elements per page.
   *
   * @param pageSize the page size
   * @return this object
   */
  public PaginatedMessage<T> withPageSize(int pageSize) {
    this.pageSize = pageSize;

    return getSelf();
  }

  /**
   * Sets the string converter.
   *
   * @param stringConverter the string converter
   * @return this object
   */
  public PaginatedMessage<T> withToStringConverter(Function<T, String> stringConverter) {
    this.toStringFunction = stringConverter;

    return getSelf();
  }

  @Override
  public void afterSend(Message message) {
    reactionWatcher = new PaginationWatcher<>(elements, pageSize, toStringFunction, message);
    reactionListener.registerWatcher(reactionWatcher);
  }

  @Override
  public void afterDestruction(Message message) {
    if (reactionWatcher != null) {
      reactionListener.unregisterWatcher(reactionWatcher);
    }
  }

  @Override
  protected PaginatedMessage<T> getSelf() {
    return this;
  }

  @Override
  public MessageBuilder toDiscordMessage() {
    return new MessageBuilder()
        .setEmbed(new EmbedBuilder().setDescription("Computing...").build());
  }
}
