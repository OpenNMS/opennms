package org.opennms.netmgt.dao.db;

import java.sql.SQLException;

import org.opennms.netmgt.config.DataSourceFactory;

public class AbstractTransactionalTemporaryDatabaseSpringContextTestsTest
        extends AbstractTransactionalTemporaryDatabaseSpringContextTests {

    @Override
    protected String[] getConfigLocations() {
        return new String[] {
                "classpath:META-INF/opennms/applicationContext-AbstractTransactionalTemporaryDatabaseSpringContextTestsTest.xml"
        };
    }
    
    public void testNothing() {
    }
    
    /**
     * Make sure that we're calling setDirty()
     *
     */
    public void testNothingAgain() {
    }
    
    public void testGetConnectionFromDataSource() throws SQLException {
        jdbcTemplate.getDataSource().getConnection().close();
    }
    
    public void testGetConnectionFromFactory() throws SQLException {
        DataSourceFactory.getInstance().getConnection().close();
    }
    
    public void testJdbcTemplateExecute() throws SQLException {
        jdbcTemplate.execute("SELECT now()");
    }

}
