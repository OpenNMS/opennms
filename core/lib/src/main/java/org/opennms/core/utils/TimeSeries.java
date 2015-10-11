package org.opennms.core.utils;

public abstract class TimeSeries {
    public static final String TIMESERIES_GRAPHS_ENGINE_PROPERTY = "org.opennms.web.graphs.engine";
    public static final String TIMESERIES_STRATEGY_PROPERTY = "org.opennms.timeseries.strategy";
    public static final String DEFAULT_GRAPHS_ENGINE_TYPE = "auto";

    public static final String RRD_STRATEGY_CLASS_PROPERTY = "org.opennms.rrd.strategyClass";
    public static final String RRD_TIME_SERIES_STRATEGY_NAME = "rrd";
    public static final String DEFAULT_RRD_STRATEGY_CLASS = "org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy";

    protected TimeSeries() {
        throw new UnsupportedOperationException();
    }

    public static String getTimeseriesStrategy() {
        return System.getProperty(TIMESERIES_STRATEGY_PROPERTY, RRD_TIME_SERIES_STRATEGY_NAME);
    }

    public static String getGraphEngine() {
        final String graphEngine = System.getProperty(TIMESERIES_GRAPHS_ENGINE_PROPERTY, DEFAULT_GRAPHS_ENGINE_TYPE);
        if (DEFAULT_GRAPHS_ENGINE_TYPE.equals(graphEngine)) {
            if ("newts".equals(TimeSeries.getTimeseriesStrategy())) {
                return "backshift";
            } else {
                return "png";
            }
        } else {
            return graphEngine;
        }

    }
}
