package org.togetherjava.reactions.watchers;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

/**
 * Contains emojis representing numbers or forward/backward movements.
 */
public enum NavigationEmoji {
  BEGINNING("⏮", ignored -> 0),
  BACKWARD("⏪", current -> current - 1),
  DELETE("❌", IntUnaryOperator.identity()),
  FORWARD("⏩", current -> current + 1),
  END("⏭", (ignored, last) -> last);

  private String emoji;
  private IntBinaryOperator offset;

  NavigationEmoji(String emoji, IntUnaryOperator offset) {
    this(emoji, (current, max) -> offset.applyAsInt(current));
  }

  NavigationEmoji(String emoji, IntBinaryOperator offset) {
    this.emoji = emoji;
    this.offset = offset;
  }

  /**
   * Returns the emoji.
   *
   * @return the emoji
   */
  public String getEmoji() {
    return emoji;
  }

  /**
   * Returns the offset.
   *
   * @param currentPage the current page index
   * @param lastPage the last page index
   * @return the offset
   */
  public int getNewPageIndex(int currentPage, int lastPage) {
    return offset.applyAsInt(currentPage, lastPage);
  }

  /**
   * Returns the NavigationEmoji for the unicode emoji.
   *
   * @param emoji the unicode emoji
   * @return the optional
   */
  public static Optional<NavigationEmoji> getForEmoji(String emoji) {
    return Arrays.stream(values())
        .filter(navigationEmoji -> navigationEmoji.getEmoji().equals(emoji))
        .findFirst();
  }
}
