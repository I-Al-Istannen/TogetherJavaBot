package org.togetherjava.messaging;

import java.time.Duration;

public abstract class BotMessage<T extends BotMessage<T>> {

  private Duration selfDestructDuration;
  private DestructionState selfDestructingState;
  private MessageCategory category;

  /**
   * Prevent instantiation. This effectively emulates a sealed class hierarchy.
   */
  BotMessage(MessageCategory category) {
    this.category = category;
    this.selfDestructingState = DestructionState.DEFAULT;
  }

  /**
   * Returns this object with the correct type. Used to emulate a self type.
   *
   * @return this object
   */
  protected abstract T getSelf();

  /**
   * The category of the message.
   *
   * @return the message category
   */
  public MessageCategory getCategory() {
    return category;
  }

  /**
   * Sets the category for this message.
   *
   * @param category the {@link MessageCategory}
   */
  public T setCategory(MessageCategory category) {
    this.category = category;
    return getSelf();
  }


  /**
   * Returns the duration after which a message self destructs.
   *
   * @return the duration after which the message self destructs. Not null if {@link
   * #getSelfDestructingState()} is {@link DestructionState#SELF_DESTRUCTING}
   */
  public Duration getSelfDestructDelay() {
    return selfDestructDuration;
  }

  /**
   * Whether this message self-destructs.
   *
   * @return the {@link DestructionState} of this message
   */
  public DestructionState getSelfDestructingState() {
    return selfDestructingState;
  }

  /**
   * Sets this message to self-destruct.
   *
   * @param selfDestructDuration the duration after which it self destructs
   * @return this message
   */
  public T selfDestructing(Duration selfDestructDuration) {
    this.selfDestructDuration = selfDestructDuration;
    this.selfDestructingState = DestructionState.SELF_DESTRUCTING;

    return getSelf();
  }

  /**
   * If called this message will opt out of the {@link DestructionState#DEFAULT} handling and will
   * persist.
   *
   * @return this message
   */
  public T notSelfDestructing() {
    this.selfDestructingState = DestructionState.NOT_DESTRUCTING;

    return getSelf();
  }


  /**
   * The category of the message
   */
  public enum MessageCategory {
    ERROR,
    SUCCESS,
    INFORMATION,
    NONE
  }

  /**
   * Whether this method should self destruct, follow defaults or be persistent.
   */
  public enum DestructionState {
    SELF_DESTRUCTING,
    NOT_DESTRUCTING,
    DEFAULT
  }
}
