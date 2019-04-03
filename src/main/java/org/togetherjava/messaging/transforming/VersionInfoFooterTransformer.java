package org.togetherjava.messaging.transforming;

import net.dv8tion.jda.core.EmbedBuilder;
import org.togetherjava.util.BuildProperties;

/**
 * Displays version information in the footer.
 */
public class VersionInfoFooterTransformer implements Transformer<EmbedBuilder, EmbedBuilder> {

  @Override
  public EmbedBuilder transform(EmbedBuilder embed) {
    String footer = BuildProperties.getGitBranch()
        + " (" + BuildProperties.getGitCommitId() + ") from " + BuildProperties.getGitCommitTime()
        + "  || " + BuildProperties.getVersion();

    return embed.setFooter(
        footer,
        "https://git-scm.com/images/logos/downloads/Git-Icon-1788C.png"
    );
  }
}
