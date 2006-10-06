package org.opennms.install;

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

}
