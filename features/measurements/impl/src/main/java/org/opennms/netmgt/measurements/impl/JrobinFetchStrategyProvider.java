package org.opennms.netmgt.measurements.impl;

import org.opennms.netmgt.measurements.api.MeasurementFetchStrategy;
import org.opennms.netmgt.measurements.api.MeasurementFetchStrategyProvider;
import org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy;

public class JrobinFetchStrategyProvider implements MeasurementFetchStrategyProvider {
    @Override
    public Class<? extends MeasurementFetchStrategy> getStrategyClass(String rrdStrategyClass) {
        if(!JRobinRrdStrategy.class.getCanonicalName().equals(rrdStrategyClass)) {
            return null;
        }
        return JrobinFetchStrategy.class;
    }
}
