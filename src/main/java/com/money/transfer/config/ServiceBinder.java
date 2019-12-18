package com.money.transfer.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;

public class ServiceBinder extends AbstractModule {

  private static final String JDBC_URL = "jdbc:h2:./test";
  private static final String USER = "sa";
  private static final String PASSWORD = "";

  @Provides
  @Singleton
  public DataSource dataSource() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(JDBC_URL);
    config.setUsername(USER);
    config.setPassword(PASSWORD);
    return new HikariDataSource(config);
  }

  @Provides
  @Singleton
  public DSLContext defaultDSLContext(DataSource dataSource) {
    DefaultConfiguration jooqConfiguration = new DefaultConfiguration();
    jooqConfiguration.set(dataSource);
    jooqConfiguration.set(SQLDialect.H2);
    return new DefaultDSLContext(jooqConfiguration);
  }

  @Override
  protected void configure() {
    bind(String.class)
        .annotatedWith(Names.named("JDBC URL"))
        .toInstance(JDBC_URL);

    bind(String.class)
        .annotatedWith(Names.named("JDBC user"))
        .toInstance(USER);

    bind(String.class)
        .annotatedWith(Names.named("JDBC password"))
        .toInstance(PASSWORD);
  }

}
