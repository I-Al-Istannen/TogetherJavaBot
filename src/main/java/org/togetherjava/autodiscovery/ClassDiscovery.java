package org.togetherjava.autodiscovery;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassDiscovery {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClassDiscovery.class);

  /**
   * Finds all classes of the given type in a package and instantiates them.
   *
   * @param loader the {@link ClassLoader} to use
   * @param packageName the name of the package to look in
   * @param typeToken the class to search instances for
   * @return all found class instances
   * @throws RuntimeException if an error occurs while getting the classpath
   */
  public static <T> List<T> find(ClassLoader loader, String packageName, Class<T> typeToken) {
    try {
      return ClassPath.from(loader)
          .getTopLevelClassesRecursive(packageName)
          .stream()
          .flatMap(instantiateClasses())
          .filter(typeToken::isAssignableFrom)
          .map(aClass -> (Class<T>) aClass)
          .flatMap(instantiate())
          .collect(Collectors.toList());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static Function<ClassInfo, Stream<Class<?>>> instantiateClasses() {
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
