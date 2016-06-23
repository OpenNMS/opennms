package org.opennms.netmgt.measurements.impl;

import org.opennms.core.utils.TimeSeries;
import org.opennms.netmgt.measurements.api.MeasurementFetchStrategy;
import org.opennms.netmgt.measurements.api.MeasurementFetchStrategyProvider;
import org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy;

public class JrobinFetchStrategyProvider implements MeasurementFetchStrategyProvider {
    @Override
    public Class<? extends MeasurementFetchStrategy> getStrategyClass(String timeSeriesStrategyName, String rrdStrategyClass) {
        if(!TimeSeries.RRD_TIME_SERIES_STRATEGY_NAME.equalsIgnoreCase(timeSeriesStrategyName) ||
                !JRobinRrdStrategy.class.getCanonicalName().equals(rrdStrategyClass)) {
            return null;
        }
        return JrobinFetchStrategy.class;
    }
}
