package org.togetherjava.storage.sql;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.sqlite.SQLiteDataSource;
import org.togetherjava.storage.dao.TagDao;
import org.togetherjava.util.IOStreamUtil;

public class Database {

  private Jdbi jdbi;

  public Database(String jdbcUrl) {
    SQLiteDataSource dataSource = new SQLiteDataSource();
    dataSource.setUrl(jdbcUrl);
    dataSource.setEnforceForeignKeys(true);

    this.jdbi = Jdbi.create(dataSource).installPlugin(new SqlObjectPlugin());

    jdbi.useHandle(handle ->
        handle.createScript(
            IOStreamUtil.readToString(IOStreamUtil.resource("/db/setup.sql"))
        ).execute()
    );
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
