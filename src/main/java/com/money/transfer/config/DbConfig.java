package com.money.transfer.config;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DbConfig {

  private String jdbcUrl;
  private String user;
  private String password;

  @Inject
  public DbConfig(@Named("JDBC URL") String jdbcUrl,
      @Named("JDBC user") String user,
      @Named("JDBC password") String password) {
    this.jdbcUrl = jdbcUrl;
    this.user = user;
    this.password = password;
  }

  public void dropTable() {
    try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password)) {
      Statement st = null;
      st = conn.createStatement();
      st.execute("DROP TABLE IF EXISTS ACCOUNT");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void initDb() {
    try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password)) {

      Statement st = null;
      st = conn.createStatement();
      st.execute("CREATE TABLE IF NOT EXISTS ACCOUNT ( \n"
          + "   id int IDENTITY(1,1)  NOT NULL , \n"
          + "   amount long NOT NULL\n"
          + ");");
      st.execute("INSERT INTO account VALUES(1, 1000)");
      st.execute("INSERT INTO account VALUES(2, 10)");
      st.closeOnCompletion();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

}
