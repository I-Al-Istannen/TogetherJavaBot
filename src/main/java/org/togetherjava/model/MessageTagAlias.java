package org.togetherjava.model;

import org.immutables.value.Value;

@Value.Immutable
public abstract class MessageTagAlias {

  /**
   * Returns the keyword for this alias.
   *
   * @return the keyword for this alias
   */
  public abstract String keyword();

  /**
   * The target it aliases to.
   *
   * @return the target it aliases to.
   */
  public abstract String target();
}
