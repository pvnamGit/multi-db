package com.multitenantmultidb.multidb.service;

import com.multitenantmultidb.multidb.model.Tenant;
import com.multitenantmultidb.multidb.repository.TenantRepository;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@EnableConfigurationProperties(LiquibaseProperties.class)
public class TenantService {

  private static final String VALID_DATABASE_NAME_REGEXP = "[A-Za-z0-9_]*";
  private final JdbcTemplate jdbcTemplate;
  @Qualifier("tenantLiquibaseProperties")
  private final LiquibaseProperties liquibaseProperties;
  private final ResourceLoader resourceLoader;
  private final TenantRepository tenantRepository;

  @Value("${multitenancy.tenant.datasource.url-prefix}")
  private String urlPrefix;

  public void createTenant(String db, String password) {

    // Verify db string to prevent SQL injection
    if (!db.matches(VALID_DATABASE_NAME_REGEXP)) {
      throw new TenantCreationException("Invalid db name: " + db);
    }
    String tenantId = UUID.randomUUID().toString();
    String url = urlPrefix + db;
    try {
      createDatabase(db, password);
    } catch (DataAccessException e) {
      throw new TenantCreationException("Error when creating db: " + db, e);
    }
    try (Connection connection = DriverManager.getConnection(url, db, password)) {
      DataSource tenantDataSource = new SingleConnectionDataSource(connection, false);
      runLiquibase(tenantDataSource);
    } catch (SQLException | LiquibaseException e) {
      throw new TenantCreationException("Error when populating db: ", e);
    }
    Tenant tenant = Tenant.builder()
        .tenantId(tenantId)
        .db(db)
        .password(password)
        .build();
    tenantRepository.save(tenant);
  }

  private void createDatabase(String db, String password) {
    jdbcTemplate.execute((StatementCallback<Boolean>) stmt -> stmt.execute("CREATE DATABASE " + db));
    jdbcTemplate.execute((StatementCallback<Boolean>) stmt -> stmt.execute("CREATE USER " + db + " WITH PASSWORD '" + password + "'"));
    jdbcTemplate.execute((StatementCallback<Boolean>) stmt -> stmt.execute("GRANT ALL PRIVILEGES ON DATABASE " + db + " TO " + db));
  }

  private void runLiquibase(DataSource dataSource) throws LiquibaseException {
    SpringLiquibase liquibase = getSpringLiquibase(dataSource);
    liquibase.afterPropertiesSet();
  }

  private SpringLiquibase getSpringLiquibase(DataSource dataSource) {
    SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setResourceLoader(resourceLoader);
    liquibase.setDataSource(dataSource);
    liquibase.setChangeLog(liquibaseProperties.getChangeLog());
    liquibase.setContexts(liquibaseProperties.getContexts());
    liquibase.setDefaultSchema(liquibaseProperties.getDefaultSchema());
    liquibase.setLiquibaseSchema(liquibaseProperties.getLiquibaseSchema());
    liquibase.setLiquibaseTablespace(liquibaseProperties.getLiquibaseTablespace());
    liquibase.setDatabaseChangeLogTable(liquibaseProperties.getDatabaseChangeLogTable());
    liquibase.setDatabaseChangeLogLockTable(liquibaseProperties.getDatabaseChangeLogLockTable());
    liquibase.setDropFirst(liquibaseProperties.isDropFirst());
    liquibase.setShouldRun(liquibaseProperties.isEnabled());
    liquibase.setLabels(liquibaseProperties.getLabels());
    liquibase.setChangeLogParameters(liquibaseProperties.getParameters());
    liquibase.setRollbackFile(liquibaseProperties.getRollbackFile());
    liquibase.setTestRollbackOnUpdate(liquibaseProperties.isTestRollbackOnUpdate());
    return liquibase;
  }
}
