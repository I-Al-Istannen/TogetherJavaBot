package org.togetherjava.util;

public class StringUtils {

  public static final char ZERO_WIDTH_SPACE = '\u200B';

  /**
   * Repeats a given string amount times.
   *
   * @param input the input to repeat
   * @param amount the amount to repeat it by
   * @return the repeated string
   */
  public static String repeat(String input, int amount) {
    StringBuilder result = new StringBuilder();

    for (int i = 0; i < amount; i++) {
      result.append(input);
    }

    return result.toString();
  }
}
