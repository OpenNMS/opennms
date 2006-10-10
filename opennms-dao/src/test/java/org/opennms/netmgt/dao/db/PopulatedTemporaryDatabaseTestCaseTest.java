package org.opennms.netmgt.dao.db;

public class PopulatedTemporaryDatabaseTestCaseTest extends
        PopulatedTemporaryDatabaseTestCase {

    /**
     * Can we properly initialize the TestCase (including loading the database)?
     *
     */
    public void testNothing() {
        // nothing... this class mainly tests our super's setUp() 
    }

    public void testExecuteSQL() {
        executeSQL("SELECT now()");
    }
    
    public void testExecuteSQLFromJdbcTemplate() {
        jdbcTemplate.execute("SELECT now()");
    }

}
