package org.togetherjava.markdown;

import com.overzealous.remark.convert.AbstractNodeHandler;
import com.overzealous.remark.convert.DocumentConverter;
import com.overzealous.remark.convert.NodeHandler;
import org.jsoup.nodes.Element;

/**
 * A block handler that just does nothing and recurses down to the children.
 */
class NullBlockHandler extends AbstractNodeHandler {

  @Override
  public void handleNode(NodeHandler parent, Element node, DocumentConverter converter) {
    converter.getOutput().startBlock();
    converter.walkNodes(this, node);
    converter.getOutput().endBlock();
  }
}
