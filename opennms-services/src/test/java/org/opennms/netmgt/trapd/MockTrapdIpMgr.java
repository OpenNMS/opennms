package org.opennms.netmgt.trapd;

/**
 * A TrapdIpMgr that doesn't talk to the database.  If we want something
 * there for our test, we'll populate it.
 */
public class MockTrapdIpMgr extends JdbcTrapdIpMgr {
    @Override
    public void afterPropertiesSet() throws Exception {
        // Don't check for the dataSource property being set
    }

    @Override
    public synchronized void dataSourceSync() {
        // Don't do anything... don't want to have to mess with the DB here
    }  
}