package org.togetherjava.storage.dao.jooq.dao;

import static de.ialistannen.db.autogen.tables.Tagaliases.TAGALIASES;
import static de.ialistannen.db.autogen.tables.Tags.TAGS;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import de.ialistannen.db.autogen.tables.records.TagaliasesRecord;
import de.ialistannen.db.autogen.tables.records.TagsRecord;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.togetherjava.model.ImmutableMessageTag;
import org.togetherjava.model.MessageTag;

/**
 * A DAO for {@link MessageTag}s.
 */
public class TagDao {

  private DSLContext dslContext;

  public TagDao(DSLContext dslContext) {
    this.dslContext = dslContext;
  }

  /**
   * Returns all tags.
   *
   * @return all tags
   */
  public Collection<MessageTag> getAllTags() {
    Map<String, List<String>> aliases = dslContext.selectFrom(TAGALIASES)
        .stream()
        .collect(groupingBy(
            TagaliasesRecord::getTarget,
            mapping(TagaliasesRecord::getKeyword, toList())
        ));

    return dslContext.selectFrom(TAGS)
        .stream()
        .map(toMessageTag(it -> aliases.getOrDefault(it.getKeyword(), Collections.emptyList())))
        .collect(toList());
  }

  /**
   * Returns the tag with the given keyword.
   *
   * @param keyword the tag keyword
   * @return the tag
   */
  public Optional<MessageTag> getTagForKeyword(String keyword) {
    Optional<MessageTag> foundByTag = dslContext.selectFrom(TAGS)
        .where(TAGS.KEYWORD.eq(keyword))
        .stream()
        .map(toMessageTag(a -> getAliasesFor(keyword)))
        .findFirst();

    if (foundByTag.isPresent()) {
      return foundByTag;
    }

    return dslContext.selectFrom(
        TAGALIASES.leftJoin(TAGS)
            .on(TAGALIASES.TARGET.eq(TAGS.KEYWORD))
    )
        .where(TAGALIASES.KEYWORD.eq(keyword))
        .stream()
        .map(it -> it.into(TAGS))
        .map(toMessageTag(tag -> getAliasesFor(tag.getKeyword())))
        .findFirst();
  }

  private List<String> getAliasesFor(String keyword) {
    return dslContext.selectFrom(TAGALIASES)
        .where(TAGALIASES.TARGET.eq(keyword))
        .stream()
        .map(TagaliasesRecord::getKeyword)
        .collect(toList());
  }

  /**
   * Adds a tag or updates an existing one.
   *
   * @param tag the tag to add or update
   */
  public void addOrUpdate(MessageTag tag) {
    dslContext.transaction(it -> {
      DSLContext context = DSL.using(it);
      boolean existsAlready = context.fetchExists(
          context.selectFrom(TAGS)
              .where(TAGS.KEYWORD.eq(tag.keyword()))
      );

      if (existsAlready) {
        context.update(TAGS)
            .set(TAGS.KEYWORD, tag.keyword())
            .set(TAGS.VALUE, tag.value())
            .set(TAGS.DESCRIPTION, tag.description())
            .set(TAGS.CREATOR, tag.creator())
            .execute();
      } else {
        context.insertInto(TAGS)
            .set(TAGS.KEYWORD, tag.keyword())
            .set(TAGS.VALUE, tag.value())
            .set(TAGS.DESCRIPTION, tag.description())
            .set(TAGS.CREATOR, tag.creator())
            .execute();
      }
      updateAliases(tag, context);
    });
  }

  /**
   * Deletes a tag.
   *
   * @param tag the tag to delete
   */
  public void deleteTag(MessageTag tag) {
    dslContext.deleteFrom(TAGS)
        .where(TAGS.KEYWORD.eq(tag.keyword()))
        .execute();
  }

  private void updateAliases(MessageTag tag, DSLContext context) {
    context.deleteFrom(TAGALIASES)
        .where(TAGALIASES.TARGET.eq(tag.keyword()))
        .execute();

    List<TagaliasesRecord> records = tag.aliases().stream()
        .map(keyword -> {
          TagaliasesRecord record = TAGALIASES.newRecord();
          record.setKeyword(keyword);
          record.setTarget(tag.keyword());
          return record;
        })
        .collect(toList());

    context.batchInsert(records).execute();
  }

  private Function<TagsRecord, MessageTag> toMessageTag(
      Function<TagsRecord, List<String>> aliases) {
    return it -> ImmutableMessageTag.builder()
        .keyword(it.getKeyword())
        .description(it.getDescription())
        .creator(it.getCreator())
        .value(it.getValue())
        .aliases(aliases.apply(it))
        .build();
  }
}
