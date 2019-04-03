package org.togetherjava.reactions.watchers;

import java.util.Arrays;
import java.util.Optional;

/**
 * Contains emojis representing numbers or forward/backward movements.
 */
public enum NavigationEmoji {
  ZERO("0⃣", true, 0),
  ONE("1⃣", true, 1),
  TWO("2⃣", true, 2),
  THREE("3⃣", true, 3),
  FOUR("4⃣", true, 4),
  FIVE("5⃣", true, 5),
  SIX("6⃣", true, 6),
  SEVEN("7⃣", true, 7),
  EIGHT("8⃣", true, 8),
  NINE("9⃣", true, 9),
  FORWARD("▶", false, 1),
  BACKWARD("◀", false, -1);

  private String emoji;
  private boolean isNumber;
  private int number;

  NavigationEmoji(String emoji, boolean isNumber, int number) {
    this.emoji = emoji;
    this.isNumber = isNumber;
    this.number = number;
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
   * Returns the offset needed to reach this element.
   *
   * @param current the current page, zero based
   * @return the offset needed to reach this element.
   */
  public int getOffset(int current) {
    if (isNumber) {
      // input is zero based, numbers displayed are not
      return number - current - 1;
    }
    return number;
  }

  /**
   * Returns the emoji for the given emoji string.
   *
   * @param emoji the emoji string
   * @return the NavigationEmoji
   */
  public static Optional<NavigationEmoji> getForEmoji(String emoji) {
    return Arrays.stream(values())
        .filter(it -> it.getEmoji().equals(emoji))
        .findFirst();
  }

  /**
   * Returns the emoji for the given number.
   *
   * @param number the number
   * @return the NavigationEmoji
   */
  public static Optional<NavigationEmoji> getForNumber(int number) {
    return Arrays.stream(values())
        .filter(it -> it.number == number)
        .findFirst();
  }
}
