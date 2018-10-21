package org.togetherjava.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public class CommandGenericHelper {

  public static LiteralArgumentBuilder<CommandSource> literal(String literal) {
    return LiteralArgumentBuilder.literal(literal);
  }
}
