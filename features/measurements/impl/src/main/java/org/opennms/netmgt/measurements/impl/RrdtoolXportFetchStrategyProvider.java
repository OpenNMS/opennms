package org.opennms.netmgt.measurements.impl;

import org.opennms.netmgt.measurements.api.MeasurementFetchStrategy;
import org.opennms.netmgt.measurements.api.MeasurementFetchStrategyProvider;
import org.opennms.netmgt.rrd.rrdtool.JniRrdStrategy;

public class RrdtoolXportFetchStrategyProvider implements MeasurementFetchStrategyProvider {
    @Override
    public Class<? extends MeasurementFetchStrategy> getStrategyClass(String rrdStrategyClass) {
        if(!JniRrdStrategy.class.getCanonicalName().equals(rrdStrategyClass)) {
            return null;
        }
        return RrdtoolXportFetchStrategy.class;
    }
}
