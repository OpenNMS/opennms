/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.core.utils;

public abstract class TimeSeries {
    public static final String TIMESERIES_GRAPHS_ENGINE_PROPERTY = "org.opennms.web.graphs.engine";
    public static final String TIMESERIES_STRATEGY_PROPERTY = "org.opennms.timeseries.strategy";
    public static final String DEFAULT_GRAPHS_ENGINE_TYPE = "backshift";

    public static final String RRD_STRATEGY_CLASS_PROPERTY = "org.opennms.rrd.strategyClass";
    public static final String RRD_TIME_SERIES_STRATEGY_NAME = "rrd";
    public static final String DEFAULT_RRD_STRATEGY_CLASS = "org.opennms.netmgt.rrd.rrdtool.MultithreadedJniRrdStrategy";

    private static final String NEWTS_TIME_SERIES_STRATEGY_NAME = "newts";

    private static final String EVALUATE_TIME_SERIES_STRATEGY_NAME = "evaluate";

    private static final String TCP_TIME_SERIES_STRATEGY_NAME = "tcp";

    private static final String INTEGRATION_LAYER_TIME_SERIES_STRATEGY_NAME = "integration";

    public static enum Strategy {
        RRD(RRD_TIME_SERIES_STRATEGY_NAME, "RRD implementation"),
        NEWTS(NEWTS_TIME_SERIES_STRATEGY_NAME, "Newts"),
        EVALUATE(EVALUATE_TIME_SERIES_STRATEGY_NAME, "Evaluate (Sizing mode, all data discarded)"),
        TCP(TCP_TIME_SERIES_STRATEGY_NAME, "TCP (protobuf)"),
        INTEGRATION(INTEGRATION_LAYER_TIME_SERIES_STRATEGY_NAME, "Integration (the timeseries integration layer, to be used for TimeseriesStorage implementations)");

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
