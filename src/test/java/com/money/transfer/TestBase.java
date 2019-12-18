package com.money.transfer;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.money.transfer.dao.AccountDao;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.junit.Before;

public class TestBase {


  protected Injector injector = Guice.createInjector(new AbstractModule() {

    @Provides
    @Singleton
    public DSLContext defaultDSLContext(DataSource dataSource) {
      DefaultConfiguration jooqConfiguration = new DefaultConfiguration();
      jooqConfiguration.set(dataSource);
      jooqConfiguration.set(SQLDialect.H2);
      return new DefaultDSLContext(jooqConfiguration);
    }

    @Provides
    @Singleton
    public DataSource dataSource() {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl("jdbc:h2:./test");
      config.setUsername("sa");
      config.setPassword("");
      return new HikariDataSource(config);
    }

    @Override
    protected void configure() {
      bind(String.class)
          .annotatedWith(Names.named("JDBC URL"))
          .toInstance("jdbc:h2:./test");

      bind(String.class)
          .annotatedWith(Names.named("JDBC user"))
          .toInstance("sa");

      bind(String.class)
          .annotatedWith(Names.named("JDBC password"))
          .toInstance("");

      bind(AccountDao.class);
    }
  });

  @Before
  public void setup() {
    injector.injectMembers(this);
  }
}