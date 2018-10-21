package org.togetherjava.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

public class CommandGenericHelper {

  public static LiteralArgumentBuilder<CommandSource> literal(String literal) {
    return LiteralArgumentBuilder.literal(literal);
  }

  public static <T> RequiredArgumentBuilder<CommandSource, T> argument(String name,
      ArgumentType<T> type) {
    return RequiredArgumentBuilder.argument(name, type);
  }
}
