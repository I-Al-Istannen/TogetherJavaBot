package org.togetherjava.command.commands.javadoc.formatting;

import de.ialistannen.htmljavadocparser.model.doc.BlockTag;
import de.ialistannen.htmljavadocparser.model.doc.HtmlTag;
import de.ialistannen.htmljavadocparser.model.doc.JavadocComment;
import de.ialistannen.htmljavadocparser.model.doc.JavadocCommentVisitor;
import de.ialistannen.htmljavadocparser.model.properties.JavadocElement;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;
import org.togetherjava.command.commands.javadoc.formatting.JavadocMessageFormatter.Formatter;
import org.togetherjava.markdown.HtmlMarkdownConverter;
import org.togetherjava.messaging.ComplexMessage;
import org.togetherjava.util.StringUtils;

/**
 * Formats the description with the javadoc content.
 */
class JavadocDescriptionFormatter implements Formatter {

  private HtmlMarkdownConverter htmlMarkdownConverter;

  JavadocDescriptionFormatter() {
    htmlMarkdownConverter = new HtmlMarkdownConverter();
  }

  @Override
  public boolean applies(JavadocElement element) {
    return true;
  }

  @Override
  public void format(ComplexMessage message, JavadocElement element) {
    element.getJavadoc()
        .ifPresent(javadocComment -> applyComment(javadocComment, message.getEmbedBuilder()));
  }

  private void applyComment(JavadocComment javadocComment, EmbedBuilder embedBuilder) {
    List<Field> blockTagFields = new ArrayList<>();
    StringBuilder descriptionMarkdown = new StringBuilder();

    javadocComment.acceot(new JavadocCommentVisitor() {
      @Override
      public void visitBlockTag(BlockTag tag) {
        blockTagFields.add(convertToField(tag));
      }

      @Override
      public void visitHtmlTag(HtmlTag tag) {
        convertHtml(descriptionMarkdown, tag);
      }
    });

    String description = StringUtils
        .trimToSize(descriptionMarkdown.toString(), MessageEmbed.TEXT_MAX_LENGTH);

    embedBuilder.setDescription(description);

    for (Field field : blockTagFields) {
      embedBuilder.addField(field);
    }
  }

  private void convertHtml(StringBuilder converted, HtmlTag child) {
    converted.append(htmlMarkdownConverter.convert(child.getHtml()));
  }

  private Field convertToField(BlockTag tag) {
    String name = htmlMarkdownConverter.convert(tag.getName().getHtml());
    String value = htmlMarkdownConverter.convert(tag.getValue().getHtml());

    return new Field(
        StringUtils.trimToSize(name, MessageEmbed.TITLE_MAX_LENGTH),
        StringUtils.trimToSize(value, MessageEmbed.VALUE_MAX_LENGTH),
        true
    );
  }
}
