package org.togetherjava.command.commands.javadoc;

import de.ialistannen.htmljavadocparser.JavadocApi;
import de.ialistannen.htmljavadocparser.model.properties.HasFields;
import de.ialistannen.htmljavadocparser.model.properties.Invocable;
import de.ialistannen.htmljavadocparser.model.properties.Invocable.Parameter;
import de.ialistannen.htmljavadocparser.model.properties.JavadocElement;
import de.ialistannen.htmljavadocparser.model.types.JavadocClass;
import de.ialistannen.htmljavadocparser.model.types.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * A selector for javadoc elements
 */
public class JavadocSelector {

  private static final String TYPE_SEPARATOR = "#";

  private String typeName;
  private String memberName;
  private List<String> parameterTypes;
  private JavadocType type;

  private JavadocSelector(String typeName, String memberName, List<String> parameterTypes,
      JavadocType type) {
    this.typeName = typeName;
    this.memberName = memberName;
    this.parameterTypes = parameterTypes;
    this.type = type;
  }

  /**
   * Selects all elements matching this selector.
   *
   * <p>his will only start exploring classes if a single one was found. If there were multiple it
   * just returns the types.</p>
   *
   * @param api the api to query
   * @return all elements matching the selector
   * @throws IllegalArgumentException if you tried to fetch a field from a type that doesn't
   *     have any (e.g. an Annotation)
   */
  public List<? extends JavadocElement> select(JavadocApi api) {
    if (type == JavadocType.PACKAGE) {
      return api.getIndex().getPackage(typeName).stream().collect(Collectors.toList());
    }

    List<Type> potentialTypes = api.getIndex().findMatching(this::matches);

    if (potentialTypes.stream().anyMatch(this::matchesExact)) {
      potentialTypes = potentialTypes.stream()
          .filter(this::matchesExact)
          .collect(Collectors.toList());
    }

    if (type == JavadocType.CLASS) {
      return potentialTypes;
    }

    // Do not resolve further as that would involve webrequests
    if (potentialTypes.size() != 1) {
      return potentialTypes;
    }

    Type foundType = potentialTypes.get(0);

    if (type == JavadocType.FIELD) {
      return findField(foundType);
    }

    return findMethod(foundType);
  }

  private boolean matches(Type element) {
    return element.getFullyQualifiedName().endsWith(typeName);
  }

  private boolean matchesExact(Type element) {
    return element.getSimpleName().equals(typeName);
  }

  @NotNull
  private List<? extends JavadocElement> findField(Type owner) {
    if (!(owner instanceof HasFields)) {
      throw new IllegalArgumentException("The found element has no fields!");
    }

    return ((HasFields) owner).getFields().stream()
        .filter(field -> field.getSimpleName().equals(memberName))
        .collect(Collectors.toList());
  }

  @NotNull
  private List<Invocable> findMethod(Type owner) {
    List<Invocable> allInvocables = new ArrayList<>(owner.getMethods());

    if (owner instanceof JavadocClass) {
      allInvocables.addAll(((JavadocClass) owner).getConstructors());
    }

    List<Invocable> potentialMethods = allInvocables.stream()
        .filter(invocable -> invocable.getSimpleName().startsWith(memberName))
        .collect(Collectors.toList());

    List<Invocable> exactMatchingNames = potentialMethods.stream()
        .filter(invocable -> invocable.getSimpleName().equals(memberName))
        .collect(Collectors.toList());

    if (exactMatchingNames.isEmpty()) {
      return potentialMethods;
    }

    List<Invocable> sameParameters = exactMatchingNames.stream()
        .filter(this::hasSameParameters)
        .collect(Collectors.toList());

    if (sameParameters.isEmpty()) {
      return exactMatchingNames;
    }

    return sameParameters;
  }

  private boolean hasSameParameters(Invocable invocable) {
    List<String> methodParameters = invocable.getParameters().stream()
        .map(Parameter::getType)
        .map(JavadocElement::getSimpleName)
        .collect(Collectors.toList());

    return methodParameters.equals(parameterTypes);
  }

  /**
   * Parses a String to a selector.
   *
   * @param input the input string
   * @return the javadoc selector
   */
  public static JavadocSelector fromString(String input) {
    JavadocType type = parseType(input);

    switch (type) {
      case CLASS:
      case PACKAGE:
        return new JavadocSelector(input, "", List.of(), type);
      case FIELD:
        return new JavadocSelector(extractType(input), extractFieldName(input), List.of(), type);
      case METHOD:
        return new JavadocSelector(
            extractType(input),
            extractMethodName(input),
            extractParameter(input),
            type
        );
      default:
        throw new IllegalArgumentException("Unknown type!");
    }
  }

  private static JavadocType parseType(String input) {
    JavadocType type;
    if (!input.contains(TYPE_SEPARATOR)) {
      if (input.matches(".+[A-z].+")) {
        type = JavadocType.CLASS;
      } else {
        type = JavadocType.PACKAGE;
      }
    } else {
      if (input.contains("(")) {
        type = JavadocType.METHOD;
      } else {
        type = JavadocType.FIELD;
      }
    }

    return type;
  }

  private static String extractType(String input) {
    return input.substring(0, input.indexOf(TYPE_SEPARATOR));
  }

  private static String extractFieldName(String input) {
    return input.substring(input.indexOf(TYPE_SEPARATOR) + 1);
  }

  private static String extractMethodName(String input) {
    String rest = input.substring(input.indexOf(TYPE_SEPARATOR) + 1);
    return rest.substring(0, rest.indexOf('('));
  }

  private static List<String> extractParameter(String input) {
    String inParens = input.replaceAll(".+\\((.*)\\).*", "$1");
    String withoutSpaces = inParens.replaceAll("\\s", "");
    return withoutSpaces.isEmpty() ? List.of() : List.of(withoutSpaces.split(","));
  }

  /**
   * The type of the element.
   */
  public enum JavadocType {
    PACKAGE,
    CLASS,
    FIELD,
    METHOD
  }
}
