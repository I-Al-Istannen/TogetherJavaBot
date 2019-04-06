package org.togetherjava.command.commands.javadoc;

import de.ialistannen.htmljavadocparser.resolving.DocumentResolver;
import de.ialistannen.htmljavadocparser.resolving.UrlDocumentResolver;
import de.ialistannen.htmljavadocparser.util.LinkUtils;

/**
 * A {@link DocumentResolver} for javafx classes.
 */
class JfxDocumentResolver extends UrlDocumentResolver {

  /**
   * The base url.
   *
   * @param baseUrl the base url
   */
  public JfxDocumentResolver(String baseUrl) {
    super(baseUrl);
  }

  @Override
  public String relativizeAbsoluteUrl(String absUrl) {
    if (absUrl.contains("is-external")) {
      return LinkUtils.clearQueryFragment(absUrl)
          // remove the link to the normal javadocs
          .replaceAll("https:.+?/api/", "");
    }

    return super.relativizeAbsoluteUrl(absUrl);
  }
}
