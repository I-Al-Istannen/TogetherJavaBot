package org.togetherjava.messaging.transforming;

import org.togetherjava.messaging.ComplexMessage;
import org.togetherjava.util.BuildProperties;

public class VerionInfoFooterTransformer implements Transformer<ComplexMessage, ComplexMessage> {

  @Override
  public ComplexMessage transform(ComplexMessage complexMessage) {
    String footer = BuildProperties.getGitBranch()
        + " (" + BuildProperties.getGitCommitId() + ") from " + BuildProperties.getGitCommitTime()
        + "  || " + BuildProperties.getVersion();

    return complexMessage.editEmbed(it ->
        it.setFooter(footer, "https://git-scm.com/images/logos/downloads/Git-Icon-1788C.png")
    );
  }
}
