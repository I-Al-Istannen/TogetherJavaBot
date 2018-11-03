package org.togetherjava.command.exceptions;

public class CommonExceptions {

  public static CommandException noPermission(String permission) {
    return new CommandException("Sorry, but I can not let you do that (" + permission + ").");
  }
}
