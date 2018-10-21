package org.togetherjava.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.dv8tion.jda.core.entities.Message;

public class CommandGenericHelper {

  public static LiteralArgumentBuilder<Message> literal(String literal) {
    return LiteralArgumentBuilder.literal(literal);
  }
}
