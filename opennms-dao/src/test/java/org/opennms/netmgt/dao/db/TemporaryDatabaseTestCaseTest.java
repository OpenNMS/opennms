package org.opennms.netmgt.dao.db;

import java.util.Date;

public class TemporaryDatabaseTestCaseTest extends TemporaryDatabaseTestCase {
    public void testNothing() {
        // Nothing, just make sure that setUp() and tearDown() work
    }
    
    public void testExecuteSQL() {
        executeSQL("SELECT now()");
    }
    
    public void testExecuteSQLFromJdbcTemplate() {
        jdbcTemplate.queryForObject("SELECT now()", Date.class);
    }
    
    public void testExecuteSQLFromGetJdbcTemplate() {
        getJdbcTemplate().queryForObject("SELECT now()", Date.class);
    }

}
