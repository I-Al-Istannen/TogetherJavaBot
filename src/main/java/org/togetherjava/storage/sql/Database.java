package org.togetherjava.storage.sql;

import java.sql.SQLException;
import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.sqlite.SQLiteDataSource;
import org.togetherjava.storage.dao.jooq.dao.TagDao;

/**
 * The main database class.
 */
public class Database {

  private final DSLContext dslContext;
  private final TagDao tagDao;

  /**
   * Creates a new database.
   *
   * @param jdbcUrl the url to the database
   * @throws SQLException if no connection could be established
   */
  public Database(String jdbcUrl) throws SQLException {
    SQLiteDataSource dataSource = new SQLiteDataSource();
    dataSource.setUrl(jdbcUrl);
    dataSource.setEnforceForeignKeys(true);

    Flyway flyway = Flyway.configure()
        .dataSource(dataSource)
        .load();
    flyway.migrate();

    this.dslContext = DSL.using(dataSource.getConnection(), SQLDialect.SQLITE);
    this.tagDao = new TagDao(dslContext);
  }

  /**
   * Returns the database dsl context.
   *
   * @return the database dsl context
   */
  public DSLContext getDslContext() {
    return dslContext;
  }

  /**
   * Returns the tag DAO.
   *
   * @return the tag dao
   */
  public TagDao getTagDao() {
    return tagDao;
  }
}
