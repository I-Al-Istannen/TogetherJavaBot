package org.togetherjava.storage.dao;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.RowMapperFactory;
import org.jdbi.v3.core.statement.StatementContext;
import org.togetherjava.model.ImmutableMessageTag;
import org.togetherjava.model.MessageTag;

public class MessageTagMapperFacory implements RowMapperFactory {

  @Override
  public Optional<RowMapper<?>> build(Type type, ConfigRegistry config) {
    if (type == MessageTag.class) {
      return Optional.of(new MessageTagMapper());
    }
    return Optional.empty();
  }

  private class MessageTagMapper implements RowMapper<MessageTag> {

    @Override
    public MessageTag map(ResultSet rs, StatementContext ctx) throws SQLException {
      String keyword = rs.getString("keyword");
      String description = rs.getString("description");
      String value = rs.getString("value");
      long creator = rs.getLong("creator");

      return ImmutableMessageTag.builder()
          .keyword(keyword)
          .description(description)
          .value(value)
          .creator(creator)
          .build();
    }
  }

}