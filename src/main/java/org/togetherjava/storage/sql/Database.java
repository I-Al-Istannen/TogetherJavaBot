package org.togetherjava.storage.sql;

import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.sqlite.SQLiteDataSource;
import org.togetherjava.storage.dao.TagDao;

public class Database {

  private Jdbi jdbi;

  public Database(String jdbcUrl) {
    SQLiteDataSource dataSource = new SQLiteDataSource();
    dataSource.setUrl(jdbcUrl);
    dataSource.setEnforceForeignKeys(true);

    Flyway flyway = Flyway.configure()
        .dataSource(dataSource)
        .load();
    flyway.migrate();

    this.jdbi = Jdbi.create(dataSource).installPlugin(new SqlObjectPlugin());
  }

  /**
   * Returns the tag dao.
   *
   * @return the creates {@link TagDao} class
   */
  public TagDao getTagDao() {
    return jdbi.onDemand(TagDao.class);
  }
}
