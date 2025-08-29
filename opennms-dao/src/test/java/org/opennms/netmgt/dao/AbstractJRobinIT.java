package org.opennms.netmgt.dao;

import org.junit.BeforeClass;

public abstract class AbstractJRobinIT {
    @BeforeClass
    public static void beforeClass() {
        System.setProperty("org.opennms.rrd.strategyClass", "org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy");
    }
}
