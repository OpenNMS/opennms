package org.opennms.netmgt.dao.db;

public class TemporaryDatabaseTestCaseTest extends TemporaryDatabaseTestCase {
    public void testNothing() {
        // Nothing, just make sure that setUp() and tearDown() work
    }
    
    public void testExecuteSQL() {
        executeSQL("SELECT now()");
    }
    
    public void testExecuteSQLFromJdbcTemplate() {
        jdbcTemplate.execute("SELECT now()");
    }
}
