package org.togetherjava.messaging.transforming;

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
}
