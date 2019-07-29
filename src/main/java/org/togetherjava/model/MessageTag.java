package org.togetherjava.model;

import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
public abstract class MessageTag {

  /**
   * A short description of what the tag does.
   *
   * @return the description
   */
  public abstract String description();

  /**
   * The keyword that triggers this tagM
   *
   * @return the tag keyword
   */
  public abstract String keyword();

  /**
   * The value to post.
   *
   * @return the value
   */
  public abstract String value();

  /**
   * The id o the user creating this tag.
   *
   * @return the id of the tag creator
   */
  public abstract long creator();

  /**
   * Returns all aliases that are registered to this tag.
   *
   * @return all aliases for this tag
   */
  public abstract List<String> aliases();
}
