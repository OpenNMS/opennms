package org.opennms.netmgt.threshd;

import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockUtil;

import junit.framework.TestCase;

public class ThreshdTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ThreshdTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        MockUtil.setupLogging();
        MockDatabase db = new MockDatabase();
        DatabaseConnectionFactory.setInstance(db);
        
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void xtestthreshd() {
        Threshd threshd = new Threshd();
        
        threshd.init();
        threshd.start();
        threshd.stop();
    }
}
