package org.togetherjava.messaging.transforming;

/**
 * A simple transformer between two types.
 *
 * @param <T> the input type
 * @param <R> the output type
 */
@FunctionalInterface
public interface Transformer<T, R> {

  /**
   * Transforms the given object.
   *
   * @param t the object to transform
   * @return the transformed object
   */
  R transform(T t);

  /**
   * Chains this {@link Transformer} with another.
   *
   * @param next the next transformer
   * @param <S> the return type of the passed transformer
   * @return a transformer that transforms T to S
   */
  default <S> Transformer<T, S> then(Transformer<R, S> next) {
    return inputT -> next.transform(transform(inputT));
  }

  /**
   * Returns a transformer that splits the chain based on the type. Does not accept nulls.
   *
   * @param first the class of the first branch
   * @param second the class of the second branch
   * @param firstTransformer the transformer for the first branch
   * @param secondTransformer the transformer for the second branch
   * @param <T> the original type
   * @param <T1> the type of the first branch
   * @param <T2> the type of the second branch
   * @param <R> the return type
   * @return a {@link Transformer} from T to R that chooses the appropriate branch
   */
  static <T, T1 extends T, T2 extends T, R> Transformer<T, R> typeSwitch(
      Class<T1> first, Class<T2> second,
      Transformer<T1, R> firstTransformer, Transformer<T2, R> secondTransformer) {

    return t -> {
      if (first.isInstance(t)) {
        return firstTransformer.transform(first.cast(t));
      } else if (second.isInstance(t)) {
        return secondTransformer.transform(second.cast(t));
      } else if (t == null) {
        throw new NullPointerException("t is null");
      } else {
        throw new IllegalArgumentException("Unknown type: " + t.getClass().getName());
      }
    };
  }

  /**
   * Returns a transformer that splits the chain based on the type. Does not accept nulls.
   *
   * @param first the class of the first branch
   * @param firstTransformer the transformer for the first branch
   * @param <T> the original type
   * @param <T1> the type of the first branch
   * @param <R> the return type
   * @return a {@link Transformer} from T to R that chooses the appropriate branch
   */
  static <T, T1 extends T, R> Transformer<T, R> defaultTypeSwitch(
      Class<T1> first,
      Transformer<T1, ? extends R> firstTransformer, Transformer<T, ? extends R> otherTransformer) {

    return t -> {
      if (first.isInstance(t)) {
        return firstTransformer.transform(first.cast(t));
      } else if (t == null) {
        throw new NullPointerException("t is null");
      } else {
        return otherTransformer.transform(t);
      }
    };
  }
}
