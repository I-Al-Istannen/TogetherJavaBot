package org.togetherjava.command;

import com.google.common.reflect.ClassPath;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandDiscovery {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommandDiscovery.class);

  /**
   * Finds all commands and instabtiates them.
   *
   * @return all found commands
   * @throws RuntimeException if an error occurs while getting the classpath
   */
  public static List<TJCommand> findCommands() {
    try {
      return ClassPath.from(CommandDiscovery.class.getClassLoader())
          .getTopLevelClassesRecursive("org.togetherjava.command.commands")
          .stream()
          .flatMap(instantiateClasses())
          .filter(TJCommand.class::isAssignableFrom)
          .map(aClass -> (Class<TJCommand>) aClass)
          .flatMap(instantiate())
          .collect(Collectors.toList());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static Function<ClassPath.ClassInfo, Stream<Class<?>>> instantiateClasses() {
    return classInfo -> {
      try {
        return Stream.of(Class.forName(classInfo.getName()));
      } catch (ClassNotFoundException e) {
        LOGGER.warn("Could not find command class '" + classInfo.getName() + "'", e);
        return Stream.empty();
      }
    };
  }

  private static <T> Function<Class<T>, Stream<T>> instantiate() {
    return tClass -> {
      try {
        return Stream.of(tClass.getConstructor().newInstance());
      } catch (ReflectiveOperationException e) {
        LOGGER.warn("Could not instantiate command class '" + tClass.getName() + "'", e);
        return Stream.empty();
      }
    };
  }
}
