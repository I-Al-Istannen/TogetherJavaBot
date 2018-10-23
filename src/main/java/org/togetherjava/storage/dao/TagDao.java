package org.togetherjava.storage.dao;

import java.util.List;
import java.util.Optional;
import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapperFactory;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.togetherjava.model.MessageTag;

@RegisterRowMapperFactory(MessageTagMapperFacory.class)
public interface TagDao extends SqlObject {

  /**
   * Returns all stored tags.
   *
   * @return all stored tags
   */
  @SqlQuery("SELECT * FROM Tags")
  List<MessageTag> getAll();


  /**
   * Returns a {@link MessageTag} by keyword, if present.
   *
   * @param keyword the keyword of the message tag
   * @return the tag if found
   */
  @SqlQuery("SELECT * FROM Tags WHERE keyword = :keyword")
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
}
