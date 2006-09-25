package org.opennms.install;

public class TemporaryDatabaseTestCaseTest extends TemporaryDatabaseTestCase {
    public void testNothing() {
        // Nothing, just make sure that setUp() and tearDown() work
    }
    
    public void testExecuteSQL() {
        executeSQL("SELECT now()");
    }
}
