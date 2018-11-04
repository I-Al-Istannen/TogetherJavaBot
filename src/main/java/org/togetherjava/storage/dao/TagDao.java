package org.togetherjava.storage.dao;

import java.util.List;
import java.util.Optional;
import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapperFactory;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.togetherjava.model.MessageTag;
import org.togetherjava.model.MessageTagAlias;

@RegisterRowMapperFactory(MessageTagMapperFacory.class)
public interface TagDao extends SqlObject {

  /**
   * Returns all stored tags, but no aliases.
   *
   * @return all stored tags
   */
  @SqlQuery("SELECT * FROM Tags")
  List<MessageTag> getAllTags();

  /**
   * Returns all stored aliases.
   *
   * @return all stored tags
   */
  @SqlQuery(
      "SELECT * FROM TagAliases"
  )
  List<MessageTagAlias> getAllAliases();


  /**
   * Returns a {@link MessageTag} by keyword, if present.
   *
   * @param keyword the keyword of the message tag
   * @return the tag if found
   */
  @SqlQuery(
      "SELECT * "
          + "FROM Tags "
          + "WHERE keyword = :keyword "
          + "UNION "
          + "SELECT TagAliases.keyword, T.description, T.value, T.creator "
          + "FROM TagAliases "
          + "       JOIN Tags T on TagAliases.target = T.keyword "
          + "WHERE TagAliases.keyword = :keyword;"
  )
  Optional<MessageTag> getByKeyword(String keyword);

  /**
   * Deletes a tag with a given keyword.
   *
   * @param keyword the keyword of the message tag
   * @return the amount of affected rows. 1 if it was deleted, 0 if it didn't exist
   */
  @SqlUpdate("DELETE FROM Tags WHERE keyword = :keyword")
  int deleteTag(String keyword);

  /**
   * Adds a message tag to the db.
   *
   * @param tag the tag to add
   */
  @SqlUpdate(
      "INSERT INTO Tags (keyword, description, value, creator) VALUES "
          + "(:keyword, :description, :value, :creator)"
  )
  void addTag(@BindMethods MessageTag tag);

  /**
   * Edits a given tag (matched by keyword) to reflect the passed one.
   *
   * @param tag the new tag
   */
  @SqlUpdate(
      "UPDATE Tags SET description = :description, value = :value, creator = :creator "
          + "WHERE keyword = :keyword"
  )
  void editTag(@BindMethods MessageTag tag);

  /**
   * Adds a new tag alias.
   *
   * @param keyword the keyword of the message tag
   * @param target the target to alias it to
   */
  @SqlUpdate("INSERT INTO TagAliases (keyword, target) VALUES (:keyword, :target)")
  int addAlias(String keyword, String target);

  /**
   * Gets an alias by keyword.
   *
   * @param keyword the keyword for this alias
   */
  @SqlQuery("SELECT * FROM TagAliases WHERE keyword = :keyword")
  Optional<MessageTagAlias> getAlias(String keyword);

  /**
   * Deletes an existing tag alias.
   *
   * @param keyword the keyword of the message tag
   */
  @SqlUpdate("DELETE FROM TagAliases WHERE keyword = :keyword")
  int deleteAlias(String keyword);
}
