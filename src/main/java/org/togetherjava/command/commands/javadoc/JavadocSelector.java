package org.togetherjava.command.commands.javadoc;

import static org.togetherjava.util.ListUtils.filter;

import de.ialistannen.htmljavadocparser.JavadocApi;
import de.ialistannen.htmljavadocparser.model.JavadocPackage;
import de.ialistannen.htmljavadocparser.model.properties.HasFields;
import de.ialistannen.htmljavadocparser.model.properties.Invocable;
import de.ialistannen.htmljavadocparser.model.properties.Invocable.Parameter;
import de.ialistannen.htmljavadocparser.model.properties.JavadocElement;
import de.ialistannen.htmljavadocparser.model.types.JavadocClass;
import de.ialistannen.htmljavadocparser.model.types.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.togetherjava.util.ListUtils;

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
      Optional<JavadocPackage> packageOptional = api.getPackage(typeName);
      if (packageOptional.isPresent()) {
        return List.of(packageOptional.get());
      }
      // fall through, maybe it was a lowercase class name
    }

    List<Type> potentialTypes = ListUtils.withFallback(
        api.findMatching(this::matchesExact),
        () -> api.findMatching(this::matches),
        () -> api.findMatching(this::matchesIgnoreCase)
    );

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

  private boolean matchesIgnoreCase(Type element) {
    return element.getFullyQualifiedName().toLowerCase().endsWith(typeName.toLowerCase());
  }

  private boolean matchesExact(Type element) {
    return element.getSimpleName().equals(typeName);
  }

  @NotNull
  private List<? extends JavadocElement> findField(Type owner) {
    if (!(owner instanceof HasFields)) {
      throw new IllegalArgumentException("The found element has no fields!");
    }

    return filter(
        ((HasFields) owner).getFields(),
        field -> field.getSimpleName().equals(memberName)
    );
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

    // Only fuzzy match now to allow exact matches to win.
    // If we did not do this, it would be impossible to match "method(Function)" if there also is
    // a "method(Functional)", as it would always return both
    List<Invocable> parameterMatches = ListUtils.withFallback(
        filter(exactMatchingNames, this::hasSameParameters),
        () -> filter(exactMatchingNames, this::hasSameParametersFuzzy)
    );

    if (!parameterMatches.isEmpty()) {
      return parameterMatches;
    }

    return exactMatchingNames;
  }

  private boolean hasSameParameters(Invocable invocable) {
    List<Parameter> parameters = invocable.getParameters();

    // Differing parameter count
    if (parameters.size() != parameterTypes.size()) {
      return false;
    }

    for (int i = 0; i < parameters.size(); i++) {
      Type parameter = parameters.get(i).getType();

      String requestedParameter = parameterTypes.get(i);
      boolean matchesFullyQualified = requestedParameter.equals(parameter.getFullyQualifiedName());
      boolean matchesSimpleName = requestedParameter.equals(parameter.getSimpleName());

      if (!matchesFullyQualified && !matchesSimpleName) {
        return false;
      }
    }

    return false;
  }

  private boolean hasSameParametersFuzzy(Invocable invocable) {
    List<JavadocElement> methodParameters = invocable.getParameters().stream()
        .map(Parameter::getType)
        .collect(Collectors.toList());

    // Ensure they have the same amount as our query
    if (methodParameters.size() != parameterTypes.size()) {
      return false;
    }

    for (int i = 0; i < methodParameters.size(); i++) {
      JavadocElement actualParameter = methodParameters.get(i);
      String supplierParameter = parameterTypes.get(i).toLowerCase();

      // allow case insensitive prefix matches (e.g. "fun" for "Function"
      boolean simpleFuzzyMatch = actualParameter.getSimpleName()
          .toLowerCase()
          .startsWith(supplierParameter);
      boolean fullyFuzzyMatch = actualParameter.getFullyQualifiedName()
          .toLowerCase()
          .startsWith(supplierParameter);

      if (!simpleFuzzyMatch && !fullyFuzzyMatch) {
        return false;
      }
    }

    // fuzzy matching successful
    return true;
  }

  @Override
  public String toString() {
    return "JavadocSelector{" +
        "typeName='" + typeName + '\'' +
        ", memberName='" + memberName + '\'' +
        ", parameterTypes=" + parameterTypes +
        ", type=" + type +
        '}';
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
      if (input.matches(".*[A-Z].*")) {
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
    List<String> parameters = withoutSpaces.isEmpty()
        ? List.of()
        : List.of(withoutSpaces.split(","));

    return parameters.stream()
        // Allow ... for arrays as well
        .map(s -> s.replace("...", "[]"))
        .collect(Collectors.toList());
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
