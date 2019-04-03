package org.togetherjava.markdown;

import com.overzealous.remark.convert.AbstractNodeHandler;
import com.overzealous.remark.convert.DocumentConverter;
import com.overzealous.remark.convert.NodeHandler;
import java.util.stream.Collectors;
import org.jsoup.nodes.Element;

/**
 * A handler for code blocks that supports syntax highlighting.
 */
class CodeBlockHandler extends AbstractNodeHandler {

  private String highlightLang;

  /**
   * Creates a new handler for code blocks that appends the given language syntax highlighting.
   *
   * @param highlightLang the highlighting language
   */
  CodeBlockHandler(String highlightLang) {
    this.highlightLang = highlightLang;
  }

  @Override
  public void handleNode(NodeHandler parent, Element node, DocumentConverter converter) {
    converter.getOutput().startBlock();
    converter.getOutput().println("```" + highlightLang);
    converter.getOutput().write(stripIndent(node.wholeText()));
    converter.getOutput().print("```");
    converter.getOutput().endBlock();
  }

  private String stripIndent(String input) {
    int indentation = (int) input.chars().takeWhile(Character::isWhitespace).count();
    String indentationString = " ".repeat(indentation);

    return input.lines()
        .map(s -> s.replaceFirst(indentationString, ""))
        .collect(Collectors.joining("\n"))
        .strip();
  }
}
