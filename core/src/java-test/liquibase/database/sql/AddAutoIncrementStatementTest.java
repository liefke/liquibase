package liquibase.database.sql;

import liquibase.database.*;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.template.JdbcTemplate;
import liquibase.exception.JDBCException;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.TestContext;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class AddAutoIncrementStatementTest extends AbstractSqlStatementTest {

    private static final String TABLE_NAME = "AddAutoIncTest";
    private static final String COLUMN_NAME = "testCol";
    private static final String COLUMN_TYPE = "int";

    @Before
    public void dropTable() throws Exception {
        for (Database database : TestContext.getInstance().getAvailableDatabases()) {

            dropAndCreateTable(new CreateTableStatement(TABLE_NAME)
                            .addPrimaryKeyColumn(COLUMN_NAME, COLUMN_TYPE)
                            .addColumn("otherColumn", "varchar(50)"), database);

            if (database.supportsSchemas()) {
                dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
                                .addPrimaryKeyColumn(COLUMN_NAME, COLUMN_TYPE)
                                .addColumn("otherColumn", "varchar(50)"), database);
            }
        }
    }

    protected AddAutoIncrementStatement generateTestStatement() {
        return new AddAutoIncrementStatement(null, null, null, null);
    }

    @Test
    public void supportsDatabase() throws Exception {
        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                if (database instanceof OracleDatabase
                        || database instanceof MSSQLDatabase
                        || database instanceof PostgresDatabase
                        || database instanceof DerbyDatabase
                        || database instanceof CacheDatabase
                        || database instanceof H2Database
                        || database instanceof FirebirdDatabase) {
                    assertFalse(new AddAutoIncrementStatement(TABLE_NAME, COLUMN_NAME, COLUMN_TYPE).supportsDatabase(database));
                } else {
                    assertTrue(new AddAutoIncrementStatement(TABLE_NAME, COLUMN_NAME, COLUMN_TYPE).supportsDatabase(database));
                }
            }

        });
    }

    @Test
    public void execute_defaultSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                AddAutoIncrementStatement statement = new AddAutoIncrementStatement(TABLE_NAME, COLUMN_NAME, COLUMN_TYPE);

                if (statement.supportsDatabase(database)) {

                    DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                    assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isAutoIncrement());

                    new JdbcTemplate(database).execute(statement);

                    snapshot = new DatabaseSnapshot(database);
                    assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isAutoIncrement());
                } else {
                    try {
                        statement.getSqlStatement(database);
                        fail("Did not throw expected exception");
                    } catch (StatementNotSupportedOnDatabaseException e) {
                        ;
                    }
                }
            }
        });
    }

    @Test
    public void execute_alternateSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                AddAutoIncrementStatement statement = new AddAutoIncrementStatement(TestContext.ALT_SCHEMA, TABLE_NAME, COLUMN_NAME, COLUMN_TYPE);

                if (statement.supportsDatabase(database)) {

                    DatabaseSnapshot snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
                    assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isAutoIncrement());

                    new JdbcTemplate(database).execute(statement);

                    snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
                    assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isAutoIncrement());
                } else {
                    try {
                        statement.getSqlStatement(database);
                        fail("Did not throw expected exception");
                    } catch (StatementNotSupportedOnDatabaseException e) {
                        ;
                    }
                }
            }
        });
    }
}
