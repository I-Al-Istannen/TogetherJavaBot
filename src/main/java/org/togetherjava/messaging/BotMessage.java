package org.togetherjava.messaging;

import java.time.Duration;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import org.togetherjava.messaging.transforming.Transformer;

/**
 * A bot message.
 *
 * @param <T> the type of the message
 */
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
   *     #getSelfDestructingState()} is {@link DestructionState#SELF_DESTRUCTING}
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
   * Applies the given {@link Transformer} to this message
   *
   * @param transformer the transformer to apply
   * @return this message
   */
  public T applyTransformer(Transformer<T, T> transformer) {
    transformer.transform(getSelf());

    return getSelf();
  }

  /**
   * Converts this bor message to a discord message builder.
   *
   * @return the discord message builder
   */
  public abstract MessageBuilder toDiscordMessage();

  /**
   * Called when the message was sent.
   *
   * @param message the resulting message
   */
  public void afterSend(Message message) {
  }

  /**
   * Called when the message was destructed.
   *
   * @param message the message
   */
  public void afterDestruction(Message message) {
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
