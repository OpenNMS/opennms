package org.opennms.netmgt.dao.support;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.opennms.netmgt.rrd.RrdConfig;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy;

public class RrdTestUtils {
    // Reference the class name this way so that it is refactoring resistant
    private static final String RRD_CONFIG = "org.opennms.rrd.strategyClass=" + JRobinRrdStrategy.class.getName()
        + "\norg.opennms.rrd.usequeue=false";
    // Reference the class name this way so that it is refactoring resistant
    private static final String RRD_NULL_STRATEGY_CONFIG = "org.opennms.rrd.strategyClass=" + NullRrdStrategy.class.getName()
        + "\norg.opennms.rrd.usequeue=false";

    /**
     * This class cannot be instantiated.  Use static methods.
     */
    private RrdTestUtils() {
        
    }
    
    public static void initialize() throws IOException, RrdException {
        RrdConfig.loadProperties(new ByteArrayInputStream(RRD_CONFIG.getBytes()));
        RrdUtils.initialize();
    }
    
    public static void initializeNullStrategy() throws IOException, RrdException {
        RrdConfig.loadProperties(new ByteArrayInputStream(RRD_NULL_STRATEGY_CONFIG.getBytes()));
        RrdUtils.initialize();
    }
    
    
}
