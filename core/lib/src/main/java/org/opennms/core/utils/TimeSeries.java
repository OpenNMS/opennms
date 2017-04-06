package org.opennms.core.utils;

public abstract class TimeSeries {
    public static final String TIMESERIES_GRAPHS_ENGINE_PROPERTY = "org.opennms.web.graphs.engine";
    public static final String TIMESERIES_STRATEGY_PROPERTY = "org.opennms.timeseries.strategy";
    public static final String DEFAULT_GRAPHS_ENGINE_TYPE = "auto";

    public static final String RRD_STRATEGY_CLASS_PROPERTY = "org.opennms.rrd.strategyClass";
    public static final String RRD_TIME_SERIES_STRATEGY_NAME = "rrd";
    public static final String DEFAULT_RRD_STRATEGY_CLASS = "org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy";

    private static final String NEWTS_TIME_SERIES_STRATEGY_NAME = "newts";

    private static final String EVALUETE_TIME_SERIES_STRATEGY_NAME = "evaluate";

    private static final String TCP_TIME_SERIES_STRATEGY_NAME = "tcp";

    public static enum Strategy {
        RRD(RRD_TIME_SERIES_STRATEGY_NAME),
        NEWTS(NEWTS_TIME_SERIES_STRATEGY_NAME),
        EVALUATE(EVALUETE_TIME_SERIES_STRATEGY_NAME),
        TCP(TCP_TIME_SERIES_STRATEGY_NAME);

        private final String m_name;

        Strategy(String name) {
            m_name = name;
        }

        public String getName() {
            return m_name;
        }
    }

    protected TimeSeries() {
        throw new UnsupportedOperationException();
    }

    public static Strategy getTimeseriesStrategy() {
        final String effectiveStrategyName = System.getProperty(TIMESERIES_STRATEGY_PROPERTY, RRD_TIME_SERIES_STRATEGY_NAME);
        for (Strategy strategy : Strategy.values()) {
            if (strategy.getName().equalsIgnoreCase(effectiveStrategyName)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("Unsupported time series strategy: " + effectiveStrategyName);
    }

    public static String getGraphEngine() {
        final String graphEngine = System.getProperty(TIMESERIES_GRAPHS_ENGINE_PROPERTY, DEFAULT_GRAPHS_ENGINE_TYPE);
        if (DEFAULT_GRAPHS_ENGINE_TYPE.equals(graphEngine)) {
            switch(TimeSeries.getTimeseriesStrategy()) {
            case RRD:
                return "png";
            default:
                return "backshift";
            }
        } else {
            return graphEngine;
        }
    }
}
