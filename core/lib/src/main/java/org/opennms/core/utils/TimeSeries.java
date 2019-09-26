/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.core.utils;

public abstract class TimeSeries {
    public static final String TIMESERIES_GRAPHS_ENGINE_PROPERTY = "org.opennms.web.graphs.engine";
    public static final String TIMESERIES_STRATEGY_PROPERTY = "org.opennms.timeseries.strategy";
    public static final String DEFAULT_GRAPHS_ENGINE_TYPE = "backshift";

    public static final String RRD_STRATEGY_CLASS_PROPERTY = "org.opennms.rrd.strategyClass";
    public static final String RRD_TIME_SERIES_STRATEGY_NAME = "rrd";
    public static final String DEFAULT_RRD_STRATEGY_CLASS = "org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy";

    private static final String NEWTS_TIME_SERIES_STRATEGY_NAME = "newts";

    private static final String EVALUETE_TIME_SERIES_STRATEGY_NAME = "evaluate";

    private static final String TCP_TIME_SERIES_STRATEGY_NAME = "tcp";

    public static enum Strategy {
        RRD(RRD_TIME_SERIES_STRATEGY_NAME, "RRDTool or JRobin"),
        NEWTS(NEWTS_TIME_SERIES_STRATEGY_NAME, "Newts"),
        EVALUATE(EVALUETE_TIME_SERIES_STRATEGY_NAME, "Evaluate (Sizing mode, all data discarded)"),
        TCP(TCP_TIME_SERIES_STRATEGY_NAME, "TCP (protobuf)");

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
        if ("auto".equals(graphEngine)) {
            return DEFAULT_GRAPHS_ENGINE_TYPE;
        } else {
            return graphEngine;
        }
    }
}
