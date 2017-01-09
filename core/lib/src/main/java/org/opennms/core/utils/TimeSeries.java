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

    public static enum Strategy {
        RRD(RRD_TIME_SERIES_STRATEGY_NAME, "RRDTool or JRobin"),
        NEWTS(NEWTS_TIME_SERIES_STRATEGY_NAME, "Newts"),
        EVALUATE(EVALUETE_TIME_SERIES_STRATEGY_NAME, "Evaluate (Sizing mode, all data discarded)");

        private final String m_name;
        private final String m_descr;

        Strategy(String name, String descr) {
        	m_name = name;
        	m_descr = descr;
        }
        Strategy(String name) {
            this(name, name);
        }

        public String getName() {
            return m_name;
        }

        public String getDescr() {
        	return m_descr;
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
