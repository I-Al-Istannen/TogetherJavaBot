package org.togetherjava.command.commands.javadoc;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static org.togetherjava.command.CommandGenericHelper.argument;
import static org.togetherjava.command.CommandGenericHelper.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.ialistannen.htmljavadocparser.JavadocApi;
import de.ialistannen.htmljavadocparser.model.doc.JavadocComment;
import de.ialistannen.htmljavadocparser.model.properties.JavadocElement;
import de.ialistannen.htmljavadocparser.resolving.CachingDocumentResolver;
import de.ialistannen.htmljavadocparser.resolving.CachingDocumentResolver.SimpleCache;
import de.ialistannen.htmljavadocparser.resolving.DocumentResolver;
import java.io.IOException;
import java.lang.module.ResolutionException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.togetherjava.command.CommandSource;
import org.togetherjava.command.TJCommand;
import org.togetherjava.command.commands.javadoc.formatting.JavadocMessageFormatter;
import org.togetherjava.command.exceptions.CommandException;
import org.togetherjava.messaging.BotMessage.MessageCategory;
import org.togetherjava.messaging.ComplexMessage;
import org.togetherjava.messaging.SimpleMessage;

public class JavadocCommand implements TJCommand {

  private JavadocApi javadocApi;
  private JavadocMessageFormatter messageFormatter;

  public JavadocCommand() {
    this.messageFormatter = new JavadocMessageFormatter();

    String baseUrl = "https://docs.oracle.com/javase/10/docs/api";
    DocumentResolver documentResolver = new CachingDocumentResolver(
        url -> {
          try {
            return Jsoup.connect(url).get();
          } catch (IOException e) {
            e.printStackTrace();
            throw new ResolutionException(e);
          }
        },
        new SimpleCache<>() {
          private Map<String, Document> cache = new HashMap<>();

          @Override
          public void put(String key, Document value) {
            cache.put(key, value);
          }

          @Override
          public Document get(String key) {
            return cache.get(key);
          }
        }
    );

    this.javadocApi = new JavadocApi(baseUrl, documentResolver);
  }

  @Override
  public LiteralCommandNode<CommandSource> getCommand(CommandDispatcher<CommandSource> dispatcher) {
    return literal("doc")
        .then(
            argument("fqn", greedyString())
                .executes(context -> {
                  String selectorString = context.getArgument("fqn", String.class);
                  CommandSource commandSource = context.getSource();

                  JavadocSelector selector = JavadocSelector.fromString(selectorString);

                  try {
                    List<? extends JavadocElement> foundElements = selector.select(javadocApi);

                    if (foundElements.isEmpty()) {
                      commandSource.getMessageSender().sendMessage(
                          SimpleMessage.error("Nothing found :("),
                          commandSource.getChannel()
                      );
                      return 0;
                    }

                    if (foundElements.size() == 1) {
                      return sendSingleJavadoc(commandSource, foundElements.get(0));
                    }

                    return sendMultipleFoundError(commandSource, foundElements);
                  } catch (IllegalArgumentException e) {
                    throw new CommandException(e.getMessage());
                  }
                })
        )
        .build();
  }

  private int sendSingleJavadoc(CommandSource source, JavadocElement element) {
    ComplexMessage message = new ComplexMessage(MessageCategory.NONE);
    Optional<JavadocComment> javadoc = element.getJavadoc();
    if (javadoc.isEmpty()) {
      message.setCategory(MessageCategory.ERROR)
          .editEmbed(eb -> eb.setDescription("No javadoc found on that element"));
    } else {
      messageFormatter.format(message, element);
    }

    source.getMessageSender().sendMessage(
        message, source.getChannel()
    );
    return 0;
  }

  private int sendMultipleFoundError(CommandSource source,
      List<? extends JavadocElement> foundElements) {
    String types = foundElements.stream()
        .map(JavadocElement::getFullyQualifiedName)
        .map(s -> "`" + s + "`")
        .limit(10)
        .collect(Collectors.joining("\n**\\*** ", "**\\*** ", ""));

    ComplexMessage message = new ComplexMessage(MessageCategory.ERROR)
        .editEmbed(eb -> eb.setTitle("I found at least the following types:"))
        .editEmbed(eb -> eb.setDescription(types))
        .selfDestructing(Duration.ofSeconds(15));

    source.getMessageSender().sendMessage(
        message, source.getChannel()
    );

    return 0;
  }
}
