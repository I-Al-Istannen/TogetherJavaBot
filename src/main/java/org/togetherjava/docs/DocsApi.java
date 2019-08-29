package org.togetherjava.docs;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.moandjiezana.toml.Toml;
import de.ialistannen.htmljavadocparser.JavadocApi;
import de.ialistannen.htmljavadocparser.model.properties.JavadocElement;
import de.ialistannen.htmljavadocparser.resolving.CachingDocumentResolver;
import de.ialistannen.htmljavadocparser.resolving.CachingDocumentResolver.SimpleCache;
import java.util.List;
import org.jsoup.nodes.Document;
import org.togetherjava.command.commands.javadoc.JavadocSelector;

/**
 * An API helper to interact with Javadocs.
 */
public class DocsApi {

  private final JavadocApi javadocApi;

  /**
   * Creates a new docs API.
   *
   * @param config the config
   */
  public DocsApi(Toml config) {
    this.javadocApi = new JavadocApi();

    SimpleCache<String, Document> cache = new SimpleCache<>() {
      private Cache<String, Document> cache = Caffeine.newBuilder()
          .<String, Document>weigher((key, value) -> key.length() + value.outerHtml().length())
          // maximum char count. One char is 2 byte, let's use a maximum for 50 MB
          // 50 * 1024 * 1024 / 2
          // MB    KB      B
          .maximumWeight(50 * 1024 * 1024 / 2)
          .build();

      @Override
      public void put(String key, Document value) {
        cache.put(key, value);
      }

      @Override
      public Document get(String key) {
        return cache.getIfPresent(key);
      }
    };

    for (Toml javadocEntry : config.getTables("javadoc")) {
      String baseUrl = javadocEntry.getString("base-url");

      javadocApi.addApi(
          baseUrl,
          javadocEntry.getString("all-classes-appendix"),
          new CachingDocumentResolver(
              new JfxDocumentResolver(baseUrl), cache
          )
      );
    }
  }

  /**
   * Finds all classes matching the given javadoc selector.
   *
   * @param selector the selector
   * @return all classes for it
   */
  public List<? extends JavadocElement> find(JavadocSelector selector) {
    return selector.select(javadocApi);
  }

  /**
   * Returns the underlying {@link JavadocApi}.
   *
   * @return the underlying JavadocApi
   */
  public JavadocApi getUnderlyingJavadocApi() {
    return javadocApi;
  }
}
