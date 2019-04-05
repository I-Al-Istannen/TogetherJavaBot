package org.togetherjava.util;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class ListUtils {

  private ListUtils() {
    throw new UnsupportedOperationException("No instantiation");
  }

  /**
   * Returns the given list or the fallback one if the given list is empty.
   *
   * @param first the first list
   * @param fallback the fallback list
   * @param <T> the type of the list elements
   * @return the resulting list
   */
  public static <T> List<T> withFallback(List<T> first, Supplier<List<T>> fallback) {
    if (first.isEmpty()) {
      return fallback.get();
    }
    return first;
  }

  /**
   * Returns the given list or the fallback one if the given list is empty.
   *
   * @param first the first list
   * @param fallback the fallback list
   * @param fallback2 the second fallback list
   * @param <T> the type of the list elements
   * @return the resulting list
   */
  public static <T> List<T> withFallback(List<T> first, Supplier<List<T>> fallback,
      Supplier<List<T>> fallback2) {
    if (first.isEmpty()) {
      return withFallback(fallback.get(), fallback2);
    }
    return first;
  }

  /**
   * Filters a list.
   *
   * @param list the list to filter
   * @param predicate the predicate to use
   * @param <T> the type of the list elements
   * @return a filtered version of the passed list
   */
  public static <T> List<T> filter(List<T> list, Predicate<T> predicate) {
    return list.stream().filter(predicate).collect(Collectors.toList());
  }
}
