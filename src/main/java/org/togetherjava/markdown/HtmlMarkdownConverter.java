package org.togetherjava.markdown;

import com.overzealous.remark.Options;
import com.overzealous.remark.Options.FencedCodeBlocks;
import com.overzealous.remark.Remark;
import org.jsoup.nodes.Element;

/**
 * A HTML to markdown converter.
 */
public class HtmlMarkdownConverter {

  private Remark remark;

  /**
   * Creates a new HTML to Markdown converter.
   */
  public HtmlMarkdownConverter() {
    Options options = new Options();
    options.inlineLinks = true;
    options.fencedCodeBlocks = FencedCodeBlocks.ENABLED_BACKTICK;
    options.fencedCodeBlocksWidth = 3;

    this.remark = new Remark(options);

    remark.getConverter().addBlockNode(new NullBlockHandler(), "blockquote");
    remark.getConverter().addBlockNode(new CodeBlockHandler("java"), "pre");
  }

  /**
   * Converts an element to markdown.
   *
   * @param element the element to convert
   * @return the converted string
   */
  public String convert(Element element) {
    String baseUri = element.ownerDocument().baseUri();

    return remark.convertFragment(element.html(), baseUri);
  }

}
